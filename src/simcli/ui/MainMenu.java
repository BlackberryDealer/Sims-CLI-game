package simcli.ui;

import simcli.engine.GameEngine;
import simcli.engine.SimulationLogger;
import simcli.persistence.SaveManager;
import simcli.entities.models.Aspiration;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.Trait;
import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main menu controller — handles new game creation, loading, deletion and
 * display settings. All output is routed through {@link UIManager} for
 * consistency.
 */
public class MainMenu {
    /** Longest allowed world name (it becomes a file name). */
    private static final int MAX_WORLD_NAME = 24;
    /** Longest allowed Sim name (keeps the HUD tidy). */
    public static final int MAX_SIM_NAME = 16;

    private final Scanner scanner;
    /** A message to show above the menu on its next redraw (e.g. "Invalid option"). */
    private String notice = null;

    public MainMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Displays and processes the main menu selection loop until a game is launched or exited.
     */
    public void display() {
        boolean inMenu = true;

        while (inMenu) {
            UIManager.clearScreen();
            AsciiArt.printLogo();

            List<String> saves = SaveManager.getExistingSaves();
            List<String> lines = new ArrayList<>();
            lines.add("");
            lines.add("   " + Panel.option("1", "New world", "start a fresh household"));
            lines.add("   " + Panel.option("2", "Load world", saves.isEmpty() ? "no saves yet"
                    : saves.size() + (saves.size() == 1 ? " saved world" : " saved worlds")));
            lines.add("   " + Panel.option("3", "Delete world"));
            lines.add("   " + Panel.option("4", "Settings", "colours " + onOff(Term.isColorEnabled())
                    + ", animations " + onOff(Term.isAnimationsEnabled())));
            lines.add("   " + Panel.option("0", "Quit"));
            lines.add("");
            System.out.println(Panel.box("{W}Main menu{/}", 'g', lines));

            if (!saves.isEmpty()) {
                UIManager.printMessage("  {k}Saved worlds:{/} " + String.join(Style.render("{k}, {/}"), saves));
            }
            if (notice != null) {
                UIManager.printWarning(notice);
                notice = null;
            }

            UIManager.prompt("\nChoose> ");
            String choice = scanner.nextLine().trim();

            switch (choice.toUpperCase()) {
                case "1": createNewWorld(); break;
                case "2": loadWorld(); break;
                case "3": deleteWorld(); break;
                case "4": settings(); break;
                case "0":
                case "Q":
                    UIManager.printMessage("\n  {G}Thanks for playing! Goodbye.{/}\n");
                    inMenu = false;
                    break;
                default:
                    notice = "Invalid option. Please enter 1, 2, 3, 4 or 0.";
            }
        }
    }

    private static String onOff(boolean on) {
        return on ? "on" : "off";
    }

    // -------------------------------------------------------------------------
    // New world
    // -------------------------------------------------------------------------

    private void createNewWorld() {
        SimulationLogger.getInstance().reset();
        UIManager.clearScreen();
        UIManager.title("Create a new world");

        String newName;
        while (true) {
            UIManager.prompt("\nWorld name> ");
            newName = scanner.nextLine().trim();

            String problem = worldNameProblem(newName);
            if (problem != null) {
                UIManager.error(problem);
            } else if (SaveManager.saveExists(newName)) {
                UIManager.error("Error: World '" + newName + "' already exists!");
            } else {
                break;
            }
        }

        UIManager.title("Household creation");
        int simCount = 1;
        while (true) {
            UIManager.prompt("\nHow many Sims in this household? (1-4)> ");
            try {
                String inputStr = scanner.nextLine().trim();
                simCount = Integer.parseInt(inputStr);
                if (simCount >= 1 && simCount <= 4) break;
                UIManager.error("Please enter a number between 1 and 4.");
            } catch (NumberFormatException e) {
                UIManager.error("Please enter a valid number.");
            }
        }

        List<Sim> startingNeighborhood = new ArrayList<>();
        for (int i = 0; i < simCount; i++) {
            UIManager.printMessage("\n" + Panel.rule("{W}Sim " + (i + 1) + " of " + simCount + "{/}", 'g'));
            UIManager.prompt("Name> ");
            String name = cleanName(scanner.nextLine());
            if (name.isEmpty()) {
                name = "Sim" + (i + 1);
                UIManager.info("Sim's name set to " + name + ".");
            }

            int age = 21;
            while (true) {
                UIManager.prompt("Age (" + GameConstants.ADULT_AGE + "-" + (GameConstants.DEATH_AGE - 1) + ", ENTER for 21)> ");
                try {
                    String inputAge = scanner.nextLine().trim();
                    if (inputAge.isEmpty()) {
                        break;
                    }
                    int parsedAge = Integer.parseInt(inputAge);
                    if (parsedAge >= GameConstants.ADULT_AGE && parsedAge < GameConstants.DEATH_AGE) {
                        age = parsedAge;
                        break;
                    } else {
                        UIManager.error("Age must be between " + GameConstants.ADULT_AGE + " and " + (GameConstants.DEATH_AGE - 1) + ". Please try again.");
                    }
                } catch (NumberFormatException e) {
                    UIManager.error("Invalid age format. Please enter a valid number.");
                }
            }

            Gender gender = Gender.MALE;
            while (true) {
                UIManager.prompt("Gender (M/F)> ");
                String gInput = scanner.nextLine().trim().toUpperCase();
                if (gInput.equals("M")) {
                    gender = Gender.MALE;
                    break;
                }
                if (gInput.equals("F")) {
                    gender = Gender.FEMALE;
                    break;
                }
                UIManager.error("Please enter M or F.");
            }
            Sim sim = new Sim(name, age, gender, Job.UNEMPLOYED);
            Aspiration[] aspirations = Aspiration.values();
            UIManager.printMessage("  {k}What does " + name + " want out of life?{/}");
            for (int a = 0; a < aspirations.length; a++) {
                UIManager.option(String.valueOf(a + 1), Style.fit(aspirations[a].getDisplayName(), 18),
                        aspirations[a].getGoal());
            }
            while (true) {
                UIManager.prompt("Aspiration (1-" + aspirations.length + ", ENTER for random)> ");
                String aInput = scanner.nextLine().trim();
                if (aInput.isEmpty()) {
                    break; // keep the random one the Sim was born with
                }
                try {
                    int pick = Integer.parseInt(aInput);
                    if (pick >= 1 && pick <= aspirations.length) {
                        sim.setAspiration(aspirations[pick - 1]);
                        break;
                    }
                } catch (NumberFormatException e) {
                    // fall through to the error below
                }
                UIManager.error("Please enter a number from 1 to " + aspirations.length + ", or press ENTER.");
            }
            startingNeighborhood.add(sim);
            UIManager.success("{W}" + name + "{/} joins the household - age " + age + ", personality: {M}"
                    + traitName(sim.getTraits().get(0)) + "{/} {k}(" + traitBlurb(sim.getTraits().get(0)) + "){/}"
                    + ", dreams of being a {M}" + sim.getAspiration().getDisplayName() + "{/}");
        }

        System.out.println();
        UIManager.success("Booting world {W}" + newName + "{/}...");
        UIManager.prompt("\nPress ENTER to begin your new life...");
        scanner.nextLine();

        GameEngine newGame = new GameEngine(newName, startingNeighborhood);
        newGame.run(scanner);
    }

    /** Returns why a world name can't be used, or {@code null} if it's fine. */
    static String worldNameProblem(String name) {
        if (name.isEmpty()) {
            return "Error: World name cannot be blank!";
        }
        if (name.length() > MAX_WORLD_NAME) {
            return "World names can be at most " + MAX_WORLD_NAME + " characters.";
        }
        if (!name.matches("[A-Za-z0-9 _-]+")) {
            return "Use only letters, numbers, spaces, '-' and '_' in world names.";
        }
        if (name.matches("(?i)(con|prn|aux|nul|com[1-9]|lpt[1-9])")) {
            return "That name is reserved by the system - please pick another.";
        }
        return null;
    }

    /**
     * Tidies a typed Sim name: trims it, removes colour-tag braces, and caps
     * its length so it fits the HUD.
     */
    public static String cleanName(String raw) {
        String name = Style.plain(raw).replace("{", "").replace("}", "").trim();
        return name.length() > MAX_SIM_NAME ? name.substring(0, MAX_SIM_NAME).trim() : name;
    }

    private static String traitName(Trait t) {
        return t.name().charAt(0) + t.name().substring(1).toLowerCase();
    }

    private static String traitBlurb(Trait t) {
        switch (t) {
            case ACTIVE: return "tires out slowly";
            case LAZY: return "tires out quickly";
            case SOCIALITE: return "makes friends fast";
            default: return "";
        }
    }

    // -------------------------------------------------------------------------
    // Load / delete
    // -------------------------------------------------------------------------

    private void loadWorld() {
        SimulationLogger.getInstance().reset();
        String loadName = pickSave("Load which world?");
        if (loadName == null) {
            return;
        }

        UIManager.info("Loading '" + loadName + "'...");
        GameEngine loadedGame = SaveManager.loadGame(loadName);
        if (loadedGame != null) {
            UIManager.title("Welcome back to " + loadName);
            for (Sim s : loadedGame.getNeighborhood()) {
                UIManager.info("{W}" + s.getName() + "{/} {k}- age " + s.getAge() + ", " + s.getCareer().getTitle()
                        + (s.getState() == simcli.entities.models.SimState.DEAD ? ", passed away" : "") + "{/}");
            }
            UIManager.prompt("\nPress ENTER to resume game...");
            scanner.nextLine();
            loadedGame.run(scanner);
        } else {
            // loadGame() printed either the final stats of a finished game
            // or why the save couldn't be read — keep it on screen.
            UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
        }
    }

    private void deleteWorld() {
        String deleteName = pickSave("Delete which world?");
        if (deleteName == null) {
            return;
        }

        UIManager.prompt("Really delete '" + deleteName + "' forever? (Y/N)> ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (confirm.equals("Y")) {
            if (SaveManager.deleteSave(deleteName)) {
                UIManager.success("World '" + deleteName + "' has been deleted.");
            } else {
                UIManager.error("Error deleting '" + deleteName + "'.");
            }
        } else {
            UIManager.info("Deletion canceled.");
        }
        UIManager.prompt("\nPress ENTER to return...");
        scanner.nextLine();
    }

    /**
     * Lists saved worlds and lets the player pick one by number or by name.
     *
     * @return the chosen world's name, or {@code null} if cancelled / none exist.
     */
    private String pickSave(String title) {
        List<String> saves = SaveManager.getExistingSaves();
        UIManager.clearScreen();
        UIManager.title(title);

        if (saves.isEmpty()) {
            UIManager.info("No saved games found! Please create a new world.");
            UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
            return null;
        }

        for (int i = 0; i < saves.size(); i++) {
            UIManager.option(String.valueOf(i + 1), saves.get(i));
        }
        UIManager.option("0", "Back");

        while (true) {
            UIManager.prompt("\nWorld (number or name)> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty() || input.equals("0")) {
                return null;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 1 && index <= saves.size()) {
                    return saves.get(index - 1);
                }
            } catch (NumberFormatException ignored) {
                // not a number — try it as a name
            }
            for (String save : saves) {
                if (save.equalsIgnoreCase(input)) {
                    return save;
                }
            }
            UIManager.error("Error: World '" + input + "' does not exist.");
        }
    }

    // -------------------------------------------------------------------------
    // Settings
    // -------------------------------------------------------------------------

    private void settings() {
        while (true) {
            UIManager.clearScreen();
            UIManager.title("Settings");
            UIManager.option("1", Style.fit("Colours", 14) + toggle(Term.isColorEnabled()),
                    "turn off if you see odd symbols like [32m");
            UIManager.option("2", Style.fit("Animations", 14) + toggle(Term.isAnimationsEnabled()),
                    Term.isInteractive() ? "short scenes when your Sim does things" : "needs an interactive terminal");
            UIManager.option("0", "Back");
            UIManager.printMessage("\n  Preview: " + Panel.meter("Hunger", 75, 12) + "  " + Panel.meter("Energy", 20, 12));
            UIManager.prompt("\nToggle> ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                Term.setColorEnabled(!Term.isColorEnabled());
            } else if (choice.equals("2")) {
                Term.setAnimationsEnabled(!Term.isAnimationsEnabled());
            } else {
                return;
            }
        }
    }

    private static String toggle(boolean on) {
        return on ? "{G}[ ON  ]{/}" : "{k}[ OFF ]{/}";
    }
}
