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

    
    // can add more later
    // public static void printHouse() { ... }
    // public static void printGym() { ... }
}