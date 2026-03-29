package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GameEngine} — constructor variants, active player
 * management, and getter correctness.
 *
 * <p>Coverage:
 * <ul>
 *     <li>New-game constructor initializes with correct defaults</li>
 *     <li>Load-game constructor restores tick and game-over state</li>
 *     <li>Active player defaults to the first Sim</li>
 *     <li>setActivePlayer() switches control</li>
 *     <li>Load with all-dead neighborhood selects index 0</li>
 *     <li>Getters return correct values</li>
 *     <li>WorldManager is initialized and has expected buildings</li>
 * </ul>
 */
@DisplayName("GameEngine — Construction & State Management")
public class GameEngineTest {

    private SimulationLogger logger;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
    }

    // -----------------------------------------------------------------------
    // New-game constructors
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("New Game Construction")
    class NewGameTests {

        @Test
        @DisplayName("New game sets worldName correctly")
        void testNewGameWorldName() {
            Sim sim = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("TestWorld", Arrays.asList(sim));

            assertEquals("TestWorld", engine.getWorldName());
        }

        @Test
        @DisplayName("New game starts at tick 1")
        void testNewGameStartsAtTickOne() {
            Sim sim = new Sim("Bob", 25, Gender.MALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("World1", Arrays.asList(sim));

            assertEquals(1, engine.getCurrentTick(), "New game should start at tick 1");
        }

        @Test
        @DisplayName("New game has isGameOver = false")
        void testNewGameNotOver() {
            Sim sim = new Sim("Charlie", 25, Gender.MALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("World2", Arrays.asList(sim));

            assertFalse(engine.isGameOver(), "New game should not be game over");
        }

        @Test
        @DisplayName("New game active player is the first Sim")
        void testNewGameActivePlayer() {
            Sim alice = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
            Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("World3", Arrays.asList(alice, bob));

            assertSame(alice, engine.getActivePlayer(),
                    "Active player should be the first Sim in the neighborhood");
        }

        @Test
        @DisplayName("New game preserves the full neighborhood list")
        void testNewGameNeighborhood() {
            Sim alice = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
            Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
            List<Sim> sims = Arrays.asList(alice, bob);
            GameEngine engine = new GameEngine("World4", sims);

            assertEquals(2, engine.getNeighborhood().size());
            assertSame(alice, engine.getNeighborhood().get(0));
            assertSame(bob, engine.getNeighborhood().get(1));
        }
    }

    // -----------------------------------------------------------------------
    // Loaded-game constructors
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Load Game Construction")
    class LoadGameTests {

        @Test
        @DisplayName("Loaded game restores the correct tick")
        void testLoadedGameTick() {
            Sim sim = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("SavedWorld", 42, Arrays.asList(sim), false);

            assertEquals(42, engine.getCurrentTick(), "Loaded game should restore tick 42");
        }

        @Test
        @DisplayName("Loaded game-over state is preserved")
        void testLoadedGameOverState() {
            Sim sim = new Sim("Bob", 25, Gender.MALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("OverWorld", 100, Arrays.asList(sim), true);

            assertTrue(engine.isGameOver(), "Loaded game-over should be true");
        }

        @Test
        @DisplayName("Loaded game selects first alive Sim as active player")
        void testLoadedGameActivePlayerFirstAlive() {
            Sim dead = new Sim("Dead", 25, Gender.MALE, Job.UNEMPLOYED);
            dead.markAsDead();
            Sim alive = new Sim("Alive", 30, Gender.FEMALE, Job.UNEMPLOYED);

            GameEngine engine = new GameEngine("World", 10,
                    Arrays.asList(dead, alive), false);

            assertSame(alive, engine.getActivePlayer(),
                    "Active player should be the first alive Sim, skipping dead ones");
        }

        @Test
        @DisplayName("Loaded game with all dead Sims defaults to index 0")
        void testLoadedGameAllDead() {
            Sim dead1 = new Sim("Dead1", 25, Gender.MALE, Job.UNEMPLOYED);
            dead1.markAsDead();
            Sim dead2 = new Sim("Dead2", 30, Gender.FEMALE, Job.UNEMPLOYED);
            dead2.markAsDead();

            GameEngine engine = new GameEngine("DeadWorld", 50,
                    Arrays.asList(dead1, dead2), true);

            assertSame(dead1, engine.getActivePlayer(),
                    "When all Sims are dead, active player should default to index 0");
        }
    }

    // -----------------------------------------------------------------------
    // Active player management
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("setActivePlayer() changes the active player")
    void testSetActivePlayer() {
        Sim alice = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
        Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        GameEngine engine = new GameEngine("World", Arrays.asList(alice, bob));

        engine.setActivePlayer(bob);
        assertSame(bob, engine.getActivePlayer(),
                "setActivePlayer should switch control to Bob");
    }

    // -----------------------------------------------------------------------
    // SubSystem Initialization
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Subsystem Initialization")
    class SubsystemTests {

        @Test
        @DisplayName("WorldManager is initialized with expected buildings")
        void testWorldManagerInitialized() {
            Sim sim = new Sim("Test", 25, Gender.MALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("World", Arrays.asList(sim));

            assertNotNull(engine.getWorldManager(), "WorldManager should be initialized");
            assertEquals(5, engine.getWorldManager().getCityMap().size(),
                    "City map should have 5 buildings");
        }

        @Test
        @DisplayName("LifecycleManager is initialized")
        void testLifecycleManagerInitialized() {
            Sim sim = new Sim("Test", 25, Gender.FEMALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("World", Arrays.asList(sim));

            assertNotNull(engine.getLifecycleManager(),
                    "LifecycleManager should be initialized");
        }

        @Test
        @DisplayName("NpcManager is initialized")
        void testNpcManagerInitialized() {
            Sim sim = new Sim("Test", 25, Gender.MALE, Job.UNEMPLOYED);
            GameEngine engine = new GameEngine("World", Arrays.asList(sim));

            assertNotNull(engine.getNpcManager(),
                    "NPCManager should be initialized");
        }
    }
}
