package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;

import java.util.List;
import java.util.Scanner;

public class UpgradeRoomCommand extends BaseCommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final Building currentLocation;

    public UpgradeRoomCommand(Sim activePlayer, Scanner scanner, Building currentLocation) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.currentLocation = currentLocation;
    }

    @Override
    public CommandResult execute() {
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
                int rChoice = Integer.parseInt(scanner.nextLine().trim());
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
        pause(scanner);
        return CommandResult.NO_TICK;
    }

}
