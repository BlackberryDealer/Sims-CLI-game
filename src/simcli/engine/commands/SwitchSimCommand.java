package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.GameEngine;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

import java.util.List;
import java.util.Scanner;
import simcli.entities.models.SimState;

public class SwitchSimCommand implements ICommand {
    private final GameEngine engine;
    private final Scanner scanner;

    public SwitchSimCommand(GameEngine engine, Scanner scanner) {
        this.engine = engine;
        this.scanner = scanner;
    }

    @Override
    public CommandResult execute() {
        List<Sim> neighborhood = engine.getNeighborhood();
        UIManager.printMessage("\n=== Switch Sim ===");
        if (neighborhood.size() <= 1) {
            UIManager.printMessage("There is no one else in your household to switch to.");
            UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        }

        UIManager.printMessage("Current Household:");
        for (int i = 0; i < neighborhood.size(); i++) {
            Sim s = neighborhood.get(i);
            String status = s.getState() == SimState.DEAD ? "[DEAD]" : "[Alive]";
            String marker = (s == engine.getActivePlayer()) ? " (Current)" : "";
            UIManager.printMessage("[" + (i + 1) + "] " + s.getName() + " " + status + marker);
        }
        UIManager.printMessage("[0] Cancel");
        UIManager.prompt("Select Sim to control> ");

        try {
            int target = Integer.parseInt(scanner.nextLine().trim());
            if (target == 0) return CommandResult.NO_TICK;
            
            if (target > 0 && target <= neighborhood.size()) {
                Sim chosen = neighborhood.get(target - 1);
                if (chosen.getState() == SimState.DEAD) {
                    UIManager.printMessage(chosen.getName() + " is deceased. Let them rest.");
                    UIManager.prompt("\nPress ENTER to return...");
                    scanner.nextLine();
                    return CommandResult.NO_TICK;
                }
                
                engine.setActivePlayer(chosen);
                UIManager.printMessage("Switched control to " + chosen.getName() + ".");
                UIManager.prompt("\nPress ENTER to continue...");
                scanner.nextLine();
                
                return CommandResult.NO_TICK;
            } else {
                UIManager.printWarning("Invalid selection.");
                UIManager.prompt("\nPress ENTER to return...");
                scanner.nextLine();
                return CommandResult.NO_TICK;
            }

        } catch (NumberFormatException e) {
             UIManager.printWarning("Invalid input.");
             UIManager.prompt("\nPress ENTER to return...");
             scanner.nextLine();
             return CommandResult.NO_TICK;
        }
    }
}
