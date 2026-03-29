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
 */
public class SimulationLogger {
    private static final List<String> logs = new ArrayList<>();
    private static Sim simToAnimate = null;

    /**
     * Buffers a plain message for later display.
     *
     * @param message the text to log.
     */
    public static void log(String message) {
        logs.add(message);
    }

    /**
     * Buffers a warning message (prefixed with {@code [WARNING]}).
     *
     * @param message the warning text to log.
     */
    public static void logWarning(String message) {
        logs.add("[WARNING] " + message);
    }

    /**
     * Registers a Sim for an action animation on the next flush.
     *
     * @param player the Sim whose action should be animated.
     */
    public static void logAnimation(Sim player) {
        simToAnimate = player;
    }

    /**
     * Flushes all buffered messages, then prints a prompt without a trailing newline.
     *
     * @param message the prompt text (e.g. {@code "Press ENTER..."}).
     */
    public static void prompt(String message) {
        flushAndPrint();
        System.out.print(message);
    }

    /**
     * Flushes all buffered log messages to stdout and triggers any pending
     * action animation. Clears the buffer afterwards.
     */
    public static void flushAndPrint() {
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
    public static void reset() {
        logs.clear();
        simToAnimate = null;
    }

}
