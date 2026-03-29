package simcli;

import simcli.ui.MainMenu;
import java.util.Scanner;

/**
 * The main entry point for the Sims-CLI game.
 * This class initializes the game interface and starts the main menu loop.
 */
public class Main {
    /**
     * The main method that serves as the entry point for the application.
     * It sets up the input scanner and launches the main menu.
     * 
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        MainMenu menu = new MainMenu(scanner);
        menu.display();
        
        scanner.close();
    }
}
