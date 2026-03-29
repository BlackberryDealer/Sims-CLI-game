package simcli.entities.managers;

import simcli.entities.models.SkillType;

import java.util.HashMap;
import java.util.Map;
import simcli.engine.SimulationLogger;
import simcli.utils.GameConstants;

/**
 * Tracks experience points and levels for each {@link SkillType}.
 * Every 10 XP equals one skill level. The {@link simcli.entities.models.Trait#FAST_LEARNER}
 * trait grants 50% bonus XP on all skill gains.
 */
public class SkillManager {
    private Map<SkillType, Integer> skills;

    public SkillManager() {
        this.skills = new HashMap<>();
        for (SkillType type : SkillType.values()) {
            this.skills.put(type, 0);
        }
    }

    public int getSkillLevel(SkillType type) {
        return skills.getOrDefault(type, 0) / 10;
    }

    public void addSkillExperience(SkillType type, int amount, String simName, boolean isFastLearner) {
        if (isFastLearner) {
            amount = (int) (amount * GameConstants.BONUS_TIMES); // 50% extra xp
        }
        int current = skills.get(type);
        int newExp = current + amount;
        skills.put(type, newExp);

        // Every 10 points is a new level
        if (current / 10 < newExp / 10) {
            SimulationLogger.getInstance()
                    .log("\n*** SKILL UP! " + simName + " reached level " + (newExp / 10) + " in " + type + "! ***");
        }
    }
}
