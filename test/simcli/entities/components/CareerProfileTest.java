package simcli.entities.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Job;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Career Profile — Job Path Tests")
public class CareerProfileTest {
    private CareerProfile profile;
    
    @BeforeEach
    void setUp() { 
        profile = new CareerProfile(Job.SOFTWARE_ENGINEER); 
    }

    @Test
    @DisplayName("checkTruancy() demotes/fires player when days missed > 3")
    void testTruancyDemotion() {
        profile.checkTruancy("Bob");
        profile.checkTruancy("Bob"); // missed 2 days
        assertEquals(2, profile.getConsecutiveDaysMissed());
        
        profile.promote("Bob"); // Tier 2
        assertEquals(2, profile.getJobTier());
        
        profile.checkTruancy("Bob"); // missed 3 days
        profile.checkTruancy("Bob"); // missed 4 days -> FIRED
        
        // Assert Demotion and Reset
        assertEquals(Job.UNEMPLOYED, profile.getCareer());
        assertEquals(1, profile.getJobTier());
        assertEquals(0, profile.getConsecutiveDaysMissed());
    }

    @Test
    @DisplayName("promote() correctly updates and limits to max Tier")
    void testPromotionLocksAtMaxTier() {
        int maxTier = Job.SOFTWARE_ENGINEER.getMaxTier();
        
        for (int i = 0; i < maxTier + 5; i++) {
            profile.promote("Bob");
        }
        
        assertEquals(maxTier, profile.getJobTier(), "Job tier should naturally lock against its maximum constraint.");
    }
}
