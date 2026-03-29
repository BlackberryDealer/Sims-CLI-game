package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.engine.commands.CommandContext;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.world.Building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CommandContext} and {@link CommandContext.Builder} —
 * builder validation, getters, and the switchActivePlayer callback.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Builder creates a valid context when all required fields are set</li>
 *     <li>Builder throws NullPointerException for each missing required field</li>
 *     <li>Getters return the values that were provided to the builder</li>
 *     <li>switchActivePlayer() delegates to the Consumer callback</li>
 *     <li>switchActivePlayer() is safe when no callback is set</li>
 * </ul>
 */
@DisplayName("CommandContext — Builder Pattern & Context Access")
public class CommandContextTest {

    private SimulationLogger logger;
    private Sim activePlayer;
    private List<Sim> neighborhood;
    private Scanner scanner;
    private TimeManager timeManager;
    private IWorldManager worldManager;
    private Building currentLocation;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);

        activePlayer = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
        neighborhood = new ArrayList<>(Collections.singletonList(activePlayer));
        scanner = new Scanner(System.in);
        timeManager = new TimeManager(1, 24);

        NPCManager npcMgr = new NPCManager();
        worldManager = new WorldManager(npcMgr, neighborhood);
        worldManager.setupWorld();
        currentLocation = worldManager.getCurrentLocation();
    }

    // -----------------------------------------------------------------------
    // Successful build
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Builder creates a valid CommandContext with all required fields")
    void testBuilderCreatesValidContext() {
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(activePlayer)
                .neighborhood(neighborhood)
                .scanner(scanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(currentLocation)
                .logger(logger)
                .build();

        assertNotNull(ctx, "CommandContext should not be null when all fields are set");
    }

    // -----------------------------------------------------------------------
    // Builder validation — missing required fields
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Builder Validation")
    class BuilderValidationTests {

        @Test
        @DisplayName("build() throws NullPointerException if activePlayer is null")
        void testMissingActivePlayer() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .neighborhood(neighborhood)
                            .scanner(scanner)
                            .timeManager(timeManager)
                            .worldManager(worldManager)
                            .currentLocation(currentLocation)
                            .logger(logger)
                            .build());
        }

        @Test
        @DisplayName("build() throws NullPointerException if neighborhood is null")
        void testMissingNeighborhood() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .activePlayer(activePlayer)
                            .scanner(scanner)
                            .timeManager(timeManager)
                            .worldManager(worldManager)
                            .currentLocation(currentLocation)
                            .logger(logger)
                            .build());
        }

        @Test
        @DisplayName("build() throws NullPointerException if scanner is null")
        void testMissingScanner() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .activePlayer(activePlayer)
                            .neighborhood(neighborhood)
                            .timeManager(timeManager)
                            .worldManager(worldManager)
                            .currentLocation(currentLocation)
                            .logger(logger)
                            .build());
        }

        @Test
        @DisplayName("build() throws NullPointerException if timeManager is null")
        void testMissingTimeManager() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .activePlayer(activePlayer)
                            .neighborhood(neighborhood)
                            .scanner(scanner)
                            .worldManager(worldManager)
                            .currentLocation(currentLocation)
                            .logger(logger)
                            .build());
        }

        @Test
        @DisplayName("build() throws NullPointerException if worldManager is null")
        void testMissingWorldManager() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .activePlayer(activePlayer)
                            .neighborhood(neighborhood)
                            .scanner(scanner)
                            .timeManager(timeManager)
                            .currentLocation(currentLocation)
                            .logger(logger)
                            .build());
        }

        @Test
        @DisplayName("build() throws NullPointerException if currentLocation is null")
        void testMissingCurrentLocation() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .activePlayer(activePlayer)
                            .neighborhood(neighborhood)
                            .scanner(scanner)
                            .timeManager(timeManager)
                            .worldManager(worldManager)
                            .logger(logger)
                            .build());
        }

        @Test
        @DisplayName("build() throws NullPointerException if logger is null")
        void testMissingLogger() {
            assertThrows(NullPointerException.class, () ->
                    new CommandContext.Builder()
                            .activePlayer(activePlayer)
                            .neighborhood(neighborhood)
                            .scanner(scanner)
                            .timeManager(timeManager)
                            .worldManager(worldManager)
                            .currentLocation(currentLocation)
                            .build());
        }
    }

    // -----------------------------------------------------------------------
    // Getter correctness
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Context Getters")
    class GetterTests {

        private CommandContext ctx;

        @BeforeEach
        void buildContext() {
            ctx = new CommandContext.Builder()
                    .activePlayer(activePlayer)
                    .neighborhood(neighborhood)
                    .scanner(scanner)
                    .timeManager(timeManager)
                    .worldManager(worldManager)
                    .currentLocation(currentLocation)
                    .logger(logger)
                    .build();
        }

        @Test
        @DisplayName("getActivePlayer() returns the Sim set via builder")
        void testGetActivePlayer() {
            assertSame(activePlayer, ctx.getActivePlayer());
        }

        @Test
        @DisplayName("getNeighborhood() returns the list set via builder")
        void testGetNeighborhood() {
            assertSame(neighborhood, ctx.getNeighborhood());
        }

        @Test
        @DisplayName("getScanner() returns the Scanner set via builder")
        void testGetScanner() {
            assertSame(scanner, ctx.getScanner());
        }

        @Test
        @DisplayName("getTimeManager() returns the TimeManager set via builder")
        void testGetTimeManager() {
            assertSame(timeManager, ctx.getTimeManager());
        }

        @Test
        @DisplayName("getWorldManager() returns the IWorldManager set via builder")
        void testGetWorldManager() {
            assertSame(worldManager, ctx.getWorldManager());
        }

        @Test
        @DisplayName("getCurrentLocation() returns the Building set via builder")
        void testGetCurrentLocation() {
            assertSame(currentLocation, ctx.getCurrentLocation());
        }

        @Test
        @DisplayName("getLogger() returns the SimulationLogger set via builder")
        void testGetLogger() {
            assertSame(logger, ctx.getLogger());
        }
    }

    // -----------------------------------------------------------------------
    // switchActivePlayer callback
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Active Player Callback")
    class CallbackTests {

        @Test
        @DisplayName("switchActivePlayer() delegates to the Consumer callback")
        void testSwitchActivePlayerCallback() {
            AtomicReference<Sim> captured = new AtomicReference<>();

            CommandContext ctx = new CommandContext.Builder()
                    .activePlayer(activePlayer)
                    .neighborhood(neighborhood)
                    .scanner(scanner)
                    .timeManager(timeManager)
                    .worldManager(worldManager)
                    .currentLocation(currentLocation)
                    .setActivePlayer(captured::set)
                    .logger(logger)
                    .build();

            Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
            ctx.switchActivePlayer(bob);

            assertSame(bob, captured.get(),
                    "switchActivePlayer() should invoke the Consumer with the given Sim");
        }

        @Test
        @DisplayName("switchActivePlayer() is safe when no callback was set")
        void testSwitchActivePlayerNoCallback() {
            CommandContext ctx = new CommandContext.Builder()
                    .activePlayer(activePlayer)
                    .neighborhood(neighborhood)
                    .scanner(scanner)
                    .timeManager(timeManager)
                    .worldManager(worldManager)
                    .currentLocation(currentLocation)
                    .logger(logger)
                    .build();

            assertDoesNotThrow(() -> ctx.switchActivePlayer(activePlayer),
                    "switchActivePlayer() should not throw when no callback is set");
        }
    }
}
