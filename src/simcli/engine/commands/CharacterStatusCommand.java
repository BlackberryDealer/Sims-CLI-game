package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;

import java.util.Scanner;

public class CharacterStatusCommand extends BaseCommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final Building currentLocation;

    public CharacterStatusCommand(Sim activePlayer, Scanner scanner, Building currentLocation) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.currentLocation = currentLocation;
    }

    @Override
    protected CommandResult run() {
        UIManager.printMessage("\n=== CHARACTER STATUS ===");
        UIManager.printMessage("Name: " + activePlayer.getName());
        UIManager.printMessage("Age: " + activePlayer.getAge());
        UIManager.printMessage("Traits: " + activePlayer.getTraits());
        UIManager.printMessage("Money: $" + activePlayer.getMoney());
        if (activePlayer.canWork()) {
            UIManager.printMessage("Job: " + activePlayer.getCareer().getTitle() + " (Tier " + activePlayer.getJobTier() + ")");
            UIManager.printMessage("Salary: $" + activePlayer.getCareer().getSalaryAtTier(activePlayer.getJobTier()) + " per shift");
        }
        UIManager.printMessage("Health: " + activePlayer.getHealth() + "%");
        UIManager.printMessage("Hunger: " + activePlayer.getHunger().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("Energy: " + activePlayer.getEnergy().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("Hygiene: " + activePlayer.getHygiene().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("Happiness: " + activePlayer.getHappiness().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage("Social: " + activePlayer.getSocial().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
        UIManager.printMessage(
                "Inventory Items: " + activePlayer.getInventory().size() + " / " + activePlayer.getInventoryCapacity());
        UIManager.printMessage("Location: " + currentLocation.getName());
        if (activePlayer.getRelationshipManager().getSpouse() != null) {
            UIManager.printMessage("Spouse: " + activePlayer.getRelationshipManager().getSpouse().getName());
        }
        if (!activePlayer.getRelationshipManager().getChildren().isEmpty()) {
            StringBuilder childInfo = new StringBuilder("Children: ");
            for (simcli.entities.actors.Sim child : activePlayer.getRelationshipManager().getChildren()) {
                childInfo.append(child.getName()).append(" (Age: ").append(child.getAge())
                         .append(", ").append(child.isPlayable() ? "Playable" : "Not Playable")
                         .append(") ");
            }
            UIManager.printMessage(childInfo.toString().trim());
        }
        UIManager.printMessage("==================");
        
        pause(scanner);
        return CommandResult.NO_TICK;
    }

}
