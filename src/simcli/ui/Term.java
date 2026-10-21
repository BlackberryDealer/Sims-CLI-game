package simcli.ui;

import java.io.Console;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Terminal capabilities and display settings.
 *
 * <p>Detects, once at start-up, whether the game is attached to an
 * interactive terminal, whether that terminal understands ANSI colour
 * codes, and which characters the console encoding can print. Every
 * fancy glyph is requested through {@link #glyph(String, String)} so the
 * UI degrades gracefully to plain ASCII on limited consoles.</p>
 *
 * <p>Colours and animations can also be toggled by the player from the
 * Settings menu, or forced with the {@code SIMCLI_COLOR=always|never}
 * environment variable ({@code NO_COLOR} is honoured too).</p>
 */
public final class Term {

    /** Width of every panel the UI draws; fits an 80-column terminal. */
    public static final int WIDTH = 78;

    private static final boolean INTERACTIVE = detectInteractive();
    private static final CharsetEncoder ENCODER = detectEncoder();

    private static boolean colorEnabled = detectColor();
    private static boolean animationsEnabled = INTERACTIVE;

    private Term() {
    }

    // -------------------------------------------------------------------------
    // Capabilities
    // -------------------------------------------------------------------------

    /** Returns {@code true} when a human is at a real terminal (not piped/tests). */
    public static boolean isInteractive() {
        return INTERACTIVE;
    }

    /** Returns whether ANSI colour codes are currently being emitted. */
    public static boolean isColorEnabled() {
        return colorEnabled;
    }

    /** Turns ANSI colours on or off for the rest of the session. */
    public static void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
    }

    /** Returns whether timed animations play. Always off when not interactive. */
    public static boolean isAnimationsEnabled() {
        return animationsEnabled && INTERACTIVE;
    }

    /** Turns timed animations on or off for the rest of the session. */
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    /**
     * Returns {@code unicode} if the console can print it, otherwise the
     * plain-ASCII {@code fallback}.
     */
    public static String glyph(String unicode, String fallback) {
        return ENCODER != null && ENCODER.canEncode(unicode) ? unicode : fallback;
    }

    // -------------------------------------------------------------------------
    // Screen control
    // -------------------------------------------------------------------------

    /**
     * Clears the screen. Uses an ANSI escape when colours are on (instant,
     * no flicker), the platform command otherwise, and just a divider line
     * when output isn't a terminal (so logs stay readable).
     */
    public static void clearScreen() {
        if (!INTERACTIVE) {
            System.out.println();
            return;
        }
        if (colorEnabled) {
            System.out.print("\u001b[H\u001b[2J\u001b[3J");
            System.out.flush();
            return;
        }
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++)
                System.out.println();
        }
    }

    /** Moves the cursor up {@code lines} rows (used to redraw animation frames). */
    static void cursorUp(int lines) {
        if (lines > 0) {
            System.out.print("\u001b[" + lines + "A\r");
        }
    }

    /** Waits {@code millis} ms if animations are on; returns at once otherwise. */
    public static void pause(long millis) {
        if (!isAnimationsEnabled()) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -------------------------------------------------------------------------
    // Detection
    // -------------------------------------------------------------------------

    private static boolean detectInteractive() {
        Console console = System.console();
        if (console == null) {
            return false;
        }
        // JDK 22+ returns a Console even when redirected; ask it if it's a terminal.
        try {
            Method isTerminal = Console.class.getMethod("isTerminal");
            return (Boolean) isTerminal.invoke(console);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    private static boolean detectColor() {
        String forced = System.getenv("SIMCLI_COLOR");
        if ("always".equalsIgnoreCase(forced)) return true;
        if ("never".equalsIgnoreCase(forced)) return false;
        if (System.getenv("NO_COLOR") != null || !INTERACTIVE) return false;

        String term = System.getenv("TERM");
        if (!System.getProperty("os.name").contains("Windows")) {
            return term == null || !term.equals("dumb");
        }
        // The classic Windows console only understands ANSI when a modern
        // host (Windows Terminal, VS Code, ConEmu, mintty...) is in front of it.
        return System.getenv("WT_SESSION") != null
                || System.getenv("TERM_PROGRAM") != null
                || "ON".equalsIgnoreCase(System.getenv("ConEmuANSI"))
                || System.getenv("ANSICON") != null
                || term != null;
    }

    private static CharsetEncoder detectEncoder() {
        try {
            String name = System.getProperty("stdout.encoding");
            Charset cs = name != null ? Charset.forName(name) : Charset.defaultCharset();
            return cs.newEncoder();
        } catch (Exception e) {
            return null;
        }
    }
}
