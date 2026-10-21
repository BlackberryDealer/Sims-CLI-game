package simcli.entities.managers;

import simcli.engine.SimulationLogger;
import simcli.entities.models.SkillType;
import simcli.utils.GameConstants;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks a Sim's experience in each {@link SkillType}.
 *
 * <p>Skills are stored as raw experience points; every
 * {@link GameConstants#SKILL_XP_PER_LEVEL} points is one level, up to
 * {@link GameConstants#MAX_SKILL_LEVEL}.</p>
 *
 * <p>Extracted from {@link simcli.entities.actors.Sim} like the other
 * managers, so the Sim class only delegates.</p>
 */
public class SkillManager {

    /** Experience points per skill. */
    private final Map<SkillType, Integer> experience;

    /** Constructs a SkillManager with every skill at zero. */
    public SkillManager() {
        this.experience = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            experience.put(type, 0);
        }
    }

    /**
     * Returns the current level of a skill (0 to {@link GameConstants#MAX_SKILL_LEVEL}).
     *
     * @param type the skill to look up.
     * @return the skill level.
     */
    public int getLevel(SkillType type) {
        return Math.min(GameConstants.MAX_SKILL_LEVEL, getExperience(type) / GameConstants.SKILL_XP_PER_LEVEL);
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
        if (getLevel(type) >= GameConstants.MAX_SKILL_LEVEL) {
            return 100;
        }
        return getExperience(type) % GameConstants.SKILL_XP_PER_LEVEL * 100 / GameConstants.SKILL_XP_PER_LEVEL;
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
        return GameConstants.MAX_SKILL_LEVEL * GameConstants.SKILL_XP_PER_LEVEL;
    }
}
