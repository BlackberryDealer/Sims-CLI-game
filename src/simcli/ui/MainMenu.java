package simcli.ui;

import simcli.engine.GameEngine;
import simcli.persistence.SaveManager;
import simcli.entities.actors.Gender;
import simcli.entities.actors.Job;
import simcli.entities.actors.Sim;

import java.util.List;
import java.util.Scanner;

/**
 * Main menu controller — handles new game creation, loading, and deletion.
 * All output is routed through {@link UIManager} for consistency.
 */
public class MainMenu {
    private final Scanner scanner;

    public MainMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void display() {
        boolean inMenu = true;

        while (inMenu) {
            UIManager.clearScreen();
            AsciiArt.printLogo();
            UIManager.printMessage("  [1] Create New World");
            UIManager.printMessage("  [2] Load Existing World");
            UIManager.printMessage("  [3] Delete Saved World");
            UIManager.printMessage("  [4] Exit Game");
            UIManager.printMessage("==================================================");

            List<String> saves = SaveManager.getExistingSaves();
            if (!saves.isEmpty()) {
                UIManager.printMessage("--- Saved Worlds ---");
                for (String save : saves) {
                    UIManager.printMessage("- " + save);
                }
                UIManager.printMessage("--------------------");
            }

            UIManager.prompt("COMMAND> ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                createNewWorld();
            } else if (choice.equals("2")) {
                loadWorld();
            } else if (choice.equals("3")) {
                deleteWorld();
            } else if (choice.equals("4")) {
                UIManager.printMessage("Goodbye!");
                inMenu = false;
            } else {
                UIManager.printMessage("Invalid option. Please enter 1, 2, 3, or 4.");
            }
        }
    }

    private void createNewWorld() {
        String newName = "";
        while (true) {
            UIManager.prompt("\nEnter a name for your new world: ");
            newName = scanner.nextLine().trim();

            if (newName.isEmpty()) {
                UIManager.printMessage("Error: World name cannot be blank!");
            } else if (SaveManager.saveExists(newName)) {
                UIManager.printMessage("Error: World '" + newName + "' already exists!");
            } else {
                break;
            }
        }

        UIManager.printMessage("\n=== Character Creation ===");
        UIManager.prompt("Enter your Sim's Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Dylan";
            UIManager.printMessage("Sim's name set to Dylan.");
        }

        int age = 21;
        while (true) {
            UIManager.prompt("Enter your Sim's Age (" + simcli.utils.GameConstants.ADULT_AGE + "-" + (simcli.utils.GameConstants.DEATH_AGE - 1) + "): ");
            try {
                String inputAge = scanner.nextLine().trim();
                if (inputAge.isEmpty()) {
                    UIManager.printMessage("Sim's age set to 21 years old.");
                    break;
                }
                int parsedAge = Integer.parseInt(inputAge);
                if (parsedAge >= simcli.utils.GameConstants.ADULT_AGE && parsedAge < simcli.utils.GameConstants.DEATH_AGE) {
                    age = parsedAge;
                    break;
                } else {
                    UIManager.printMessage("Age must be between " + simcli.utils.GameConstants.ADULT_AGE + " and " + (simcli.utils.GameConstants.DEATH_AGE - 1) + ". Please try again.");
                }
            } catch (NumberFormatException e) {
                UIManager.printMessage("Invalid age format. Please enter a valid number.");
            }
        }

        Gender gender = Gender.MALE;
        while (true) {
            UIManager.prompt("Enter your Sim's Gender (M/F): ");
            String gInput = scanner.nextLine().trim().toUpperCase();
            if (gInput.equals("M")) {
                gender = Gender.MALE;
                break;
            }
            if (gInput.equals("F")) {
                gender = Gender.FEMALE;
                break;
            }
            UIManager.printMessage("Please enter M or F.");
        }

        Sim player1 = new Sim(name, age, gender, Job.UNEMPLOYED);
        UIManager.printMessage("\n=== Booting World: " + newName + " ===");

        GameEngine newGame = new GameEngine(newName, player1);
        newGame.run(scanner);
    }

    private void loadWorld() {
        List<String> saves = SaveManager.getExistingSaves();

        if (saves.isEmpty()) {
            UIManager.printMessage("\nNo saved games found! Please create a new world.\n");
            UIManager.prompt("Press ENTER to return...");
            scanner.nextLine();
            return;
        }

        UIManager.printMessage("\n--- Existing Worlds ---");
        for (String save : saves) {
            UIManager.printMessage("- " + save);
        }

        UIManager.prompt("Enter world name to load (or press Enter to cancel): ");
        String loadName = scanner.nextLine().trim();

        if (loadName.isEmpty()) return;

        if (SaveManager.saveExists(loadName)) {
            UIManager.printMessage("Loading '" + loadName + "'...");
            GameEngine loadedGame = SaveManager.loadGame(loadName);
            if (loadedGame != null) {
                loadedGame.run(scanner);
            }
        } else {
            UIManager.printMessage("Error: World '" + loadName + "' does not exist.\n");
            UIManager.prompt("Press ENTER to return...");
            scanner.nextLine();
        }
    }

    private void deleteWorld() {
        List<String> saves = SaveManager.getExistingSaves();
        if (saves.isEmpty()) {
            UIManager.printMessage("\nNo saved games found to delete.\n");
            UIManager.prompt("Press ENTER to return...");
            scanner.nextLine();
            return;
        }

        UIManager.prompt("\nEnter world name to delete (or press Enter to cancel): ");
        String deleteName = scanner.nextLine().trim();

        if (deleteName.isEmpty()) return;

        if (SaveManager.saveExists(deleteName)) {
            UIManager.prompt("Are you sure you want to delete this world? (Y/N): ");
            String confirm = scanner.nextLine().trim().toUpperCase();
            if (confirm.equals("Y")) {
                if (SaveManager.deleteSave(deleteName)) {
                    UIManager.printMessage("World '" + deleteName + "' has been deleted.\n");
                } else {
                    UIManager.printMessage("Error deleting '" + deleteName + "'.\n");
                }
            } else {
                UIManager.printMessage("Deletion canceled.\n");
            }
        } else {
            UIManager.printMessage("Error: World '" + deleteName + "' does not exist.\n");
        }
        UIManager.prompt("Press ENTER to return...");
        scanner.nextLine();
    }
}