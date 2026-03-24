package simcli.engine;

import simcli.engine.commands.*;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;

import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

public class InputHandler implements IInputHandler {
    private final IWorldManager worldManager;
    private final TimeManager timeManager;

    private final GameEngine engine;

    public InputHandler(IWorldManager worldManager, TimeManager timeManager, GameEngine engine) {
        this.worldManager = worldManager;
        this.timeManager = timeManager;
        this.engine = engine;
    }

    @Override
    public CommandResult handle(String input, Sim activePlayer, Scanner scanner) {
        Building currentLocation = worldManager.getCurrentLocation();
        List<Interactable> items;

        if (currentLocation.isResidential() && activePlayer.getCurrentRoom() != null) {
            items = activePlayer.getCurrentRoom().getInteractables();
        } else {
            items = currentLocation.getInteractables();
        }

        try {
            ICommand command = null;

            switch (input) {
                case "W":
                    command = new WorkCommand(activePlayer, scanner, timeManager);
                    break;
                case "J":
                    command = new JobMarketCommand(activePlayer, scanner);
                    break;
                case "T":
                    command = new TravelCommand(activePlayer, scanner, currentLocation, worldManager);
                    break;
                case "M":
                    command = new MoveRoomCommand(activePlayer, scanner, currentLocation);
                    break;
                case "H":
                    command = new HouseInfoCommand(scanner, currentLocation);
                    break;
                case "I":
                    command = new CharacterStatusCommand(activePlayer, scanner, currentLocation);
                    break;
                case "V":
                    command = new InventoryCommand(activePlayer, scanner, currentLocation, timeManager);
                    break;
                case "U":
                    command = new UpgradeRoomCommand(activePlayer, scanner, currentLocation);
                    break;
                case "S":
                    return CommandResult.SAVE_AND_EXIT;
                case "K":
                    command = new SwitchSimCommand(engine, scanner);
                    break;
                case "L":
                    command = new SpouseInteractionCommand(engine, scanner);
                    break;
                default:
                    int choice = Integer.parseInt(input) - 1;
                    command = new InteractCommand(activePlayer, scanner, timeManager, items, choice);
                    break;
            }

            return command.execute();

        } catch (SleepEventException e) {
            return CommandResult.SLEEP_EVENT;
        } catch (SimulationException e) {
            UIManager.printWarning("ACTION REJECTED: " + e.getMessage());
            UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        } catch (NumberFormatException e) {
            UIManager.printWarning("Invalid input.");
            UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        }

    }

}
