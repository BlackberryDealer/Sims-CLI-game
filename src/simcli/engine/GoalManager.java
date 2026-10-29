package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.Achievement;
import simcli.entities.models.Aspiration;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.entities.models.StarterGoal;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks the household's goals: each Sim's {@link Aspiration} and the
 * household's {@link Achievement}s.
 *
 * <p>{@link #check(List)} runs once per turn. When a Sim fulfils their
 * aspiration they earn {@link GameConstants#ASPIRATION_REWARD_MONEY} and a
 * big mood boost; once every living, playable Sim in the household has
 * fulfilled theirs, the household's legacy is complete — an ending to aim
 * for other than everyone dying. Play can carry on afterwards: a Sim who
 * has fulfilled their aspiration can choose another, and the household
 * keeps building its {@link #getLegacyScore(List) legacy score} down the
 * generations.</p>
 *
 * <p>It also runs the first week's {@link StarterGoal starter goals}, small
 * cash rewards for learning the main systems.</p>
 */
public class GoalManager {

    /** The city, for checking which homes have been bought. May be {@code null}. */
    private final IWorldManager worldManager;

    /** Where goals are announced. */
    private final SimulationLogger logger;

    /** Achievements the household has unlocked. */
    private final Set<Achievement> unlocked = EnumSet.noneOf(Achievement.class);

    /** Whether the legacy-complete ending has been reached. */
    private boolean legacyComplete;

    /** Starter goals the household has completed. */
    private final Set<StarterGoal> startersDone = EnumSet.noneOf(StarterGoal.class);

    /** Whether the household has ever paid its weekly bills in full. */
    private boolean billsPaid;

    /**
     * Creates a GoalManager.
     *
     * @param worldManager the city map, or {@code null} (no home can count as bought).
     * @param logger       the logger for announcing goals.
     */
    public GoalManager(IWorldManager worldManager, SimulationLogger logger) {
        this.worldManager = worldManager;
        this.logger = logger;
    }

    /**
     * Progress towards a goal: {@code current} out of {@code target}.
     */
    public static final class Progress {
        private final int current;
        private final int target;

        Progress(int current, int target) {
            this.current = Math.min(current, target);
            this.target = target;
        }

        /** @return how far the Sim has got. */
        public int getCurrent() {
            return current;
        }

        /** @return what the Sim needs to reach. */
        public int getTarget() {
            return target;
        }

        /** @return {@code true} once the target is reached. */
        public boolean isComplete() {
            return current >= target;
        }

        /** @return progress as a percentage, 0–100. */
        public int getPercent() {
            return target == 0 ? 100 : current * 100 / target;
        }
    }

    /**
     * Measures a Sim's progress towards an aspiration.
     *
     * @param sim        the Sim.
     * @param aspiration the aspiration to measure.
     * @return the progress made.
     */
    public Progress progress(Sim sim, Aspiration aspiration) {
        switch (aspiration) {
            case CAREER_STAR: {
                Job job = sim.getCareer();
                if (job == Job.UNEMPLOYED) {
                    return new Progress(0, 1);
                }
                return new Progress(sim.getJobTier(), job.getMaxTier());
            }
            case BIG_FAMILY:
                return new Progress(sim.getRelationshipManager().getChildren().size(),
                        GameConstants.FAMILY_ASPIRATION_CHILDREN);
            case HOMEOWNER:
                return new Progress(ownsBoughtHome() ? 1 : 0, 1);
            case SKILL_MASTER:
                return new Progress(bestSkill(sim), GameConstants.MAX_SKILL_LEVEL);
            case SOCIAL_BUTTERFLY:
                return new Progress(friendCount(sim), GameConstants.SOCIAL_ASPIRATION_FRIENDS);
            default:
                return new Progress(0, 1);
        }
    }

    /**
     * Checks every goal and announces and rewards anything newly reached.
     *
     * @param household every Sim in the household.
     */
    public void check(List<Sim> household) {
        for (Sim sim : household) {
            if (isGoalHolder(sim) && !sim.isAspirationFulfilled()
                    && progress(sim, sim.getAspiration()).isComplete()) {
                sim.setAspirationFulfilled(true);
                sim.setMoney(sim.getMoney() + GameConstants.ASPIRATION_REWARD_MONEY);
                sim.addTotalMoneyEarned(GameConstants.ASPIRATION_REWARD_MONEY);
                sim.getHappiness().increase(100);
                logger.log("\n*** ASPIRATION FULFILLED! " + sim.getName() + " is a true "
                        + sim.getAspiration().getDisplayName() + "! (+$" + GameConstants.ASPIRATION_REWARD_MONEY
                        + ") ***");
                if (!nextAspirations(sim).isEmpty()) {
                    logger.log(sim.getName() + " can choose a new aspiration on the Goals screen [G].");
                }
            }
        }

        for (StarterGoal goal : StarterGoal.values()) {
            if (!startersDone.contains(goal)) {
                Sim achiever = starterAchiever(goal, household);
                if (achiever != null) {
                    startersDone.add(goal);
                    achiever.setMoney(achiever.getMoney() + goal.getReward());
                    achiever.addTotalMoneyEarned(goal.getReward());
                    logger.log("First steps: " + goal.getDescription() + " - done! (+$" + goal.getReward()
                            + " to " + achiever.getName() + ")");
                }
            }
        }

        for (Achievement achievement : Achievement.values()) {
            if (!unlocked.contains(achievement) && isEarned(achievement, household)) {
                unlocked.add(achievement);
                logger.log("\n*** ACHIEVEMENT UNLOCKED: " + achievement.getDisplayName() + " - "
                        + achievement.getDescription() + " ***");
            }
        }

        if (!legacyComplete && allAspirationsFulfilled(household)) {
            legacyComplete = true;
            logger.log("\n*************************************************"
                    + "\n*  LEGACY COMPLETE! Every Sim in the household   *"
                    + "\n*  has fulfilled their life's aspiration.        *"
                    + "\n*  You've won - but life goes on if you like.    *"
                    + "\n*************************************************");
        }
    }

    /**
     * Returns whether every living, playable Sim in the household has
     * fulfilled their aspiration (and there is at least one).
     *
     * @param household every Sim in the household.
     * @return {@code true} if the household's legacy is complete.
     */
    public boolean allAspirationsFulfilled(List<Sim> household) {
        boolean anyone = false;
        for (Sim sim : household) {
            if (isGoalHolder(sim)) {
                anyone = true;
                if (!sim.isAspirationFulfilled()) {
                    return false;
                }
            }
        }
        return anyone;
    }

    private boolean isEarned(Achievement achievement, List<Sim> household) {
        if (achievement == Achievement.HOMEOWNER) {
            return ownsBoughtHome();
        }
        if (achievement == Achievement.FORTUNE) {
            int total = 0;
            for (Sim sim : household) {
                if (sim.getState() != SimState.DEAD) {
                    total += sim.getMoney();
                }
            }
            return total >= GameConstants.FORTUNE_AMOUNT;
        }
        for (Sim sim : household) {
            if (sim.getState() == SimState.DEAD) {
                continue;
            }
            switch (achievement) {
                case FIRST_JOB:
                    if (sim.getCareer() != Job.UNEMPLOYED) return true;
                    break;
                case PROMOTED:
                    if (sim.getCareer() != Job.UNEMPLOYED && sim.getJobTier() >= 2) return true;
                    break;
                case TOP_TIER:
                    if (sim.getCareer() != Job.UNEMPLOYED && sim.getJobTier() >= sim.getCareer().getMaxTier()) {
                        return true;
                    }
                    break;
                case GOOD_FRIENDS:
                    if (friendCount(sim) > 0) return true;
                    break;
                case MARRIED:
                    if (sim.getRelationshipManager().getSpouse() != null) return true;
                    break;
                case NEW_BABY:
                    if (sim.isChildSim()) return true;
                    break;
                case GROWN_UP:
                    if (sim.isChildSim() && sim.isPlayable()) return true;
                    break;
                case SKILLED:
                    if (bestSkill(sim) >= 5) return true;
                    break;
                case MASTER:
                    if (bestSkill(sim) >= GameConstants.MAX_SKILL_LEVEL) return true;
                    break;
                case GOLDEN_YEARS:
                    if (sim.getAge() >= GameConstants.ELDER_AGE) return true;
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * Who completed a starter goal (and gets the reward), or {@code null}
     * if nobody has yet.
     */
    private Sim starterAchiever(StarterGoal goal, List<Sim> household) {
        Sim richest = null;
        for (Sim sim : household) {
            if (sim.getState() == SimState.DEAD) {
                continue;
            }
            if (richest == null || sim.getMoney() > richest.getMoney()) {
                richest = sim;
            }
            switch (goal) {
                case GET_JOB:
                    if (sim.getCareer() != Job.UNEMPLOYED) return sim;
                    break;
                case FIRST_SHIFT:
                    if (sim.getCareerManager().getTotalShiftsWorked() > 0) return sim;
                    break;
                case COOK_MEAL:
                    if (sim.getMealsCooked() > 0) return sim;
                    break;
                case MEET_SOMEONE:
                    for (int score : sim.getRelationshipManager().getRelationships().values()) {
                        if (score >= GameConstants.FLIRT_MIN_RELATIONSHIP) return sim;
                    }
                    break;
                default:
                    break;
            }
        }
        return goal == StarterGoal.PAY_BILLS && billsPaid ? richest : null;
    }

    /** Records that the household paid its weekly bills in full. */
    public void recordBillsPaid() {
        billsPaid = true;
    }

    /**
     * Returns the aspirations a Sim could move on to once their current one
     * is fulfilled: every aspiration they haven't fulfilled yet.
     *
     * @param sim the Sim.
     * @return the aspirations still open to them.
     */
    public List<Aspiration> nextAspirations(Sim sim) {
        List<Aspiration> open = new ArrayList<>();
        for (Aspiration a : Aspiration.values()) {
            if (a != sim.getAspiration() && !sim.getPastAspirations().contains(a)) {
                open.add(a);
            }
        }
        return open;
    }

    /**
     * Returns the highest generation of the household to have come of age
     * (reached their teens), living or not.
     *
     * @param household every Sim in the household.
     * @return the generation, at least 1.
     */
    public static int getHighestGeneration(List<Sim> household) {
        int highest = 1;
        for (Sim sim : household) {
            if (sim.getAge() >= GameConstants.TEEN_AGE) {
                highest = Math.max(highest, sim.getGeneration());
            }
        }
        return highest;
    }

    /**
     * Returns the household's legacy score: points for every aspiration
     * ever fulfilled, every achievement, and every generation after the
     * first to come of age.
     *
     * @param household every Sim in the household, including those who have died.
     * @return the score.
     */
    public int getLegacyScore(List<Sim> household) {
        int aspirations = 0;
        for (Sim sim : household) {
            aspirations += sim.getAspirationsFulfilled();
        }
        return aspirations * GameConstants.LEGACY_POINTS_ASPIRATION
                + unlocked.size() * GameConstants.LEGACY_POINTS_ACHIEVEMENT
                + (getHighestGeneration(household) - 1) * GameConstants.LEGACY_POINTS_GENERATION;
    }

    /** Only living Sims the player can control pursue aspirations. */
    private static boolean isGoalHolder(Sim sim) {
        return sim.getState() != SimState.DEAD && sim.isPlayable();
    }

    /** Whether the household owns a home it had to buy. */
    private boolean ownsBoughtHome() {
        if (worldManager == null) {
            return false;
        }
        for (Building building : worldManager.getCityMap()) {
            if (building instanceof Residential) {
                Residential home = (Residential) building;
                if (home.isOwned() && home.getPurchasePrice() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int bestSkill(Sim sim) {
        int best = 0;
        for (SkillType skill : SkillType.values()) {
            best = Math.max(best, sim.getSkillLevel(skill));
        }
        return best;
    }

    private static int friendCount(Sim sim) {
        int friends = 0;
        for (Map.Entry<Sim, Integer> rel : sim.getRelationshipManager().getRelationships().entrySet()) {
            if (rel.getValue() >= GameConstants.FRIEND_THRESHOLD) {
                friends++;
            }
        }
        return friends;
    }

    // -------------------------------------------------------------------------
    // State (for the goals screen and save/load)
    // -------------------------------------------------------------------------

    /**
     * Returns whether an achievement has been unlocked.
     *
     * @param achievement the achievement.
     * @return {@code true} if unlocked.
     */
    public boolean isUnlocked(Achievement achievement) {
        return unlocked.contains(achievement);
    }

    /**
     * Returns every unlocked achievement.
     *
     * @return the unlocked achievements.
     */
    public Set<Achievement> getUnlocked() {
        return unlocked;
    }

    /**
     * Marks an achievement as unlocked without announcing it. Used when loading a save.
     *
     * @param achievement the achievement.
     */
    public void restoreUnlocked(Achievement achievement) {
        unlocked.add(achievement);
    }

    /** @return {@code true} once the household's legacy is complete. */
    public boolean isLegacyComplete() {
        return legacyComplete;
    }

    /**
     * Sets whether the legacy is complete. Used when loading a save.
     *
     * @param legacyComplete the saved flag.
     */
    public void setLegacyComplete(boolean legacyComplete) {
        this.legacyComplete = legacyComplete;
    }

    /**
     * Returns whether a starter goal is done.
     *
     * @param goal the starter goal.
     * @return {@code true} if completed.
     */
    public boolean isStarterDone(StarterGoal goal) {
        return startersDone.contains(goal);
    }

    /** @return every completed starter goal. */
    public Set<StarterGoal> getStartersDone() {
        return startersDone;
    }

    /**
     * Marks a starter goal done without paying its reward. Used when loading a save.
     *
     * @param goal the starter goal.
     */
    public void restoreStarter(StarterGoal goal) {
        startersDone.add(goal);
        if (goal == StarterGoal.PAY_BILLS) {
            billsPaid = true;
        }
    }
}
