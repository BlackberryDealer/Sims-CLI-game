package simcli.ui;

/**
 * AsciiArt — utility class for one-off static art not tied to a Sim action or
 * location.
 *
 * <h2>Design note — no duplicates</h2>
 * <p>
 * This class intentionally contains ONLY art that has no equivalent elsewhere:
 * </p>
 * <ul>
 * <li>{@link #printLogo()} — the game title banner shown on the main menu.</li>
 * <li>{@link #printTravelAnimation()} — a short animated travel spinner.</li>
 * </ul>
 *
 * <p>
 * Location and action ASCII art lives exclusively in the
 * {@code simcli.ui.ascii.providers} package, rendered through
 * {@link simcli.ui.ascii.AsciiEngine}. Screen clearing is handled exclusively
 * by {@link UIManager#clearScreen()}. Those methods
 * (printHouse, printStore, clearScreen) have been removed from this class
 * to eliminate duplication.
 * </p>
 */
public class AsciiArt {

    /**
     * Prints the SIMS CLI game title logo.
     * Called once by {@link MainMenu} at the top of each menu render cycle.
     */
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

    /**
     * Prints a short animated travel message with progressive dots.
     * Called by {@link simcli.engine.InputHandler} when the player travels
     * between locations.
     */
    public static void printTravelAnimation() {
        System.out.print("Traveling");
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(300);
            } catch (Exception ignored) {
            }
            System.out.print(".");
        }
        System.out.println();
    }
}