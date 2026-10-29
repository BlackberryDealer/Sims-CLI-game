package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.utils.GameConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Family life across the generations: births, grandparents mentoring
 * teens, and what happens when a Sim dies.
 *
 * <ul>
 *     <li><b>Births</b> — a pregnancy ends on its due day, and the baby joins
 *         the household.</li>
 *     <li><b>Mentoring</b> — every day, each elder passes on their best
 *         skill to every teen in the household, and enjoys doing it.</li>
 *     <li><b>Inheritance</b> — when a Sim dies, their money goes to their
 *         spouse, otherwise to their children, otherwise to whoever else
 *         is at home. When the last of a generation is gone, the household
 *         announces that the next generation has taken charge.</li>
 * </ul>
 *
 * <p>Owned by {@link GameLoop}.</p>
 */
public class FamilyManager {

    /** Where family news is reported. */
    private final SimulationLogger logger;

    /** Sims whose death has already been dealt with. */
    private final Set<Sim> mourned = Collections.newSetFromMap(new IdentityHashMap<>());

    /** The oldest generation still living and playable, once known. */
    private int headGeneration = -1;

    /**
     * Creates a FamilyManager.
     *
     * @param logger where births, mentoring and inheritance are reported.
     */
    public FamilyManager(SimulationLogger logger) {
        this.logger = logger;
    }

    /**
     * Runs the family's daily events for the day that has just begun.
     *
     * @param day       the day that has just begun.
     * @param household every Sim in the household (babies are added to it).
     */
    public void processDay(int day, List<Sim> household) {
        deliverBabies(day, household);
        mentor(household);
    }

    /** Any pregnancy that's due ends with a new baby in the household. */
    private void deliverBabies(int day, List<Sim> household) {
        for (Sim mother : new ArrayList<>(household)) {
            if (!mother.isPregnant() || mother.getPregnancyDueDay() > day) {
                continue;
            }
            String name = mother.getExpectedBabyName();
            if (mother.getState() == SimState.DEAD) {
                mother.endPregnancy(day);
                continue;
            }
            Sim baby = mother.getRelationshipManager().finalizeBaby(name, mother.getExpectedBabyGender());
            mother.endPregnancy(day);
            household.add(baby);
            logger.log(baby.getName() + " has joined the household - welcome, little one!");
        }
    }

    /** Elders teach the household's teens their best skill. */
    private void mentor(List<Sim> household) {
        for (Sim elder : household) {
            if (elder.getState() == SimState.DEAD || elder.getAge() < GameConstants.ELDER_AGE) {
                continue;
            }
            SkillType best = bestSkill(elder);
            if (best == null) {
                continue;
            }
            for (Sim teen : household) {
                if (teen.getState() != SimState.DEAD && teen.getAge() >= GameConstants.TEEN_AGE
                        && teen.getAge() < GameConstants.ADULT_AGE) {
                    teen.trainSkill(best, GameConstants.MENTOR_XP);
                    elder.getHappiness().increase(GameConstants.GRANDPARENT_CARE_FUN);
                    elder.getSocial().increase(GameConstants.GRANDPARENT_CARE_SOCIAL);
                    logger.log(elder.getName() + " spends time teaching " + teen.getName() + " about "
                            + best.getDisplayName() + ". (+" + best.getDisplayName() + ")");
                }
            }
        }
    }

    /** The elder's highest skill, or {@code null} if they have none to teach. */
    private static SkillType bestSkill(Sim sim) {
        SkillType best = null;
        for (SkillType skill : SkillType.values()) {
            int level = sim.getSkillLevel(skill);
            if (level > 0 && (best == null || level > sim.getSkillLevel(best))) {
                best = skill;
            }
        }
        return best;
    }

    /**
     * Deals with anyone in the household who has died since the last check:
     * passes their money on, and announces when a new generation takes over.
     *
     * @param household every Sim in the household.
     */
    public void checkDeaths(List<Sim> household) {
        if (headGeneration < 0) {
            headGeneration = oldestLivingGeneration(household);
        }
        for (Sim sim : household) {
            if (sim.getState() == SimState.DEAD && mourned.add(sim)) {
                bequeath(sim, household);
            }
        }
        int head = oldestLivingGeneration(household);
        if (head > headGeneration && head > 0) {
            logger.log("\n*** A new generation takes charge: generation " + head
                    + " now carries on the family legacy. ***");
        }
        if (head > 0) {
            headGeneration = head;
        }
    }

    /** Passes a dead Sim's money to their heirs. */
    private void bequeath(Sim deceased, List<Sim> household) {
        int money = deceased.getMoney();
        if (money <= 0) {
            return;
        }
        List<Sim> heirs = heirs(deceased, household);
        if (heirs.isEmpty()) {
            return;
        }
        int share = money / heirs.size();
        int leftover = money - share * heirs.size();
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < heirs.size(); i++) {
            Sim heir = heirs.get(i);
            heir.setMoney(heir.getMoney() + share + (i == 0 ? leftover : 0));
            if (names.length() > 0) names.append(", ");
            names.append(heir.getName());
        }
        deceased.setMoney(0);
        logger.log(deceased.getName() + " leaves $" + money + " to " + names + ".");
    }

    /**
     * Who inherits from a Sim: their living spouse in the household,
     * otherwise their living children in the household, otherwise everyone
     * else still alive at home.
     *
     * @param deceased  the Sim who died.
     * @param household every Sim in the household.
     * @return the heirs, possibly empty.
     */
    static List<Sim> heirs(Sim deceased, List<Sim> household) {
        List<Sim> heirs = new ArrayList<>();
        Sim spouse = deceased.getRelationshipManager().getSpouse();
        if (spouse != null && household.contains(spouse) && spouse.getState() != SimState.DEAD) {
            heirs.add(spouse);
            return heirs;
        }
        for (Sim child : deceased.getRelationshipManager().getChildren()) {
            if (household.contains(child) && child.getState() != SimState.DEAD) {
                heirs.add(child);
            }
        }
        if (!heirs.isEmpty()) {
            return heirs;
        }
        for (Sim sim : household) {
            if (sim != deceased && sim.getState() != SimState.DEAD) {
                heirs.add(sim);
            }
        }
        return heirs;
    }

    /** The lowest generation among living, playable Sims, or -1 if there are none. */
    private static int oldestLivingGeneration(List<Sim> household) {
        int oldest = -1;
        for (Sim sim : household) {
            if (sim.getState() != SimState.DEAD && sim.isPlayable()
                    && (oldest < 0 || sim.getGeneration() < oldest)) {
                oldest = sim.getGeneration();
            }
        }
        return oldest;
    }
}
