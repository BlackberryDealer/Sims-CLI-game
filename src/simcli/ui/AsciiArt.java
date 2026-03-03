package simcli.ui;

public class AsciiArt {
    
    public static void printLogo() {
        System.out.println("==================================================");
        System.out.println("   _____ _____ __  __  _____ ");
        System.out.println("  / ____|_   _|  \\/  |/ ____|");
        System.out.println(" | (___   | | | \\  / | (___  ");
        System.out.println("  \\___ \\  | | | |\\/| |\\___ \\ ");
        System.out.println("  ____) |_| |_| |  | |____) |");
        System.out.println(" |_____/|_____|_|  |_|_____/ ");
        System.out.println("               CLI EDITION");
        System.out.println("==================================================");
    }

    public static void printMenuOptions() {
        System.out.println("  [1] Create New World");
        System.out.println("  [2] Load Existing World");
        System.out.println("  [3] Exit Game");
        System.out.println("==================================================");
    }

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

    public static void printHouse() {
        System.out.println("      ~+~");
        System.out.println("     /   \\");
        System.out.println("    /_____\\");
        System.out.println("    |  _  |");
        System.out.println("    | | | |");
        System.out.println("    |_|_|_|");
    }

    public static void printStore() {
        System.out.println("   [MARKET]");
        System.out.println("   /______\\");
        System.out.println("   | OPEN |");
        System.out.println("   |  $$  |");
        System.out.println("   |______|");
    }
    
    public static void printTravelAnimation() {
        System.out.print("Traveling");
        for (int i = 0; i < 3; i++) {
            try { Thread.sleep(300); } catch(Exception ignored) {}
            System.out.print(".");
        }
        System.out.println();
    }
}