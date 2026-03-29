package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A central logger that domain objects can publish messages to.
 *
 * <p>Instead of printing directly to stdout, commands and managers call
 * {@link #log(String)} to buffer messages. The buffer is flushed by
 * {@link #flushAndPrint()} at controlled points in the game loop, ensuring
 * that all accumulated messages appear in the correct order relative to
 * the HUD and prompts.</p>
 *
 * <p>This class also supports an optional "animation" Sim — set via
 * {@link #logAnimation(Sim)} — which triggers a visual action animation
 * when the buffer is flushed.</p>
 *
 * <p>Converted from static utility to instance class so that state is
 * scoped per game session, making it testable and eliminating the need
 * for a {@code reset()} call between sessions.</p>
 */
public class SimulationLogger {

    /**
     * Global reference to the active session's logger.
     * Set by {@link GameEngine} on construction; used by non-engine classes
     * (entities, needs, interactables) that haven't been fully migrated to
     * injected dependencies yet.
     *
     * <p>If no instance has been set, a default no-op-safe logger is
     * returned so that unit tests and ad-hoc usage don't NPE.</p>
     */
    private static SimulationLogger instance;

    private final List<String> logs = new ArrayList<>();
    private Sim simToAnimate = null;

    /**
     * Returns the global logger instance for the current game session.
     *
     * <p>If no instance has been explicitly set (e.g. in unit tests),
     * a lazy-initialized default instance is created and registered.
     * This prevents {@code NullPointerException}s in test scenarios
     * where the full engine is not running.</p>
     *
     * @return the active {@code SimulationLogger}, never {@code null}.
     */
    public static SimulationLogger getInstance() {
        if (instance == null) {
            instance = new SimulationLogger();
        }
        return instance;
    }

    /**
     * Sets the global logger instance. Called once by {@link GameEngine}
     * when a new game session starts.
     *
     * @param logger the logger to use globally.
     */
    public static void setInstance(SimulationLogger logger) {
        instance = logger;
    }

    /**
     * Buffers a plain message for later display.
     *
     * @param message the text to log.
     */
    public void log(String message) {
        logs.add(message);
    }

    /**
     * Buffers a warning message (prefixed with {@code [WARNING]}).
     *
     * @param message the warning text to log.
     */
    public void logWarning(String message) {
        logs.add("[WARNING] " + message);
    }

    /**
     * Registers a Sim for an action animation on the next flush.
     *
     * @param player the Sim whose action should be animated.
     */
    public void logAnimation(Sim player) {
        simToAnimate = player;
    }

    /**
     * Flushes all buffered messages, then prints a prompt without a trailing newline.
     *
     * @param message the prompt text (e.g. {@code "Press ENTER..."}).
     */
    public void prompt(String message) {
        flushAndPrint();
        System.out.print(message);
    }

    /**
     * Flushes all buffered log messages to stdout and triggers any pending
     * action animation. Clears the buffer afterwards.
     */
    public void flushAndPrint() {
        for(String s : logs) {
            System.out.println(s);
        }
        logs.clear();
        if (simToAnimate != null) {
            UIManager.displayActionAnimation(simToAnimate);
            simToAnimate = null;
        }
    }

    /**
     * Clears all buffered logs without printing them.
     * Must be called when transitioning between game sessions.
     */
    public void reset() {
        logs.clear();
        simToAnimate = null;
    }

}
