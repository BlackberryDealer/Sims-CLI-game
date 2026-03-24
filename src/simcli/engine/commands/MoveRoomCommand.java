package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;

import java.util.List;
import java.util.Scanner;

public class MoveRoomCommand extends BaseCommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final Building currentLocation;

    public MoveRoomCommand(Sim activePlayer, Scanner scanner, Building currentLocation) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.currentLocation = currentLocation;
    }

    @Override
    public CommandResult execute() {
        if (currentLocation.isResidential()) {
            Residential res = (Residential) currentLocation;
            UIManager.printMessage("\n=== MOVE ROOM ===");
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                UIManager.printMessage("[" + (i + 1) + "] " + rooms.get(i).getName());
            }
            UIManager.printMessage("[0] Cancel");
            UIManager.prompt("Select Room> ");
            try {
                int rChoice = Integer.parseInt(scanner.nextLine().trim());
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    activePlayer.setCurrentRoom(rooms.get(rChoice - 1));
                    UIManager.printMessage("Moved to " + activePlayer.getCurrentRoom().getName() + ".");
                }
            } catch (NumberFormatException e) {
                UIManager.printMessage("Invalid input.");
            }
        } else {
            UIManager.printMessage("You can only move between rooms at home!");
        }
        pause(scanner);
        return CommandResult.NO_TICK;
    }

}
