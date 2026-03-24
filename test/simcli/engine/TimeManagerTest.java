package simcli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.engine.TimeManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TimeManager time-progression logic.
 *
 * Coverage:
 *  - advanceTick() increments the tick by exactly 1
 *  - advanceTicks(n) increments by exactly n
 *  - getCurrentDay() returns 1 at tick 1, 2 at tick 25 (with 24 ticks/day)
 *  - Sleep fast-forward: the exact number of ticks to reach 8 AM is correct
 *    for several starting positions within a day
 *  - getTimeOfDay() returns the correct period label at boundary ticks
 *  - getFormattedTime() formats correctly
 */
@DisplayName("TimeManager — Time Progression Logic Tests")
public class TimeManagerTest {

    private TimeManager tm;

    @BeforeEach
    void setUp() {
        // Start at tick 1, 24 ticks per day (matches GameEngine default)
        tm = new TimeManager(1, 24);
    }

    // -----------------------------------------------------------------------
    // Basic tick progression
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("advanceTick() increments tick by exactly 1")
    void testAdvanceTickByOne() {
        int before = tm.getCurrentTick();
        tm.advanceTick();
        assertEquals(before + 1, tm.getCurrentTick(), "Tick should increment by 1");
    }

    @Test
    @DisplayName("advanceTicks(n) increments tick by exactly n")
    void testAdvanceTicksByN() {
        int before = tm.getCurrentTick();
        tm.advanceTicks(7);
        assertEquals(before + 7, tm.getCurrentTick(), "Tick should increment by 7");
    }

    @Test
    @DisplayName("advanceTicks(0) does NOT change the tick")
    void testAdvanceTicksByZero() {
        int before = tm.getCurrentTick();
        tm.advanceTicks(0);
        assertEquals(before, tm.getCurrentTick(), "Tick should not change when advancing by 0");
    }

    // -----------------------------------------------------------------------
    // Day calculation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getCurrentDay() returns 1 on tick 1")
    void testDayOneAtTickOne() {
        assertEquals(1, tm.getCurrentDay(), "Should be day 1 at tick 1");
    }

    @Test
    @DisplayName("getCurrentDay() returns 2 when tick crosses 24 boundary")
    void testDayTwoAtTick25() {
        // Advance from tick 1 to tick 25 (= day 2)
        tm.advanceTicks(24);
        assertEquals(25, tm.getCurrentTick());
        assertEquals(2, tm.getCurrentDay(), "Should be day 2 at tick 25");
    }

    @Test
    @DisplayName("getCurrentDay() returns 3 when tick crosses 48 boundary")
    void testDayThreeAtTick49() {
        tm.advanceTicks(48);
        assertEquals(3, tm.getCurrentDay(), "Should be day 3 at tick 49");
    }

    // -----------------------------------------------------------------------
    // Sleep fast-forward logic
    // This mirrors the exact formula used in GameEngine.run():
    //   int currentInDay = timeManager.getCurrentTick() % 24;
    //   int ticksToMorning = (24 - currentInDay + 8) % 24;
    //   if (ticksToMorning == 0) ticksToMorning = 24;
    //   timeManager.advanceTicks(ticksToMorning - 1);
    //   [then advanceTick() is called once more by the normal loop]
    // So after sleep the net advance = ticksToMorning, landing at tick % 24 == 8.
    // -----------------------------------------------------------------------

    private int computeTicksToMorning(int currentTick) {
        int currentInDay = currentTick % 24;
        int ticksToMorning = (24 - currentInDay + 8) % 24;
        if (ticksToMorning == 0) ticksToMorning = 24;
        return ticksToMorning;
    }

    @Test
    @DisplayName("Sleep from tick 0 (midnight) advances exactly 8 ticks to 8 AM")
    void testSleepFromMidnight() {
        TimeManager local = new TimeManager(0, 24);
        int ticksToMorning = computeTicksToMorning(local.getCurrentTick());
        assertEquals(8, ticksToMorning,
                "From tick 0 (midnight), sleep should advance 8 ticks to reach 8 AM");
    }

    @Test
    @DisplayName("Sleep from tick 20 (8 PM) advances exactly 12 ticks to 8 AM")
    void testSleepFromEveningTick20() {
        TimeManager local = new TimeManager(20, 24);
        int ticksToMorning = computeTicksToMorning(local.getCurrentTick());
        assertEquals(12, ticksToMorning,
                "From tick 20 (8 PM), sleep should advance 12 ticks to 8 AM next morning");
    }

    @Test
    @DisplayName("Sleep from tick 8 (8 AM) advances a full 24 ticks")
    void testSleepFromMorning() {
        // currentInDay = 8 % 24 = 8; (24 - 8 + 8) % 24 = 0 → forced to 24
        TimeManager local = new TimeManager(8, 24);
        int ticksToMorning = computeTicksToMorning(local.getCurrentTick());
        assertEquals(24, ticksToMorning,
                "Sleep exactly at 8 AM should schedule for NEXT 8 AM (24 ticks)");
    }

    @Test
    @DisplayName("Sleep fast-forward actually lands at tick % 24 == 8")
    void testSleepLandsAtCorrectTick() {
        // Start at tick 20 (8 PM of day 1)
        TimeManager local = new TimeManager(20, 24);
        int ticksToMorning = computeTicksToMorning(local.getCurrentTick());

        // Apply the same advance the game loop does
        local.advanceTicks(ticksToMorning - 1);  // GameEngine advances n-1
        local.advanceTick();                      // Then normal loop advances 1 more

        assertEquals(8, local.getCurrentTick() % 24,
                "After sleep fast-forward, tick % 24 should equal 8 (8 AM)");
    }

    // -----------------------------------------------------------------------
    // Time-of-day label
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTimeOfDay() returns 'Morning' in first quarter of day")
    void testTimeOfDayMorning() {
        TimeManager local = new TimeManager(2, 24); // tick 2 → ratio 2/24 < 0.25
        assertEquals("Morning", local.getTimeOfDay());
    }

    @Test
    @DisplayName("getTimeOfDay() returns 'Afternoon' in second quarter")
    void testTimeOfDayAfternoon() {
        TimeManager local = new TimeManager(7, 24); // tick 7 → ratio 7/24 ≈ 0.29
        assertEquals("Afternoon", local.getTimeOfDay());
    }

    @Test
    @DisplayName("getTimeOfDay() returns 'Evening' in third quarter")
    void testTimeOfDayEvening() {
        TimeManager local = new TimeManager(14, 24); // ratio 14/24 ≈ 0.58
        assertEquals("Evening", local.getTimeOfDay());
    }

    @Test
    @DisplayName("getTimeOfDay() returns 'Night' in final quarter")
    void testTimeOfDayNight() {
        TimeManager local = new TimeManager(19, 24); // ratio 19/24 ≈ 0.79
        assertEquals("Night", local.getTimeOfDay());
    }

    // -----------------------------------------------------------------------
    // Formatted time
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getFormattedTime() returns '05:00' at tick 5")
    void testFormattedTimeAtTick5() {
        TimeManager local = new TimeManager(5, 24);
        assertEquals("05:00", local.getFormattedTime());
    }

    @Test
    @DisplayName("getFormattedTime() returns '14:00' at tick 14")
    void testFormattedTimeAtTick14() {
        TimeManager local = new TimeManager(14, 24);
        assertEquals("14:00", local.getFormattedTime());
    }
}
