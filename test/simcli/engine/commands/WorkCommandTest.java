package simcli.engine.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.engine.TimeManager;
import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Command Pattern - WorkCommand & Weekend Checks")
public class WorkCommandTest {

    private Sim adultSim;

    @BeforeEach
    void setUp() {
        adultSim = new Sim("Bob", 25, Gender.MALE, Job.SOFTWARE_ENGINEER);
    }

    /** Helper to build a CommandContext for WorkCommand tests. */
    private CommandContext buildCtx(Sim sim, Scanner scanner, TimeManager tm) {
        return new CommandContext.Builder()
                .activePlayer(sim)
                .scanner(scanner)
                .timeManager(tm)
                .build();
    }

    @Test
    @DisplayName("Working on Monday advances ticks")
    void testWorkOnMonday() throws Exception {
        // Day 1 is Monday.
        TimeManager tm = new TimeManager(1, 24);
        Scanner scanner = new Scanner(System.in);
        WorkCommand command = new WorkCommand(buildCtx(adultSim, scanner, tm));

        assertEquals("Monday", tm.getDayOfWeek());

        CommandResult result = command.execute();

        assertEquals(CommandResult.TICK_FORWARD, result, "Should successfully work on Monday and return TICK_FORWARD");
        assertEquals(1, adultSim.getShiftsWorkedToday(), "Shifts worked should increase");
    }

    @Test
    @DisplayName("Working on Saturday is blocked and returns NO_TICK")
    void testWorkOnSaturdayBlocked() throws Exception {
        // Day 6 is Saturday. Day 6 * 24 ticks/day = tick 121 (start of day 6)
        TimeManager tm = new TimeManager(121, 24); 
        
        // Provide "enter" as mock user input for the pause() prompt
        ByteArrayInputStream in = new ByteArrayInputStream("\n".getBytes());
        Scanner mockScanner = new Scanner(in);

        WorkCommand command = new WorkCommand(buildCtx(adultSim, mockScanner, tm));

        assertEquals("Saturday", tm.getDayOfWeek());

        CommandResult result = command.execute();

        assertEquals(CommandResult.NO_TICK, result, "Working on Saturday should return NO_TICK");
        assertEquals(0, adultSim.getShiftsWorkedToday(), "Shifts worked should NOT increase on weekends");
    }

    @Test
    @DisplayName("Working on Sunday is blocked and returns NO_TICK")
    void testWorkOnSundayBlocked() throws Exception {
        // Day 7 is Sunday. 6 * 24 + 1 = 145
        TimeManager tm = new TimeManager(145, 24); 
        
        ByteArrayInputStream in = new ByteArrayInputStream("\n".getBytes());
        Scanner mockScanner = new Scanner(in);

        WorkCommand command = new WorkCommand(buildCtx(adultSim, mockScanner, tm));

        assertEquals("Sunday", tm.getDayOfWeek());

        CommandResult result = command.execute();

        assertEquals(CommandResult.NO_TICK, result, "Working on Sunday should return NO_TICK");
        assertEquals(0, adultSim.getShiftsWorkedToday(), "Shifts worked should NOT increase on weekends");
    }
}
