package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

import java.util.Scanner;

public class HouseInfoCommand extends BaseCommand {
    private final Scanner scanner;
    private final Building currentLocation;

    public HouseInfoCommand(Scanner scanner, Building currentLocation) {
        this.scanner = scanner;
        this.currentLocation = currentLocation;
    }

    @Override
    public CommandResult execute() {
        if (currentLocation.isResidential()) {
            Residential res = (Residential) currentLocation;
            UIManager.printMessage("\n=== HOUSE INFO: " + res.getName() + " ===");
            for (Room r : res.getRooms()) {
                UIManager.printMessage("- " + r.getName() + " (Capacity: " + r.getUsedCapacity() + "/"
                        + r.getMaxCapacity() + ")");
                for (Interactable it : r.getInteractables()) {
                    UIManager.printMessage("    => " + it.getObjectName());
                }
            }
            UIManager.printMessage("======================================");
        } else {
            UIManager.printMessage("You can only inspect residential buildings.");
        }
        pause(scanner);
        return CommandResult.NO_TICK;
    }

}
