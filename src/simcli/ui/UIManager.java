package simcli.ui;

import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.ui.ascii.AsciiEngine;
import simcli.utils.GameRandom;
import simcli.world.interactables.Interactable;
import java.util.List;

public class UIManager {
    private static final String[] HINTS = {
            "Hint: Buying food at the supermarket keeps you from starving!",
            "Hint: A good job requires energy and time management.",
            "Hint: Upgrading your home lets you buy more furniture.",
            "Hint: Visit the Park to meet new people.",
            "Hint: Sleep restores energy but leaves you hungry."
    };

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

    public static void printHint() {
        int index = GameRandom.RANDOM.nextInt(HINTS.length);
        System.out.println("[" + HINTS[index] + "]");
    }

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

    public static void displayActionAnimation(Sim player) {
        clearScreen();
        System.out.println(new AsciiEngine().render(player, null));
        System.out.println();
        try {
            Thread.sleep(600);
        } catch (Exception ignored) {
        }
    }

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

    public static void printMessage(String message) {
        System.out.println(message);
    }



    public static void printWarning(String message) {
        System.out.println("\n[WARNING] " + message);
    }

    public static void prompt(String message) {
        SimulationLogger.flushAndPrint();
        System.out.print(message);
    }

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
