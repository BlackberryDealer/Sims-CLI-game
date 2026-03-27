package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Furniture;
import simcli.entities.items.Item;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

public class InventoryCommand extends BaseCommand {
    private final CommandContext ctx;

    public InventoryCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected CommandResult run() throws SimulationException, SleepEventException {
        Sim activePlayer = ctx.getActivePlayer();
        Scanner scanner = ctx.getScanner();
        Building currentLocation = ctx.getCurrentLocation();
        TimeManager timeManager = ctx.getTimeManager();

        boolean managingInventory = true;
        int pageSize = 10;
        int currentPage = 0;

        while (managingInventory) {
            List<Item> inv = activePlayer.getInventory();
            int totalPages = (int) Math.ceil((double) inv.size() / pageSize);
            if (totalPages == 0)
                totalPages = 1;
            if (currentPage >= totalPages)
                currentPage = totalPages - 1;

            UIManager.printMessage("\n=== INVENTORY (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
            UIManager.printMessage("Capacity: " + inv.size() + " / " + activePlayer.getInventoryCapacity());

            if (inv.isEmpty()) {
                UIManager.printMessage("Your inventory is empty.");
                managingInventory = false;
                break;
            }

            int startIdx = currentPage * pageSize;
            int endIdx = Math.min(startIdx + pageSize, inv.size());

            for (int i = startIdx; i < endIdx; i++) {
                UIManager.printMessage("[" + (i - startIdx + 1) + "] " + inv.get(i).getObjectName());
            }

            UIManager.printMessage("\n[N] Next Page   [P] Previous Page");
            UIManager.printMessage("[0] Back");
            UIManager.prompt("Select item to Use/Place> ");

            String invInput = scanner.nextLine().trim().toUpperCase();

            if (invInput.equals("0")) {
                managingInventory = false;
            } else if (invInput.equals("N")) {
                if (currentPage < totalPages - 1)
                    currentPage++;
            } else if (invInput.equals("P")) {
                if (currentPage > 0)
                    currentPage--;
            } else {
                try {
                    int invChoice = Integer.parseInt(invInput);
                    if (invChoice > 0 && invChoice <= (endIdx - startIdx)) {
                        int realIndex = startIdx + invChoice - 1;
                        Item selectedItem = inv.get(realIndex);

                        if (selectedItem instanceof Furniture
                                && currentLocation.isResidential()) {
                            Furniture furn = (Furniture) selectedItem;
                            Residential res = (Residential) currentLocation;
                            UIManager.printMessage("Select a room to place " + furn.getObjectName() + " (Requires "
                                    + furn.getSpaceScore() + " space):");
                            List<Room> rooms = res.getRooms();
                            for (int i = 0; i < rooms.size(); i++) {
                                Room r = rooms.get(i);
                                UIManager.printMessage("[" + (i + 1) + "] " + r.getName() + " (Capacity: "
                                        + r.getUsedCapacity() + "/" + r.getMaxCapacity() + ")");
                            }
                            UIManager.printMessage("[0] Cancel");
                            UIManager.prompt("Room> ");
                            int rChoice = Integer.parseInt(scanner.nextLine().trim());
                            if (rChoice > 0 && rChoice <= rooms.size()) {
                                Room targetRoom = rooms.get(rChoice - 1);
                                if (targetRoom != activePlayer.getCurrentRoom()) {
                                    UIManager.printMessage("You can only place furniture in the room you are currently in!");
                                } else if (targetRoom.canFit(furn)) {
                                    Interactable instance = furn.createInteractable();
                                    if (instance != null) {
                                        targetRoom.placeFurniture(instance, furn.getSpaceScore());
                                        activePlayer.getInventory().remove(selectedItem);
                                        UIManager.printMessage("Placed " + furn.getObjectName() + " in "
                                                + targetRoom.getName() + "!");
                                    }
                                } else {
                                    UIManager.printMessage("Not enough space in " + targetRoom.getName() + "!");
                                }
                            }
                        } else {
                            selectedItem.interact(activePlayer, scanner, timeManager);
                        }
                    } else {
                        UIManager.printMessage("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    UIManager.printMessage("Invalid input.");
                }
            }
        }
        pause(scanner);
        return CommandResult.NO_TICK;
    }

}
