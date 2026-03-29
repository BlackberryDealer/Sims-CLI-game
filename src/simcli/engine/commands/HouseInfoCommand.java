package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

/**
 * Command that displays information about the current residential building.
 *
 * <p>Lists every room with its capacity usage and the furniture placed inside.
 * Only works when the player is inside a residential building; displays a
 * warning otherwise.</p>
 */
public class HouseInfoCommand extends BaseCommand {

    /**
     * Constructs a {@code HouseInfoCommand} with the given context.
     *
     * @param ctx shared command context providing access to the current location.
     */
    public HouseInfoCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Prints the room layout and furniture of the current residential building.
     *
     * @return {@link CommandResult#NO_TICK} — viewing house info does not advance time.
     */
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
