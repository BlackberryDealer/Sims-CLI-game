package simcli.ui;

import simcli.engine.GameEngine;
import simcli.utils.SaveManager;

import java.util.List;
import java.util.Scanner;

public class MainMenu {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean inMenu = true;

        while (inMenu) {
            simcli.ui.UIManager.clearScreen();
            AsciiArt.printLogo();
            System.out.println("  [1] Create New World");
            System.out.println("  [2] Load Existing World");
            System.out.println("  [3] Delete Saved World");
            System.out.println("  [4] Exit Game");
            System.out.println("==================================================");

            List<String> saves = SaveManager.getExistingSaves();
            if (!saves.isEmpty()) {
                System.out.println("--- Saved Worlds ---");
                for (String save : saves) {
                    System.out.println("- " + save);
                }
                System.out.println("--------------------");
            }

            System.out.print("COMMAND> ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                createNewWorld(scanner);
            } else if (choice.equals("2")) {
                loadWorld(scanner);
            } else if (choice.equals("3")) {
                deleteWorld(scanner);
            } else if (choice.equals("4")) {
                System.out.println("Goodbye!");
                inMenu = false;
            } else {
                System.out.println("Invalid option. Please enter 1, 2, 3, or 4.");
            }
        }
        scanner.close();
    }

    private static void createNewWorld(Scanner scanner) {
        String newName = "";
        while (true) {
            System.out.print("\nEnter a name for your new world: ");
            newName = scanner.nextLine().trim();

            if (newName.isEmpty()) {
                System.out.println("Error: World name cannot be blank!");
            } else if (SaveManager.saveExists(newName)) {
                System.out.println("Error: World '" + newName + "' already exists!");
            } else {
                break;
            }
        }

        GameEngine newGame = new GameEngine(newName);
        newGame.init(scanner);
        newGame.run(scanner); 
    }

    private static void loadWorld(Scanner scanner) {
        List<String> saves = SaveManager.getExistingSaves();

        if (saves.isEmpty()) {
            System.out.println("\nNo saved games found! Please create a new world.\n");
            System.out.print("Press ENTER to return...");
            scanner.nextLine();
            return;
        }

        System.out.println("\n--- Existing Worlds ---");
        for (String save : saves) {
            System.out.println("- " + save);
        }

        System.out.print("Enter world name to load (or press Enter to cancel): ");
        String loadName = scanner.nextLine().trim();

        if (loadName.isEmpty()) return;

        if (SaveManager.saveExists(loadName)) {
            System.out.println("Loading '" + loadName + "'...");
            GameEngine loadedGame = SaveManager.loadGame(loadName);
            if (loadedGame != null) {
                loadedGame.run(scanner); 
            }
        } else {
            System.out.println("Error: World '" + loadName + "' does not exist.\n");
            System.out.print("Press ENTER to return...");
            scanner.nextLine();
        }
    }

    private static void deleteWorld(Scanner scanner) {
        List<String> saves = SaveManager.getExistingSaves();
        if (saves.isEmpty()) {
            System.out.println("\nNo saved games found to delete.\n");
            System.out.print("Press ENTER to return...");
            scanner.nextLine();
            return;
        }

        System.out.print("\nEnter world name to delete (or press Enter to cancel): ");
        String deleteName = scanner.nextLine().trim();

        if (deleteName.isEmpty()) return;

        if (SaveManager.saveExists(deleteName)) {
            System.out.print("Are you sure you want to delete this world? (Y/N): ");
            String confirm = scanner.nextLine().trim().toUpperCase();
            if (confirm.equals("Y")) {
                if (SaveManager.deleteSave(deleteName)) {
                    System.out.println("World '" + deleteName + "' has been deleted.\n");
                } else {
                    System.out.println("Error deleting '" + deleteName + "'.\n");
                }
            } else {
                System.out.println("Deletion canceled.\n");
            }
        } else {
            System.out.println("Error: World '" + deleteName + "' does not exist.\n");
        }
        System.out.print("Press ENTER to return...");
        scanner.nextLine();
    }
}