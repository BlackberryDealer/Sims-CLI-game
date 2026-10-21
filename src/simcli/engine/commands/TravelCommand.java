package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.AsciiArt;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Park;
import simcli.world.Residential;

import java.util.List;

// Handles moving between buildings on the city map.
// Uses instanceof to check if a destination is Residential
// (polymorphism — the command treats buildings generically, only downcasting when needed).
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

        UIManager.title("Where to?");
        for (int i = 0; i < cityMap.size(); i++) {
            Building b = cityMap.get(i);
            String label = Style.fit(b.getName(), 20);
            String hint = describe(b);
            // Show "FOR SALE" tag for unowned residential properties
            if (b instanceof Residential && !((Residential) b).isOwned()) {
                hint = "{Y}for sale - $" + ((Residential) b).getPurchasePrice() + "{/}";
            }
            if (b == currentLocation) {
                hint += "  {G}" + Panel.ARROW + " you are here{/}";
            }
            UIManager.option(String.valueOf(i + 1), label + hint);
        }
        UIManager.option("0", "Stay here");
        UIManager.prompt("\nDestination> ");

        try {
            int destStr = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (destStr == 0) {
                return CommandResult.NO_TICK;
            } else if (destStr > 0 && destStr <= cityMap.size()) {
                Building target = cityMap.get(destStr - 1);
                if (currentLocation == target) {
                    UIManager.info("You are already at " + target.getName() + "!");
                    pause();
                    return CommandResult.NO_TICK;
                }

                // downcast only when we actually need Residential-specific behaviour
                if (target instanceof Residential) {
                    Residential res = (Residential) target;
                    if (!res.isOwned()) {
                        System.out.println();
                        UIManager.info("{W}" + res.getName() + "{/} is for sale at {Y}$" + res.getPurchasePrice()
                                + "{/}. You have {G}$" + activePlayer.getMoney() + "{/}.");
                        UIManager.prompt("Buy it? (Y/N)> ");
                        String answer = ctx.getScanner().nextLine().trim();
                        if (answer.equalsIgnoreCase("Y")) {
                            if (res.purchase(activePlayer)) {
                                UIManager.printEvent("*** CONGRATULATIONS! You bought " + res.getName() + "! ***");
                            } else {
                                UIManager.error("Not enough money! You need $" + res.getPurchasePrice() + ".");
                                pause();
                                return CommandResult.NO_TICK;
                            }
                        } else {
                            UIManager.info("Maybe next time.");
                            pause();
                            return CommandResult.NO_TICK;
                        }
                    }
                }

                ctx.getWorldManager().setCurrentLocation(target);
                AsciiArt.printTravelAnimation(target.getName());
                target.enter(activePlayer);
                return CommandResult.TICK_FORWARD;
            } else {
                UIManager.error("Invalid destination.");
                pause();
                return CommandResult.NO_TICK;
            }
        } catch (NumberFormatException e) {
            UIManager.error("Invalid destination.");
            pause();
            return CommandResult.NO_TICK;
        }
    }

    /** A short note on what each kind of place is for. */
    private static String describe(Building b) {
        if (b instanceof Residential) {
            return "{k}home - sleep, eat, shower{/}";
        }
        if (b instanceof Park) {
            return "{k}meet people, make friends{/}";
        }
        String name = b.getName().toLowerCase();
        if (name.contains("book")) {
            return "{k}books, a computer, energy drinks{/}";
        }
        if (name.contains("market")) {
            return "{k}food and furniture{/}";
        }
        return "";
    }
}
