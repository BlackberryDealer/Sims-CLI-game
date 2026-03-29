package simcli.ui;

import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.ui.ascii.AsciiEngine;
import simcli.utils.GameRandom;
import simcli.world.interactables.Interactable;
import java.util.List;

/**
 * A static utility class providing low-level terminal output methods.
 * Centralizes screen clearing, formatting, and common CLI prompts so that
 * the rest of the engine does not depend on system-specific print commands.
 */
public class UIManager {
    private static final String[] HINTS = {
            "Hint: Buying food at the supermarket keeps you from starving!",
            "Hint: A good job requires energy and time management.",
            "Hint: Upgrading your home lets you buy more furniture.",
            "Hint: Visit the Park to meet new people.",
            "Hint: Sleep restores energy but leaves you hungry.",
            "Hint: Always check your SIMs needs every few ticks."
    };

    /**
     * Clears the terminal screen. Uses OS-specific commands (cls for Windows, 
     * clear for Unix) or falls back to printing empty lines.
     */
    public static void clearScreen() {
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

    /**
     * Prints a randomly selected gameplay hint enclosed in brackets.
     */
    public static void printHint() {
        int index = GameRandom.RANDOM.nextInt(HINTS.length);
        System.out.println("[" + HINTS[index] + "]");
    }

    /**
     * Displays a brief animated sequence of "Zzz..." to simulate time passing 
     * while a Sim sleeps.
     */
    public static void sleepAnimation() {
        try {
            String[] frames = {
                    "Zzz...   ",
                    " Zzz...  ",
                    "  Zzz... ",
                    "   Zzz..."
            };
            for (int i = 0; i < 4; i++) {
                System.out.print("\r" + frames[i]);
                Thread.sleep(500);
            }
            System.out.println("\nMorning has arrived!");
        } catch (InterruptedException e) {
            System.out.println("Morning has arrived!");
        }
    }

    /**
     * Briefly flashes the ASCII art for the Sim's current action before continuing.
     * 
     * @param player the Sim performing the action to be rendered
     */
    public static void displayActionAnimation(Sim player) {
        clearScreen();
        System.out.println(new AsciiEngine().render(player, null)); // fetches ascii based on current sim state
        System.out.println();
        try {
            Thread.sleep(600);
        } catch (Exception ignored) {
        }
    }

    /**
     * Prints the primary command menu, dynamically separating contextual actions 
     * like interactable objects from system commands.
     * 
     * @param player        the currently active Sim
     * @param items         the list of interactable objects available in the current room
     * @param isResidential true if the location is a home, exposing build/buy commands
     */
    public static void printActionGrid(Sim player, List<Interactable> items, boolean isResidential) {
        System.out.println("\nAvailable Actions:");
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("[%2d] Use %-15s", (i + 1), items.get(i).getObjectName());
            if ((i + 1) % 4 == 0)
                System.out.println();
        }
        if (items.size() % 4 != 0)
            System.out.println();

        System.out.println("\n[System]   [W] Go to Work   [T] Travel   [J] Job Market");
        if (isResidential) {
            System.out.println("[House]    [M] Move Room    [H] House Info   [U] Upgrade Room");
        }
        
        String personalLine = "[Personal] [I] Character Status   [V] Inventory   [K] Switch Sim";
        if (player.getRelationshipManager().getSpouse() != null) {
            personalLine += "   [L] Interact with Spouse";
        }
        System.out.println(personalLine);
        
        System.out.println("[General]  [S] Save & Exit");
    }

    /**
     * Standard utility to print a message to the terminal.
     * 
     * @param message the string to print
     */
    public static void printMessage(String message) {
        System.out.println(message);
    }

    /**
     * Standard utility to print an alert or warning, prefixed with [WARNING].
     * 
     * @param message the warning string to print
     */
    public static void printWarning(String message) {
        System.out.println("\n[WARNING] " + message);
    }

    /**
     * Flushes the SimulationLogger backlog and prepares the terminal for user input.
     * 
     * @param message the prompt string (e.g., "COMMAND> ")
     */
    public static void prompt(String message) {
        SimulationLogger.getInstance().flushAndPrint();
        System.out.print(message);
    }

    /**
     * Prints the final statistics when all Sims in the household have died.
     * 
     * @param totalTicks the total timeframe survived
     * @param totalMoney the total cash earned across the session
     * @param totalItems the total items purchased across the session
     */
    public static void printGameOverStats(int totalTicks, int totalMoney, int totalItems) {
        System.out.println("\n*** GAME OVER ***");
        System.out.println("All your Sims have passed away.");
        System.out.println("--- WORLD STATS ---");
        System.out.println("Total Ticks Survived: " + totalTicks);
        System.out.println("Total Money Earned: $" + totalMoney);
        System.out.println("Total Items Bought: " + totalItems);
        System.out.println("-------------------------");
    }
}
