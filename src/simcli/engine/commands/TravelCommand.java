package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.AsciiArt;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.List;

/**
 * Command that travels between buildings on the city map.
 *
 * <p>Displays all locations (with for-sale tags on unowned residential
 * properties), allows the player to purchase a property on arrival, and
 * triggers the travel animation upon successful movement.</p>
 */
public class TravelCommand extends BaseCommand {

    /**
     * Constructs a {@code TravelCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public TravelCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Presents the destination menu, handles optional property purchase,
     * and moves the player to the selected building.
     *
     * @return {@link CommandResult#TICK_FORWARD} after successful travel,
     *         {@link CommandResult#NO_TICK} if cancelled or already at destination.
     */
    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();
        List<Building> cityMap = ctx.getWorldManager().getCityMap();

        UIManager.printMessage("\nAvailable Locations:");
        for (int i = 0; i < cityMap.size(); i++) {
            Building b = cityMap.get(i);
            String label = b.getName();
            // Show "FOR SALE" tag for unowned residential properties
            if (b instanceof Residential) {
                Residential res = (Residential) b;
                if (!res.isOwned()) {
                    label += " [FOR SALE - $" + res.getPurchasePrice() + "]";
                }
            }
            UIManager.printMessage("[" + (i + 1) + "] " + label);
        }
        UIManager.printMessage("[0] Cancel");
        UIManager.prompt("Select destination> ");

        try {
            int destStr = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (destStr == 0) {
                return CommandResult.NO_TICK;
            } else if (destStr > 0 && destStr <= cityMap.size()) {
                Building target = cityMap.get(destStr - 1);
                if (currentLocation == target) {
                    UIManager.printMessage("You are already at " + target.getName() + "!");
                    pause();
                    return CommandResult.NO_TICK;
                }

                // Check if target is an unowned residential — prompt to purchase
                if (target instanceof Residential) {
                    Residential res = (Residential) target;
                    if (!res.isOwned()) {
                        UIManager.printMessage("\n'" + res.getName() + "' is for sale at $"
                                + res.getPurchasePrice() + ". You have $" + activePlayer.getMoney() + ".");
                        UIManager.prompt("Would you like to buy it? (Y/N)> ");
                        String answer = ctx.getScanner().nextLine().trim();
                        if (answer.equalsIgnoreCase("Y")) {
                            if (res.purchase(activePlayer)) {
                                UIManager.printMessage("\n*** CONGRATULATIONS! You purchased " + res.getName() + "! ***");
                            } else {
                                UIManager.printMessage("Not enough money! You need $" + res.getPurchasePrice() + ".");
                                pause();
                                return CommandResult.NO_TICK;
                            }
                        } else {
                            UIManager.printMessage("Maybe next time.");
                            pause();
                            return CommandResult.NO_TICK;
                        }
                    }
                }

                ctx.getWorldManager().setCurrentLocation(target);
                AsciiArt.printTravelAnimation();
                target.enter(activePlayer);
                return CommandResult.TICK_FORWARD;
            } else {
                UIManager.printMessage("Invalid destination.");
                pause();
                return CommandResult.NO_TICK;
            }
        } catch (NumberFormatException e) {
            UIManager.printMessage("Invalid destination.");
            pause();
            return CommandResult.NO_TICK;
        }
    }
}
