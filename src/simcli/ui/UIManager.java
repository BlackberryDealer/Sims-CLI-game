package simcli.ui;

import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.ui.ascii.AsciiEngine;

/**
 * Centralised static utility for all user-facing output: screen clearing,
 * tips, animations, prompts, message styles, and game-over stats.
 *
 * <p>Centralizes formatting and common CLI prompts so that the rest of
 * the engine does not depend on system-specific print commands. All UI
 * output in the application is routed through this class for consistency.
 * Messages may contain colour tags (see {@link Style}).</p>
 */
public class UIManager {
    private static final String[] HINTS = {
            "Groceries from the Supermarket keep your Sims from starving.",
            "A good job needs energy and time management - sleep before your shift!",
            "Upgrade a room to make space for more furniture.",
            "Visit the Park to meet new people - you might find love!",
            "Sleeping restores energy, but you'll wake up hungry.",
            "Keep an eye on the plumbob: green is happy, red means trouble.",
            "Store food in the Fridge so it's there when you need it.",
            "The office is closed on weekends - plan some fun instead.",
            "Missing too many weekdays of work will get you fired.",
            "Press K to switch between the Sims in your household.",
            "Babies can't feed themselves - use [L] to feed them.",
            "Save the money for the Bungalow: it's a lovely upgrade.",
    };

    private static final AsciiEngine ASCII = new AsciiEngine();
    private static int hintCounter = 0;

    // -------------------------------------------------------------------------
    // Screen & animation
    // -------------------------------------------------------------------------

    /** Clears the terminal (see {@link Term#clearScreen()}). */
    public static void clearScreen() {
        Term.clearScreen();
    }

    /** Prints a gameplay tip. The tip changes every few turns rather than every frame. */
    public static void printHint() {
        int index = (hintCounter++ / 4) % HINTS.length;
        printMessage("  {Y}" + Panel.STAR + " Tip:{/} {k}" + HINTS[index] + "{/}");
    }

    /**
     * Plays the night-to-morning sleep animation.
     *
     * @param name  the sleeping Sim's name.
     * @param hours how long they sleep.
     */
    public static void sleepAnimation(String name, int hours) {
        AsciiArt.printSleepAnimation(name, hours);
    }

    /** Clears the screen and plays the animation for the Sim's current action. */
    public static void displayActionAnimation(Sim player) {
        clearScreen();
        System.out.println();
        ASCII.animate(player, null);
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Message styles
    // -------------------------------------------------------------------------

    /** Prints a line of text (colour tags allowed). */
    public static void printMessage(String message) {
        System.out.println(Style.render(message));
    }

    /** Prints a yellow warning. */
    public static void printWarning(String message) {
        System.out.println(Style.render("\n  {Y}" + Panel.WARN + " " + message + "{/}"));
    }

    /** Prints a green success message. */
    public static void success(String message) {
        System.out.println(Style.render("  {G}" + Panel.CHECK + "{/} " + message));
    }

    /** Prints a red error message. */
    public static void error(String message) {
        System.out.println(Style.render("  {R}" + Panel.CROSS + "{/} " + message));
    }

    /** Prints a neutral, indented info line. */
    public static void info(String message) {
        System.out.println(Style.render("  {C}" + Panel.DOT + "{/} " + message));
    }

    /**
     * Prints a message from the simulation log, styling it by kind:
     * warnings in yellow, big moments ({@code *** ... ***}) as highlighted
     * events, everything else as a plain bullet.
     */
    public static void printEvent(String message) {
        for (String raw : message.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[WARNING]")) {
                System.out.println(Style.render("  {Y}" + Panel.WARN + " "
                        + line.substring("[WARNING]".length()).trim() + "{/}"));
            } else if (line.contains("***")) {
                String text = line.replace("*", "").trim();
                if (!text.isEmpty()) {
                    System.out.println(Style.render("  {M}{*}" + Panel.STAR + " " + text + "{/}"));
                }
            } else {
                info(line);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Menus & prompts
    // -------------------------------------------------------------------------

    /** Prints a sub-menu title bar. */
    public static void title(String title) {
        System.out.println();
        System.out.println(Panel.rule("{W}{*}" + title + "{/}", 'c'));
    }

    /** Prints a menu option, e.g. {@code [1] Bedroom}. */
    public static void option(String key, String label) {
        System.out.println(Style.render("  " + Panel.option(key, label)));
    }

    /** Prints a menu option with a greyed-out hint after it. */
    public static void option(String key, String label, String hint) {
        System.out.println(Style.render("  " + Panel.option(key, label, hint)));
    }

    /**
     * Flushes pending log messages, then prints a prompt (no newline).
     * Prompts ending in {@code >} get a styled arrow; "Press ENTER" prompts
     * are shown subtly.
     */
    public static void prompt(String message) {
        SimulationLogger.getInstance().flushAndPrint();
        System.out.print(Style.render(stylePrompt(message)));
        System.out.flush();
    }

    private static String stylePrompt(String message) {
        int start = 0;
        while (start < message.length() && message.charAt(start) == '\n') {
            start++;
        }
        String leading = message.substring(0, start);
        String text = message.substring(start);
        if (text.contains("Press ENTER")) {
            return leading + "  {k}" + text.trim() + "{/} ";
        }
        String trimmed = text.stripTrailing();
        if (trimmed.endsWith(">")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).stripTrailing() + " " + Panel.ARROW;
        }
        return leading + "  {C}{*}" + trimmed + "{/} ";
    }

    /**
     * Prints the end-of-game summary.
     *
     * @param totalTicks hours survived.
     * @param totalMoney money earned across the household.
     * @param totalItems items bought across the household.
     */
    public static void printGameOverStats(int totalTicks, int totalMoney, int totalItems) {
        int days = totalTicks / 24;
        System.out.println();
        System.out.println(Panel.box("{R}{*}GAME OVER{/}", 'r',
                "",
                "{W}All of your Sims have passed away.{/}",
                "",
                "{k}Time survived{/}    " + days + " days, " + (totalTicks % 24) + " hours  {k}(" + totalTicks + " ticks){/}",
                "{k}Money earned{/}     {G}$" + totalMoney + "{/}",
                "{k}Items bought{/}     " + totalItems,
                ""));
    }
}
