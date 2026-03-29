package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

public class HouseInfoCommand extends BaseCommand {

    public HouseInfoCommand(CommandContext ctx) {
        super(ctx);
    }

    @Override
    protected CommandResult run() {
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
        pause();
        return CommandResult.NO_TICK;
    }
}
