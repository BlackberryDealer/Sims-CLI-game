package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.entities.actors.Sim;
import simcli.entities.items.Furniture;
import simcli.entities.items.Item;
import simcli.ui.ItemInfo;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

import java.util.List;

// Manages the Sim's inventory with pagination, item use, and furniture placement.
// Uses instanceof to branch between Furniture (placeable) and other items (consumable).
// The loop runs until the player backs out — time does not advance.
public class InventoryCommand extends BaseCommand {

    /**
     * Constructs an {@code InventoryCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public InventoryCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Runs the interactive inventory management loop.
     *
     * @return {@link CommandResult#NO_TICK} — inventory management does not
     *         advance time, though individual item uses may have effects.
     * @throws SimulationException  propagated from item interactions.
     * @throws SleepEventException  propagated from item interactions.
     */
    @Override
    protected CommandResult run() throws SimulationException, SleepEventException {
        Sim activePlayer = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();

        boolean managingInventory = true;
        int pageSize = 10;
        int currentPage = 0;

        while (managingInventory) {
            List<Item> inv = activePlayer.getInventory();
            int inventorySize = inv.size();
            int totalPages = (int) Math.ceil((double) inventorySize / pageSize);
            if (totalPages == 0)
                totalPages = 1;
            if (currentPage >= totalPages)
                currentPage = totalPages - 1;

            String pages = totalPages > 1 ? "  {k}page " + (currentPage + 1) + "/" + totalPages + "{/}" : "";
            UIManager.title(activePlayer.getName() + "'s bag  " + inventorySize + "/"
                    + activePlayer.getInventoryCapacity() + pages);

            if (inv.isEmpty()) {
                UIManager.info("Your inventory is empty. {k}Shops sell food, books and furniture.{/}");
                managingInventory = false;
                break;
            }

            int startIdx = currentPage * pageSize;
            int endIdx = Math.min(startIdx + pageSize, inventorySize);

            for (int i = startIdx; i < endIdx; i++) {
                Item item = inv.get(i);
                UIManager.option(String.valueOf(i - startIdx + 1),
                        Style.fit(item.getObjectName(), 18) + ItemInfo.describe(item));
            }

            System.out.println();
            if (inventorySize > pageSize) {
                UIManager.option("N", "Next page", null);
                UIManager.option("P", "Previous page", null);
            }
            UIManager.option("0", "Back");
            UIManager.prompt("\nUse or place> ");

            String invInput = ctx.getScanner().nextLine().trim().toUpperCase();

            if (invInput.equals("0")) {
                managingInventory = false;
            } else if (invInput.equals("N") && inventorySize > pageSize) {
                if (currentPage < totalPages - 1)
                    currentPage++;
            } else if (invInput.equals("P") && inventorySize > pageSize) {
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
                            placeFurniture(activePlayer, (Furniture) selectedItem);
                        } else {
                            // Call the interaction on the item natively (so Consume propagates appropriately).
                            // A rejected use (e.g. furniture away from home) keeps the player in the bag.
                            try {
                                selectedItem.interact(activePlayer, ctx.getScanner(), ctx.getTimeManager());
                            } catch (SleepEventException e) {
                                throw e;
                            } catch (SimulationException e) {
                                UIManager.error(e.getMessage());
                            }
                        }
                    } else {
                        UIManager.error("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    UIManager.error("Invalid input.");
                }
            }
        }
        pause();
        return CommandResult.NO_TICK;
    }

    /**
     * Places furniture in the room the Sim is standing in, after a quick
     * confirmation showing how much space it takes.
     */
    private void placeFurniture(Sim sim, Furniture furn) {
        Room room = sim.getCurrentRoom();
        if (room == null) {
            UIManager.error("Step into a room first (use [M] to move between rooms).");
            return;
        }
        UIManager.info("Place {W}" + furn.getObjectName() + "{/} in the {W}" + room.getName() + "{/}? It needs "
                + furn.getSpaceScore() + " space; " + (room.getMaxCapacity() - room.getUsedCapacity()) + " is free.");
        UIManager.prompt("Place it here? (Y/N)> ");
        if (!ctx.getScanner().nextLine().trim().equalsIgnoreCase("Y")) {
            UIManager.info("Kept in your bag.");
            return;
        }
        if (!room.canFit(furn)) {
            UIManager.error("Not enough space in " + room.getName() + "! Upgrade the room with [U] or try another room.");
            return;
        }
        Interactable instance = furn.createInteractable();
        if (instance == null) {
            UIManager.error(furn.getObjectName() + " can't be placed.");
            return;
        }
        room.placeFurniture(instance, furn.getSpaceScore());
        sim.getInventory().remove(furn);
        UIManager.success("Placed " + furn.getObjectName() + " in the " + room.getName() + "!");
    }
}
