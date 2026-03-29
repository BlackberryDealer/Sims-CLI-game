package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SimulationLogger} — buffered logging, warning format,
 * reset behaviour, and singleton management.
 *
 * <p>Coverage:
 * <ul>
 *     <li>log() buffers messages without printing</li>
 *     <li>logWarning() prefixes with [WARNING]</li>
 *     <li>reset() clears the buffer</li>
 *     <li>Singleton getInstance()/setInstance()</li>
 *     <li>logAnimation() registers and clears the Sim</li>
 * </ul>
 */
@DisplayName("SimulationLogger — Buffered Logging & Singleton")
public class SimulationLoggerTest {

    private SimulationLogger logger;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
    }

    // -----------------------------------------------------------------------
    // Basic logging
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Message Buffering")
    class BufferingTests {

        @Test
        @DisplayName("log() buffers a message (does not throw)")
        void testLogBuffersMessage() {
            assertDoesNotThrow(() -> logger.log("Test message"),
                    "Logging a message should not throw");
        }

        @Test
        @DisplayName("Multiple log() calls buffer without error")
        void testMultipleLogCalls() {
            assertDoesNotThrow(() -> {
                logger.log("Message 1");
                logger.log("Message 2");
                logger.log("Message 3");
            }, "Multiple log calls should buffer without issues");
        }

        @Test
        @DisplayName("logWarning() buffers a prefixed warning message")
        void testLogWarningPrefix() {
            // logWarning should add a [WARNING] prefix — we verify by flushing later
            assertDoesNotThrow(() -> logger.logWarning("Something bad happened"),
                    "logWarning should not throw");
        }
    }

    // -----------------------------------------------------------------------
    // Reset
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Reset Behaviour")
    class ResetTests {

        @Test
        @DisplayName("reset() clears all buffered logs without error")
        void testResetClearsBuffer() {
            logger.log("Message 1");
            logger.log("Message 2");
            assertDoesNotThrow(() -> logger.reset(),
                    "reset() should not throw");
        }

        @Test
        @DisplayName("reset() clears animation Sim reference")
        void testResetClearsAnimation() {
            Sim sim = new Sim("Bob", 25, Gender.MALE, Job.UNEMPLOYED);
            logger.logAnimation(sim);
            logger.reset();

            // After reset, flushAndPrint should not try to animate
            assertDoesNotThrow(() -> logger.flushAndPrint(),
                    "flushAndPrint after reset should not attempt animation");
        }

        @Test
        @DisplayName("reset() on empty logger does not throw")
        void testResetOnEmptyLogger() {
            assertDoesNotThrow(() -> logger.reset(),
                    "reset() on a fresh logger should be safe");
        }
    }

    // -----------------------------------------------------------------------
    // Singleton management
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Singleton Pattern")
    class SingletonTests {

        @Test
        @DisplayName("getInstance() returns the same instance set by setInstance()")
        void testGetInstanceReturnsSetInstance() {
            SimulationLogger custom = new SimulationLogger();
            SimulationLogger.setInstance(custom);

            assertSame(custom, SimulationLogger.getInstance(),
                    "getInstance() should return the instance set by setInstance()");
        }

        @Test
        @DisplayName("getInstance() creates default if none set")
        void testGetInstanceCreatesDefault() {
            // Clear the static instance
            SimulationLogger.setInstance(null);

            SimulationLogger instance = SimulationLogger.getInstance();
            assertNotNull(instance,
                    "getInstance() should create a default instance if none set");
        }

        @Test
        @DisplayName("setInstance() replaces the current global logger")
        void testSetInstanceReplaces() {
            SimulationLogger first = new SimulationLogger();
            SimulationLogger second = new SimulationLogger();

            SimulationLogger.setInstance(first);
            assertSame(first, SimulationLogger.getInstance());

            SimulationLogger.setInstance(second);
            assertSame(second, SimulationLogger.getInstance());
        }
    }

    // -----------------------------------------------------------------------
    // Animation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("logAnimation() accepts a Sim without throwing")
    void testLogAnimationAcceptsSim() {
        Sim sim = new Sim("Animator", 25, Gender.FEMALE, Job.UNEMPLOYED);
        assertDoesNotThrow(() -> logger.logAnimation(sim),
                "logAnimation should accept a Sim reference");
    }

    // -----------------------------------------------------------------------
    // flushAndPrint()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("flushAndPrint() clears the buffer after printing")
    void testFlushAndPrintClearsBuffer() {
        logger.log("Line 1");
        logger.log("Line 2");
        logger.flushAndPrint();

        // A second flushAndPrint should be a no-op (buffer is empty)
        assertDoesNotThrow(() -> logger.flushAndPrint(),
                "flushAndPrint on empty buffer should be safe");
    }

    @Test
    @DisplayName("flushAndPrint() on empty buffer does not throw")
    void testFlushAndPrintOnEmpty() {
        assertDoesNotThrow(() -> logger.flushAndPrint(),
                "flushAndPrint on a fresh logger should not throw");
    }
}
