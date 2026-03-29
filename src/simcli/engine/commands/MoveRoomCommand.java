package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;

import java.util.List;

public class MoveRoomCommand extends BaseCommand {

    public MoveRoomCommand(CommandContext ctx) {
        super(ctx);
    }

    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();

        if (currentLocation.isResidential()) {
            Residential res = (Residential) currentLocation;
            UIManager.printMessage("\n=== MOVE ROOM ===");
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                String currentTag = (room == activePlayer.getCurrentRoom()) ? " (Current)" : "";
                UIManager.printMessage("[" + (i + 1) + "] " + room.getName() + currentTag);
            }
            UIManager.printMessage("[0] Cancel");
            UIManager.prompt("Select Room> ");
            try {
                int rChoice = Integer.parseInt(ctx.getScanner().nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    Room selectedRoom = rooms.get(rChoice - 1);
                    if (selectedRoom == activePlayer.getCurrentRoom()) {
                        UIManager.printMessage("You are already in " + selectedRoom.getName() + "!");
                    } else {
                        activePlayer.setCurrentRoom(selectedRoom);
                        UIManager.printMessage("Moved to " + activePlayer.getCurrentRoom().getName() + ".");
                    }
                }
            } catch (NumberFormatException e) {
                UIManager.printMessage("Invalid input.");
            }
        } else {
            UIManager.printMessage("You can only move between rooms at home!");
        }
        pause();
        return CommandResult.NO_TICK;
    }
}
