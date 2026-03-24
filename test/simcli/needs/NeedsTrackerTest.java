package simcli.needs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.SimState;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeedsTracker — Sim State Conversion Tests")
public class NeedsTrackerTest {

    @Test
    @DisplayName("SimState becomes DEAD if hunger hits 0")
    void testStarvingDeathState() {
        NeedsTracker tracker = new NeedsTracker();
        tracker.getHunger().setValue(0);
        tracker.updateState();
        assertEquals(SimState.DEAD, tracker.getState(), "If hunger drops to 0 or below, Sim dies.");
    }
    
    @Test
    @DisplayName("SimState becomes HUNGRY if hunger drops to warning limits")
    void testHungryState() {
        NeedsTracker tracker = new NeedsTracker();
        tracker.getHunger().setValue(simcli.utils.GameConstants.HUNGER_WARNING_LEVEL - 5);
        tracker.updateState();
        assertEquals(SimState.HUNGRY, tracker.getState(), "If hunger <= HUNGER_WARNING_LEVEL, Sim enters hunger state.");
    }
    
    @Test
    @DisplayName("SimState becomes TIRED if energy drops below limits")
    void testTiredState() {
        NeedsTracker tracker = new NeedsTracker();
        // Energy must be <= 20
        tracker.getEnergy().setValue(15);
        tracker.updateState();
        assertEquals(SimState.TIRED, tracker.getState(), "If energy <= 20, Sim enters tired state.");
    }
}
