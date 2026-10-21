package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;

import java.util.List;

/**
 * Command that moves the active Sim between rooms inside a residential building.
 *
 * <p>Only available when the current location is residential. Displays all
 * rooms with a current-room marker and lets the player select a destination.</p>
 */
public class MoveRoomCommand extends BaseCommand {

    /**
     * Constructs a {@code MoveRoomCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public MoveRoomCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Presents the room selection menu and moves the Sim if valid.
     *
     * @return {@link CommandResult#NO_TICK} — moving between rooms does not advance time.
     */
    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();

        if (currentLocation.isResidential()) {
            Residential res = (Residential) currentLocation;
            UIManager.title("Go to which room?");
            List<Room> rooms = res.getRooms();
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                String contents = HouseInfoCommand.contents(room);
                String here = (room == activePlayer.getCurrentRoom()) ? "  {G}" + Panel.ARROW + " you are here{/}" : "";
                UIManager.option(String.valueOf(i + 1), Style.fit(room.getName(), 18) + contents + here);
            }
            UIManager.option("0", "Stay here");
            UIManager.prompt("\nRoom> ");
            try {
                String input = ctx.getScanner().nextLine().trim();
                if (input.isEmpty() || input.equals("0")) {
                    return CommandResult.NO_TICK;
                }
                int rChoice = Integer.parseInt(input);
                if (rChoice > 0 && rChoice <= rooms.size()) {
                    Room selectedRoom = rooms.get(rChoice - 1);
                    if (selectedRoom == activePlayer.getCurrentRoom()) {
                        UIManager.info("You are already in the " + selectedRoom.getName() + "!");
                    } else {
                        // moving is instant; the next screen shows the new room
                        activePlayer.setCurrentRoom(selectedRoom);
                        return CommandResult.NO_TICK;
                    }
                } else {
                    UIManager.error("Invalid room.");
                }
            } catch (NumberFormatException e) {
                UIManager.error("Invalid input.");
            }
        } else {
            UIManager.info("You can only move between rooms at home!");
        }
        pause();
        return CommandResult.NO_TICK;
    }
}
