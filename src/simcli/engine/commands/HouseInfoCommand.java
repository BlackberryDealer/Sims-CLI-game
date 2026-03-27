package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

import java.util.Scanner;

public class HouseInfoCommand extends BaseCommand {
    private final CommandContext ctx;

    public HouseInfoCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected CommandResult run() {
        Scanner scanner = ctx.getScanner();
        Building currentLocation = ctx.getCurrentLocation();

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
