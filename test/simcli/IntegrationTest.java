package simcli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.*;
import simcli.engine.commands.*;
import simcli.entities.actors.*;
import simcli.entities.models.*;
import simcli.world.Building;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Integration & Edge Case Test Suite.
 * Tests how the engine handles bad inputs and invalid game states.
 */
@DisplayName("E2E & Integration - Negative Testing Suite")
public class IntegrationTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private Sim activePlayer;
    private IWorldManager worldManager;
    private TimeManager timeManager;
    private InputHandler inputHandler;

    @BeforeEach
    public void setUp() {
        // Reroute System.out to capture UIManager prints
        System.setOut(new PrintStream(outputStreamCaptor));

        // Setup base test state
        activePlayer = new Sim("TestDummy", 25, Gender.MALE, Job.UNEMPLOYED);
        
        // Instantiate a functional GameEngine to avoid NullPointerExceptions when Park checks for NPCManager
        java.util.List<Sim> neighborhood = new java.util.ArrayList<>();
        neighborhood.add(activePlayer);
        GameEngine testEngine = new GameEngine("TestIntegrationWorld", 1, neighborhood, true);
        
        worldManager = testEngine.getWorldManager();
        timeManager = new TimeManager(1, 24); // Monday, Tick 1
        SimulationLogger logger = new SimulationLogger();
        inputHandler = new InputHandler(worldManager, timeManager,
                testEngine.getNeighborhood(), testEngine::setActivePlayer, logger);
    }

    @AfterEach
    public void tearDown() {
        // Restore standard output
        System.setOut(standardOut);
    }

    /**
     * Helper to simulate a user typing a command and pressing Enter.
     */
    private CommandResult simulateInput(String input) {
        // Provide the input, plus a blank newline to get past the "Press ENTER to return..." pause() block
        ByteArrayInputStream in = new ByteArrayInputStream((input + "\n\n").getBytes());
        Scanner mockScanner = new Scanner(in);
        return inputHandler.handle(input, activePlayer, mockScanner);
    }

    /** Helper to build a CommandContext for direct command tests. */
    private CommandContext buildCtx(Sim sim, Scanner scanner) {
        return new CommandContext.Builder()
                .activePlayer(sim)
                .neighborhood(java.util.Collections.singletonList(sim))
                .scanner(scanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(worldManager.getCurrentLocation())
                .logger(new SimulationLogger())
                .build();
    }


    @Test
    @DisplayName("Edge Case: Try to Move Rooms inside a Supermarket (Non-Residential)")
    void testMoveRoomInCommercialLocation() {
        Building supermarket = worldManager.getCityMap().get(3); // Index 3 is Supermarket
        worldManager.setCurrentLocation(supermarket);
        
        CommandResult result = simulateInput("M");

        assertEquals(CommandResult.NO_TICK, result, "Should reject the action and not cost a tick.");
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("You can only move between rooms at home!"), 
            "Should print the correct boundary error message.");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"HELLO", "-1", "99", "", "null", "DROP DATABASE;"})
    @DisplayName("Edge Case: Menu Bounds - Type invalid string characters and out of bounds")
    void testCommandMenuRejectsInvalidStringBugs(String badInput) {
        CommandResult result = simulateInput(badInput);
        assertEquals(CommandResult.NO_TICK, result);
        String log = outputStreamCaptor.toString();
        assertTrue(log.contains("Invalid input.") || log.contains("Invalid item choice") || log.contains("NumberFormatException"), 
            "Should cleanly reject bad formats or array index exceptions.");
    }

    @Test
    @DisplayName("Edge Case: Upgrade Room with $0 (Financial Boundary)")
    void testUpgradeRoomWithoutMoney() throws Exception {
        activePlayer.setMoney(0);
        
        Building home = worldManager.getCityMap().get(0); // Dorm
        worldManager.setCurrentLocation(home);

        // Simulate choosing Room [1]
        ByteArrayInputStream in = new ByteArrayInputStream("1\n\n".getBytes());
        Scanner mockScanner = new Scanner(in);
        
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(activePlayer)
                .neighborhood(java.util.Collections.singletonList(activePlayer))
                .scanner(mockScanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(home)
                .logger(new SimulationLogger())
                .build();
        UpgradeRoomCommand command = new UpgradeRoomCommand(ctx);
        
        CommandResult result = command.execute();

        assertEquals(CommandResult.NO_TICK, result);
        assertTrue(outputStreamCaptor.toString().contains("Not enough money!"), 
            "Should reject the upgrade due to having $0.");
    }

    @Test
    @DisplayName("Edge Case: Cancel an Overwork shift (State Validation)")
    void testCancelOverworkMaintainsState() {
        activePlayer.getCareerManager().changeJob(Job.SOFTWARE_ENGINEER, activePlayer.getName());
        activePlayer.incrementShiftsWorkedToday(); // Sim has already worked once today
        int initialEnergy = activePlayer.getEnergy().getValue();

        // Simulate pressing 'W', and then 'N' to the "Are you sure?" prompt
        ByteArrayInputStream in = new ByteArrayInputStream("N\n\n".getBytes());
        Scanner mockScanner = new Scanner(in);
        
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(activePlayer)
                .neighborhood(java.util.Collections.singletonList(activePlayer))
                .scanner(mockScanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(worldManager.getCurrentLocation())
                .logger(new SimulationLogger())
                .build();
        WorkCommand command = new WorkCommand(ctx);
        
        try {
            CommandResult result = command.execute();
            assertEquals(CommandResult.NO_TICK, result, "Cancelled work should not advance time.");
            assertEquals(initialEnergy, activePlayer.getEnergy().getValue(), "Energy should not drain if cancelled.");
            assertTrue(outputStreamCaptor.toString().contains("Work action cancelled."), "Should print cancellation message.");
        } catch (SimulationException e) {
            fail("Should not throw exception.");
        }
    }

    @Test
    @DisplayName("Edge Case: Apply for a job you are too young for (Age Boundary)")
    void testUnderageJobApplication() throws Exception {
        Sim teen = new Sim("Timmy", 15, Gender.MALE, Job.UNEMPLOYED);
        
        // Job Option 3 is usually SOFTWARE_ENGINEER (req 21+)
        ByteArrayInputStream in = new ByteArrayInputStream("3\n\n".getBytes());
        Scanner mockScanner = new Scanner(in);
        
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(teen)
                .neighborhood(java.util.Collections.singletonList(teen))
                .scanner(mockScanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(worldManager.getCurrentLocation())
                .logger(new SimulationLogger())
                .build();
        JobMarketCommand jobMarket = new JobMarketCommand(ctx);
        CommandResult result = jobMarket.execute();

        assertEquals(CommandResult.NO_TICK, result);
        assertEquals(Job.UNEMPLOYED, teen.getCareer(), "Teen should remain unemployed.");
        assertTrue(outputStreamCaptor.toString().contains("Children and Teens cannot access the professional job market."), 
            "Should reject due to Age Boundary.");
    }

    @Test
    @DisplayName("Edge Case: Travel to the exact location you are already standing in")
    void testTravelToCurrentLocation() throws Exception {
        Building home = worldManager.getCityMap().get(0); // Dorm
        worldManager.setCurrentLocation(home);

        // Simulate Travel 'T', then picking the Dorm '1'
        ByteArrayInputStream in = new ByteArrayInputStream("1\n\n".getBytes());
        Scanner mockScanner = new Scanner(in);
        
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(activePlayer)
                .neighborhood(java.util.Collections.singletonList(activePlayer))
                .scanner(mockScanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(home)
                .logger(new SimulationLogger())
                .build();
        TravelCommand command = new TravelCommand(ctx);
        
        CommandResult result = command.execute();

        assertEquals(CommandResult.NO_TICK, result);
        assertTrue(outputStreamCaptor.toString().contains("You are already at The Shared Dorm!"), 
            "Should gracefully prevent teleporting to the same spot.");
    }

    @Test
    @DisplayName("Edge Case: Check House Info in a Commercial Location")
    void testHouseInfoInCommercialLocation() {
        Building supermarket = worldManager.getCityMap().get(3); // Supermarket
        worldManager.setCurrentLocation(supermarket);

        CommandResult result = simulateInput("H");

        assertEquals(CommandResult.NO_TICK, result);
        assertTrue(outputStreamCaptor.toString().contains("You can only inspect residential buildings."), 
            "Should cleanly reject checking house info in public locations.");
    }

}
