package simcli.entities.components;

import simcli.entities.managers.SkillManager;
import simcli.entities.models.SkillType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Skill Manager Tests")
public class SkillManagerTest {
    private SkillManager skillManager;

    @BeforeEach
    void setUp() {
        skillManager = new SkillManager();
    }

    @Test
    @DisplayName("Skills start at level 0")
    void testInitialSkills() {
        assertEquals(0, skillManager.getSkillLevel(SkillType.LOGIC));
        assertEquals(0, skillManager.getSkillLevel(SkillType.COOKING));
        assertEquals(0, skillManager.getSkillLevel(SkillType.CHARISMA));
    }

    @Test
    @DisplayName("Adding base experience increases skill level correctly")
    void testAddExperience() {
        skillManager.addSkillExperience(SkillType.COOKING, 15, "Bob", false);
        assertEquals(1, skillManager.getSkillLevel(SkillType.COOKING), "15 XP should be Level 1");
        
        skillManager.addSkillExperience(SkillType.COOKING, 10, "Bob", false);
        assertEquals(2, skillManager.getSkillLevel(SkillType.COOKING), "25 total XP should be Level 2");
    }

    @Test
    @DisplayName("Fast learner trait grants 50% extra experience")
    void testFastLearner() {
        // 10 xp * 1.5 = 15 xp (Level 1)
        skillManager.addSkillExperience(SkillType.LOGIC, 10, "Alice", true);
        assertEquals(1, skillManager.getSkillLevel(SkillType.LOGIC));

        // 6 xp * 1.5 = 9 xp. Total 24 xp (Level 2)
        skillManager.addSkillExperience(SkillType.LOGIC, 6, "Alice", true);
        assertEquals(2, skillManager.getSkillLevel(SkillType.LOGIC));
    }
}
