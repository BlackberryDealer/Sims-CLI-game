package simcli.entities.models;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Something small a Sim wants to do today. Each morning every Sim gets a
 * few wants; doing one earns satisfaction points to spend on
 * {@link Reward}s, and a {@link Moodlet#WISH_CAME_TRUE} moodlet.
 *
 * <p>A want is done when the Sim's life has moved on since the wants were
 * rolled — compared with {@link SimSnapshot}s by
 * {@link simcli.engine.InnerLifeManager}. Wants that fit a Sim's
 * aspiration or traits come up more often.</p>
 */
public enum Want {

    /** Cook a meal at a fridge. */
    COOK_A_MEAL("Cook a meal", 100, (was, now) -> now.meals > was.meals,
            EnumSet.of(Aspiration.BIG_FAMILY), EnumSet.noneOf(Trait.class)),

    /** Work a shift. */
    WORK_A_SHIFT("Work a shift", 100, (was, now) -> now.shifts > was.shifts,
            EnumSet.of(Aspiration.CAREER_STAR), EnumSet.noneOf(Trait.class)),

    /** Earn some money. */
    EARN_MONEY("Earn $300", 150, (was, now) -> now.moneyEarned >= was.moneyEarned + 300,
            EnumSet.of(Aspiration.CAREER_STAR, Aspiration.HOMEOWNER), EnumSet.noneOf(Trait.class)),

    /** Gain a level in any skill. */
    LEVEL_UP_A_SKILL("Level up a skill", 200, (was, now) -> now.totalSkillLevels() > was.totalSkillLevels(),
            EnumSet.of(Aspiration.SKILL_MASTER), EnumSet.noneOf(Trait.class)),

    /** Work out on a weight bench. */
    WORK_OUT("Work out", 75, (was, now) -> now.xp(SkillType.FITNESS) > was.xp(SkillType.FITNESS),
            EnumSet.of(Aspiration.SKILL_MASTER), EnumSet.of(Trait.ACTIVE)),

    /** Study at a computer. */
    STUDY("Study something", 75, (was, now) -> now.xp(SkillType.LOGIC) > was.xp(SkillType.LOGIC),
            EnumSet.of(Aspiration.SKILL_MASTER), EnumSet.noneOf(Trait.class)),

    /** Get to know someone better. */
    SPEND_TIME("Spend time with someone", 100, (was, now) -> now.totalRelationship() >= was.totalRelationship() + 5,
            EnumSet.of(Aspiration.SOCIAL_BUTTERFLY, Aspiration.BIG_FAMILY), EnumSet.of(Trait.SOCIALITE)),

    /** Become friends with someone new. */
    MAKE_A_FRIEND("Make a new friend", 250, (was, now) -> !SimSnapshot.added(was.friends, now.friends).isEmpty(),
            EnumSet.of(Aspiration.SOCIAL_BUTTERFLY), EnumSet.of(Trait.SOCIALITE)),

    /** Buy anything. */
    BUY_SOMETHING("Buy something new", 75, (was, now) -> now.itemsBought > was.itemsBought,
            EnumSet.of(Aspiration.HOMEOWNER), EnumSet.noneOf(Trait.class)),

    /** Get into a great mood. */
    GREAT_MOOD("Be in a great mood", 150, (was, now) -> now.greatMood,
            EnumSet.noneOf(Aspiration.class), EnumSet.of(Trait.LAZY));

    private final String text;
    private final int points;
    private final BiPredicate<SimSnapshot, SimSnapshot> done;
    private final Set<Aspiration> aspirations;
    private final Set<Trait> traits;

    Want(String text, int points, BiPredicate<SimSnapshot, SimSnapshot> done,
         Set<Aspiration> aspirations, Set<Trait> traits) {
        this.text = text;
        this.points = points;
        this.done = done;
        this.aspirations = aspirations;
        this.traits = traits;
    }

    /** @return what the Sim wants, e.g. "Cook a meal". */
    public String getText() {
        return text;
    }

    /** @return the satisfaction points it's worth. */
    public int getPoints() {
        return points;
    }

    /**
     * Returns whether the want has been done since it was rolled.
     *
     * @param atRoll the Sim when the want was rolled.
     * @param now    the Sim now.
     * @return {@code true} if done.
     */
    public boolean isDone(SimSnapshot atRoll, SimSnapshot now) {
        return done.test(atRoll, now);
    }

    /**
     * Returns whether this want makes sense for the Sim today, judged on
     * the Sim alone (whether the home has the right furniture is checked
     * by the caller).
     *
     * @param sim the Sim.
     * @param day today's day number.
     * @return {@code true} if it could be rolled.
     */
    public boolean suits(Sim sim, int day) {
        boolean employed = sim.canWork() && sim.getCareer() != Job.UNEMPLOYED;
        switch (this) {
            case WORK_A_SHIFT:
            case EARN_MONEY:
                return employed && !TimeManager.isWeekend(day);
            case LEVEL_UP_A_SKILL:
                for (SkillType skill : SkillType.values()) {
                    if (sim.getSkillLevel(skill) < GameConstants.MAX_SKILL_LEVEL) {
                        return true;
                    }
                }
                return false;
            case BUY_SOMETHING:
                return sim.getMoney() >= 50;
            case GREAT_MOOD:
                return !sim.isInGreatMood();
            default:
                return true;
        }
    }

    /**
     * Returns how likely this want is to come up for the Sim: more likely
     * when it fits their aspiration or one of their traits.
     *
     * @param sim the Sim.
     * @return a weight of 1 or more.
     */
    public int weightFor(Sim sim) {
        int weight = 1;
        if (aspirations.contains(sim.getAspiration())) {
            weight += 2;
        }
        for (Trait t : sim.getTraits()) {
            if (traits.contains(t)) {
                weight += 2;
            }
        }
        return weight;
    }
}
