package simcli.entities.managers;

import simcli.engine.SimulationLogger;
import simcli.entities.models.SkillType;
import simcli.utils.GameConstants;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks a Sim's experience in each {@link SkillType}.
 *
 * <p>Skills are stored as raw experience points. Each level costs more
 * than the last: level <i>n</i> needs {@link GameConstants#SKILL_XP_BASE}
 * + {@link GameConstants#SKILL_XP_STEP} × (<i>n</i> − 1) more points, so
 * early levels come quickly and the top ones take real dedication. Skills
 * stop at {@link GameConstants#MAX_SKILL_LEVEL}.</p>
 *
 * <p>Practice ({@link #addPractice}) is limited to
 * {@link GameConstants#SKILL_DAILY_XP_CAP} experience per skill per day,
 * so a skill can't be crammed: maxing one takes a few weeks of steady
 * practice. {@link #startNewDay()} resets the limit.</p>
 *
 * <p>Extracted from {@link simcli.entities.actors.Sim} like the other
 * managers, so the Sim class only delegates.</p>
 */
public class SkillManager {

    /** Experience points per skill. */
    private final Map<SkillType, Integer> experience;

    /** Experience gained from practice today, per skill. */
    private final Map<SkillType, Integer> practisedToday = new EnumMap<>(SkillType.class);

    /** Constructs a SkillManager with every skill at zero. */
    public SkillManager() {
        this.experience = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            experience.put(type, 0);
        }
    }

    /**
     * Adds experience from practice (studying, working out, socialising,
     * work, school), up to {@link GameConstants#SKILL_DAILY_XP_CAP} per
     * skill per day. Practice beyond the limit is wasted, and the Sim is
     * told so once.
     *
     * @param type    the skill practised.
     * @param points  the experience the practice is worth.
     * @param simName the Sim's display name (used in log messages).
     * @return {@code true} if the skill went up at least one level.
     */
    public boolean addPractice(SkillType type, int points, String simName) {
        if (points <= 0) {
            return false;
        }
        int already = practisedToday.getOrDefault(type, 0);
        int granted = Math.max(0, Math.min(points, GameConstants.SKILL_DAILY_XP_CAP - already));
        practisedToday.put(type, already + granted);
        if (granted < points && already < GameConstants.SKILL_DAILY_XP_CAP + 1) {
            practisedToday.put(type, GameConstants.SKILL_DAILY_XP_CAP + 1); // only say it once a day
            SimulationLogger.getInstance().log(simName + " has learned all they can about "
                    + type.getDisplayName() + " for today. Try something else, or come back tomorrow.");
        }
        return addExperience(type, granted, simName);
    }

    /**
     * Returns how much more experience a skill can gain from practice today.
     *
     * @param type the skill.
     * @return experience points left today.
     */
    public int getPracticeLeftToday(SkillType type) {
        return Math.max(0, GameConstants.SKILL_DAILY_XP_CAP - practisedToday.getOrDefault(type, 0));
    }

    /** Starts a new day: practice limits reset. */
    public void startNewDay() {
        practisedToday.clear();
    }

    /**
     * Returns the current level of a skill (0 to {@link GameConstants#MAX_SKILL_LEVEL}).
     *
     * @param type the skill to look up.
     * @return the skill level.
     */
    public int getLevel(SkillType type) {
        int xp = getExperience(type);
        int level = 0;
        while (level < GameConstants.MAX_SKILL_LEVEL && xp >= experienceForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    /**
     * Returns the total experience needed to reach a skill level.
     *
     * @param level the level (0 to {@link GameConstants#MAX_SKILL_LEVEL}).
     * @return the experience points needed, e.g. 10 for level 1, 25 for level 2.
     */
    public static int experienceForLevel(int level) {
        int n = Math.max(0, Math.min(level, GameConstants.MAX_SKILL_LEVEL));
        return n * GameConstants.SKILL_XP_BASE + GameConstants.SKILL_XP_STEP * n * (n - 1) / 2;
    }

    /**
     * Returns the raw experience points in a skill.
     *
     * @param type the skill to look up.
     * @return the experience points.
     */
    public int getExperience(SkillType type) {
        return experience.get(type);
    }

    /**
     * Sets the raw experience points in a skill. Used for save/load restoration.
     *
     * @param type   the skill to set.
     * @param points the experience points (negative values are clamped to 0).
     */
    public void setExperience(SkillType type, int points) {
        experience.put(type, Math.max(0, Math.min(points, maxExperience())));
    }

    /**
     * Returns how far the skill is through its current level, as a
     * percentage (100 once the skill is maxed).
     *
     * @param type the skill to look up.
     * @return progress towards the next level, 0–100.
     */
    public int getProgressPercent(SkillType type) {
        int level = getLevel(type);
        if (level >= GameConstants.MAX_SKILL_LEVEL) {
            return 100;
        }
        int start = experienceForLevel(level);
        int span = experienceForLevel(level + 1) - start;
        return (getExperience(type) - start) * 100 / span;
    }

    /**
     * Adds experience to a skill and announces any level gained.
     *
     * @param type    the skill to train.
     * @param points  the experience points to add.
     * @param simName the Sim's display name (used in log messages).
     * @return {@code true} if the skill went up at least one level.
     */
    public boolean addExperience(SkillType type, int points, String simName) {
        if (points <= 0) {
            return false;
        }
        int before = getLevel(type);
        setExperience(type, getExperience(type) + points);
        int after = getLevel(type);
        if (after > before) {
            SimulationLogger.getInstance().log("\n*** SKILL UP! " + simName + " reached "
                    + type.getDisplayName() + " level " + after + "! ***");
            return true;
        }
        return false;
    }

    private static int maxExperience() {
        return experienceForLevel(GameConstants.MAX_SKILL_LEVEL);
    }
}
