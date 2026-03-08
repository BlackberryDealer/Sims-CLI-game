package simcli.ui;

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
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    public static void printHint() {
        int index = (int) (Math.random() * HINTS.length);
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

    public static void printActionGrid(List<Interactable> items, boolean isResidential) {
        System.out.println("\nAvailable Actions:");
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("[%2d] Use %-15s", (i + 1), items.get(i).getObjectName());
            if ((i + 1) % 4 == 0) System.out.println();
        }
        if (items.size() % 4 != 0) System.out.println();

        System.out.println("\n[System]   [W] Go to Work   [T] Travel   [J] Job Market");
        if (isResidential) {
            System.out.println("[House]    [M] Move Room    [H] House Info   [U] Upgrade Room");
        }
        System.out.println("[Personal] [I] Character Status   [V] Inventory");
        System.out.println("[General]  [S] Save & Exit");
    }
}
