package simcli.entities.models;

import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A frozen copy of the parts of a Sim's life that wants, fears, moodlets
 * and memories care about.
 *
 * <p>Comparing a snapshot from the start of a turn (or the start of the
 * day) with one taken now shows what happened in between — a meal cooked,
 * a promotion, a new friend — without every action having to report it.</p>
 */
public final class SimSnapshot {

    public final boolean alive;
    public final int age;
    public final String stage;
    public final Job career;
    public final int jobTier;
    public final int timesFired;
    public final int daysMissed;
    public final int shifts;
    public final int meals;
    public final int moneyEarned;
    public final int itemsBought;
    public final int money;
    public final int hunger;
    public final int energy;
    public final int hygiene;
    public final int fun;
    public final int social;
    public final boolean greatMood;
    public final int aspirationsFulfilled;
    public final Sim spouse;
    public final Set<Sim> children;
    public final Set<Sim> friends;
    public final Map<Sim, Integer> relationships;
    /** People close to the Sim (family and friends) who have died. */
    public final Set<Sim> lostLovedOnes;
    private final int[] skillXp;
    private final int[] skillLevels;

    private SimSnapshot(Sim sim) {
        alive = sim.getState() != SimState.DEAD;
        age = sim.getAge();
        stage = sim.getCurrentStageName();
        career = sim.getCareer();
        jobTier = sim.getJobTier();
        timesFired = sim.getCareerManager().getTimesFired();
        daysMissed = sim.getCareerManager().getConsecutiveDaysMissed();
        shifts = sim.getCareerManager().getTotalShiftsWorked();
        meals = sim.getMealsCooked();
        moneyEarned = sim.getTotalMoneyEarned();
        itemsBought = sim.getTotalItemsBought();
        money = sim.getMoney();
        hunger = sim.getHunger().getValue();
        energy = sim.getEnergy().getValue();
        hygiene = sim.getHygiene().getValue();
        fun = sim.getHappiness().getValue();
        social = sim.getSocial().getValue();
        greatMood = sim.isInGreatMood();
        aspirationsFulfilled = sim.getAspirationsFulfilled();
        spouse = sim.getRelationshipManager().getSpouse();
        children = Collections.unmodifiableSet(new HashSet<>(sim.getRelationshipManager().getChildren()));
        relationships = Collections.unmodifiableMap(new HashMap<>(sim.getRelationshipManager().getRelationships()));

        Set<Sim> friendSet = new HashSet<>();
        Set<Sim> lost = new HashSet<>();
        for (Map.Entry<Sim, Integer> rel : relationships.entrySet()) {
            if (rel.getValue() >= GameConstants.FRIEND_THRESHOLD) {
                friendSet.add(rel.getKey());
            }
        }
        Set<Sim> close = new HashSet<>(friendSet);
        close.addAll(children);
        if (spouse != null) {
            close.add(spouse);
        }
        for (Sim other : close) {
            if (other != sim && other.getState() == SimState.DEAD) {
                lost.add(other);
            }
        }
        friendSet.removeIf(other -> other.getState() == SimState.DEAD);
        friends = Collections.unmodifiableSet(friendSet);
        lostLovedOnes = Collections.unmodifiableSet(lost);

        SkillType[] skills = SkillType.values();
        skillXp = new int[skills.length];
        skillLevels = new int[skills.length];
        for (int i = 0; i < skills.length; i++) {
            skillXp[i] = sim.getSkillManager().getExperience(skills[i]);
            skillLevels[i] = sim.getSkillManager().getLevel(skills[i]);
        }
    }

    /**
     * Takes a snapshot of a Sim right now.
     *
     * @param sim the Sim.
     * @return the snapshot.
     */
    public static SimSnapshot of(Sim sim) {
        return new SimSnapshot(sim);
    }

    /**
     * @param skill the skill.
     * @return experience in that skill when the snapshot was taken.
     */
    public int xp(SkillType skill) {
        return skillXp[skill.ordinal()];
    }

    /**
     * @param skill the skill.
     * @return level in that skill when the snapshot was taken.
     */
    public int level(SkillType skill) {
        return skillLevels[skill.ordinal()];
    }

    /** @return the sum of every skill level. */
    public int totalSkillLevels() {
        int total = 0;
        for (int level : skillLevels) {
            total += level;
        }
        return total;
    }

    /** @return the sum of every positive relationship score. */
    public int totalRelationship() {
        int total = 0;
        for (int score : relationships.values()) {
            total += Math.max(0, score);
        }
        return total;
    }

    /**
     * Returns whether any relationship is lower now than in an earlier snapshot.
     *
     * @param earlier the earlier snapshot.
     * @return {@code true} if someone thinks less of this Sim than before.
     */
    public boolean anyRelationshipDroppedSince(SimSnapshot earlier) {
        for (Map.Entry<Sim, Integer> rel : relationships.entrySet()) {
            Integer before = earlier.relationships.get(rel.getKey());
            if (before != null && rel.getValue() < before) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Sims in {@code now} that weren't in {@code before}.
     *
     * @param before the earlier set.
     * @param now    the later set.
     * @return the newcomers, in no particular order.
     */
    public static List<Sim> added(Set<Sim> before, Set<Sim> now) {
        Set<Sim> diff = new HashSet<>(now);
        diff.removeAll(before);
        return List.copyOf(diff);
    }
}
