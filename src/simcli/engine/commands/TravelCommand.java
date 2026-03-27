package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.IWorldManager;
import simcli.entities.actors.Sim;
import simcli.ui.AsciiArt;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.List;
import java.util.Scanner;

public class TravelCommand extends BaseCommand {
    private final CommandContext ctx;

    public TravelCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Scanner scanner = ctx.getScanner();
        Building currentLocation = ctx.getCurrentLocation();
        IWorldManager worldManager = ctx.getWorldManager();

        List<Building> cityMap = worldManager.getCityMap();
        UIManager.printMessage("\nAvailable Locations:");
        for (int i = 0; i < cityMap.size(); i++) {
            Building b = cityMap.get(i);
            String label = b.getName();
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
            int destStr = Integer.parseInt(scanner.nextLine().trim());
            if (destStr == 0) {
                return CommandResult.NO_TICK;
            } else if (destStr > 0 && destStr <= cityMap.size()) {
                Building target = cityMap.get(destStr - 1);
                if (currentLocation == target) {
                    UIManager.printMessage("You are already at " + target.getName() + "!");
                    pause(scanner);
                    return CommandResult.NO_TICK;
                }

                if (target instanceof Residential) {
                    Residential res = (Residential) target;
                    if (!res.isOwned()) {
                        UIManager.printMessage("\n'" + res.getName() + "' is for sale at $"
                                + res.getPurchasePrice() + ". You have $" + activePlayer.getMoney() + ".");
                        UIManager.prompt("Would you like to buy it? (Y/N)> ");
                        String answer = scanner.nextLine().trim();
                        if (answer.equalsIgnoreCase("Y")) {
                            if (res.purchase(activePlayer)) {
                                UIManager.printMessage("\n*** CONGRATULATIONS! You purchased " + res.getName() + "! ***");
                            } else {
                                UIManager.printMessage("Not enough money! You need $" + res.getPurchasePrice() + ".");
                                pause(scanner);
                                return CommandResult.NO_TICK;
                            }
                        } else {
                            UIManager.printMessage("Maybe next time.");
                            pause(scanner);
                            return CommandResult.NO_TICK;
                        }
                    }
                }

                worldManager.setCurrentLocation(target);
                AsciiArt.printTravelAnimation();
                target.enter(activePlayer);
                return CommandResult.TICK_FORWARD;
            } else {
                UIManager.printMessage("Invalid destination.");
                pause(scanner);
                return CommandResult.NO_TICK;
            }
        } catch (NumberFormatException e) {
            UIManager.printMessage("Invalid destination.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }
    }
}
