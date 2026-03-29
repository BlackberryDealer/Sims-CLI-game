package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.engine.commands.*;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link InputHandler} — command routing, invalid input handling,
 * SimulationException mapping, and SleepEventException mapping.
 *
 * <p>Coverage:
 * <ul>
 *     <li>"S" input returns SAVE_AND_EXIT without executing a command</li>
 *     <li>Invalid (non-numeric) input returns NO_TICK</li>
 *     <li>Out-of-range numeric input returns NO_TICK</li>
 *     <li>"W" input routes to WorkCommand (verified by side effects)</li>
 *     <li>"J" input routes to JobMarketCommand</li>
 *     <li>"K" input routes to SwitchSimCommand</li>
 *     <li>"I" input routes to CharacterStatusCommand</li>
 * </ul>
 */
@DisplayName("InputHandler — Command Routing & Error Handling")
public class InputHandlerTest {

    private SimulationLogger logger;
    private IWorldManager worldManager;
    private TimeManager timeManager;
    private List<Sim> neighborhood;
    private Sim activePlayer;
    private InputHandler handler;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);

        activePlayer = new Sim("Alice", 25, Gender.FEMALE, Job.SOFTWARE_ENGINEER);
        neighborhood = new ArrayList<>(Collections.singletonList(activePlayer));
        timeManager = new TimeManager(1, 24);

        NPCManager npcMgr = new NPCManager();
        worldManager = new WorldManager(npcMgr, neighborhood);
        worldManager.setupWorld();

        handler = new InputHandler(worldManager, timeManager, neighborhood,
                sim -> { /* no-op callback */ }, logger);
    }

    /** Creates a Scanner from a fake input string. */
    private Scanner mockScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    // -----------------------------------------------------------------------
    // Save and Exit
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'S' input returns SAVE_AND_EXIT immediately")
    void testSaveAndExit() {
        CommandResult result = handler.handle("S", activePlayer, mockScanner("\n"));
        assertEquals(CommandResult.SAVE_AND_EXIT, result,
                "'S' should return SAVE_AND_EXIT without executing a command");
    }

    // -----------------------------------------------------------------------
    // Invalid input handling
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Invalid Input Handling")
    class InvalidInputTests {

        @Test
        @DisplayName("Non-numeric gibberish returns NO_TICK")
        void testGibberishInput() {
            CommandResult result = handler.handle("XYZ", activePlayer, mockScanner("\n"));
            assertEquals(CommandResult.NO_TICK, result,
                    "Invalid non-numeric input should return NO_TICK");
        }

        @Test
        @DisplayName("Empty string input returns NO_TICK")
        void testEmptyInput() {
            CommandResult result = handler.handle("", activePlayer, mockScanner("\n"));
            assertEquals(CommandResult.NO_TICK, result,
                    "Empty input should return NO_TICK");
        }

        @Test
        @DisplayName("Out-of-range numeric input (e.g., '99') returns NO_TICK")
        void testOutOfRangeInput() {
            // Numeric input goes to InteractCommand which will throw
            // IndexOutOfBoundsException → caught as SimulationException or similar
            CommandResult result = handler.handle("99", activePlayer, mockScanner("\n"));
            assertEquals(CommandResult.NO_TICK, result,
                    "Out-of-range numeric input should return NO_TICK");
        }
    }

    // -----------------------------------------------------------------------
    // Work command routing
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'W' on a weekday routes to WorkCommand and returns TICK_FORWARD")
    void testWorkCommandRouting() {
        // Day 1 is Monday (weekday)
        CommandResult result = handler.handle("W", activePlayer, mockScanner("\n"));
        assertEquals(CommandResult.TICK_FORWARD, result,
                "'W' on a weekday should route to WorkCommand and return TICK_FORWARD");
        assertEquals(1, activePlayer.getShiftsWorkedToday(),
                "Sim should have worked 1 shift");
    }

    // -----------------------------------------------------------------------
    // Character status command (no-tick info display)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'I' routes to CharacterStatusCommand and returns NO_TICK")
    void testCharacterStatusCommandRouting() {
        CommandResult result = handler.handle("I", activePlayer, mockScanner("\n"));
        assertEquals(CommandResult.NO_TICK, result,
                "'I' should route to CharacterStatusCommand and return NO_TICK");
    }

    // -----------------------------------------------------------------------
    // Switch Sim command
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'K' with single-Sim household shows 'no one to switch to' and returns NO_TICK")
    void testSwitchSimSingleHousehold() {
        CommandResult result = handler.handle("K", activePlayer, mockScanner("\n"));
        assertEquals(CommandResult.NO_TICK, result,
                "'K' with only 1 Sim should return NO_TICK (no one to switch to)");
    }

    // -----------------------------------------------------------------------
    // Job Market command
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'J' routes to JobMarketCommand and returns NO_TICK on cancel")
    void testJobMarketCommandRouting() {
        // Input "-1" to cancel the job market menu
        CommandResult result = handler.handle("J", activePlayer, mockScanner("-1\n"));
        assertEquals(CommandResult.NO_TICK, result,
                "'J' with cancel input should return NO_TICK");
    }

    // -----------------------------------------------------------------------
    // Travel command
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'T' routes to TravelCommand and returns NO_TICK on cancel")
    void testTravelCommandCancelled() {
        // Input "0" to cancel travel
        CommandResult result = handler.handle("T", activePlayer, mockScanner("0\n"));
        assertEquals(CommandResult.NO_TICK, result,
                "'T' with cancel input should return NO_TICK");
    }

    @Test
    @DisplayName("'T' with valid destination returns TICK_FORWARD")
    void testTravelToValidDestination() {
        // Travel to City Park (index 5 in the 1-based menu)
        CommandResult result = handler.handle("T", activePlayer, mockScanner("5\n"));
        assertEquals(CommandResult.TICK_FORWARD, result,
                "'T' to a valid non-current location should return TICK_FORWARD");
    }

    // -----------------------------------------------------------------------
    // House info command
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("'H' routes to HouseInfoCommand and returns NO_TICK")
    void testHouseInfoRouting() {
        CommandResult result = handler.handle("H", activePlayer, mockScanner("\n"));
        assertEquals(CommandResult.NO_TICK, result,
                "'H' should route to HouseInfoCommand and return NO_TICK");
    }
}
