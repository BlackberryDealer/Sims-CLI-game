package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;

import java.util.List;

/**
 * Command that upgrades a room's capacity in the current residential building.
 *
 * <p>Displays each room's current capacity and charges a fixed upgrade cost
 * (from {@link GameConstants#UPGRADE_COST}) to add bonus capacity
 * ({@link GameConstants#UPGRADE_CAPACITY_BONUS}). Only available when inside
 * a residential building.</p>
 */
public class UpgradeRoomCommand extends BaseCommand {

    /**
     * Constructs an {@code UpgradeRoomCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public UpgradeRoomCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Presents the room upgrade menu and processes the purchase.
     *
     * @return {@link CommandResult#NO_TICK} — upgrading does not advance time.
     */
    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();

        if (currentLocation.isResidential()) {
            Residential res = (Residential) currentLocation;
            UIManager.printMessage("\n=== UPGRADE ROOM ===");
            UIManager.printMessage("Cost: $" + GameConstants.UPGRADE_COST + " for +" + GameConstants.UPGRADE_CAPACITY_BONUS + " Capacity");
            UIManager.printMessage("Your Cash: $" + activePlayer.getMoney());
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                Room r = rooms.get(i);
                UIManager.printMessage("[" + (i + 1) + "] " + r.getName() + " (Capacity: " + r.getUsedCapacity()
                        + "/" + r.getMaxCapacity() + ")");
            }
            UIManager.printMessage("[0] Cancel");
            UIManager.prompt("Room> ");
            try {
                int rChoice = Integer.parseInt(ctx.getScanner().nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    Room targetRoom = rooms.get(rChoice - 1);
                    targetRoom.upgradeCapacity(activePlayer, GameConstants.UPGRADE_CAPACITY_BONUS, GameConstants.UPGRADE_COST);
                }
            } catch (Exception e) {
                UIManager.printMessage("Invalid selection.");
            }
        } else {
            UIManager.printMessage("You can only upgrade rooms at home!");
        }
        pause();
        return CommandResult.NO_TICK;
    }
}
