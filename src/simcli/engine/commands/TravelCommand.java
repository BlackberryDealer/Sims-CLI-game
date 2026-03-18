package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.IWorldManager;
import simcli.entities.Sim;
import simcli.ui.AsciiArt;
import simcli.ui.UIManager;
import simcli.world.Building;

import java.util.List;
import java.util.Scanner;

public class TravelCommand implements ICommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final Building currentLocation;
    private final IWorldManager worldManager;

    public TravelCommand(Sim activePlayer, Scanner scanner, Building currentLocation, IWorldManager worldManager) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.currentLocation = currentLocation;
        this.worldManager = worldManager;
    }

    @Override
    public CommandResult execute() {
        List<Building> cityMap = worldManager.getCityMap();
        UIManager.printMessage("\nAvailable Locations:");
        for (int i = 0; i < cityMap.size(); i++) {
            UIManager.printMessage("[" + (i + 1) + "] " + cityMap.get(i).getName());
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
                    pause();
                    return CommandResult.NO_TICK;
                } else {
                    worldManager.setCurrentLocation(target);
                    AsciiArt.printTravelAnimation();
                    target.enter(activePlayer);
                    return CommandResult.TICK_FORWARD;
                }
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

    private void pause() {
        UIManager.prompt("\nPress ENTER to return...");
        scanner.nextLine();
    }
}
