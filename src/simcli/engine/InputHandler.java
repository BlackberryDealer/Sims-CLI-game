package simcli.engine;

import simcli.engine.commands.*;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;

import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

/**
 * Processes raw player input and dispatches to the appropriate command.
 * Builds a {@link CommandContext} per invocation to keep command constructors clean.
 */
public class InputHandler implements IInputHandler {
    private final IWorldManager worldManager;
    private final TimeManager timeManager;
    private final IGameEngine engine;

    public InputHandler(IWorldManager worldManager, TimeManager timeManager, IGameEngine engine) {
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

        // Build a shared context for all commands
        CommandContext ctx = new CommandContext(
                activePlayer, scanner, timeManager, worldManager, currentLocation, engine);

        try {
            ICommand command = null;

            switch (input) {
                case "W":
                    command = new WorkCommand(ctx);
                    break;
                case "J":
                    command = new JobMarketCommand(ctx);
                    break;
                case "T":
                    command = new TravelCommand(ctx);
                    break;
                case "M":
                    command = new MoveRoomCommand(ctx);
                    break;
                case "H":
                    command = new HouseInfoCommand(ctx);
                    break;
                case "I":
                    command = new CharacterStatusCommand(ctx);
                    break;
                case "V":
                    command = new InventoryCommand(ctx);
                    break;
                case "U":
                    command = new UpgradeRoomCommand(ctx);
                    break;
                case "S":
                    return CommandResult.SAVE_AND_EXIT;
                case "K":
                    command = new SwitchSimCommand(ctx);
                    break;
                case "L":
                    command = new SpouseInteractionCommand(ctx);
                    break;
                default:
                    int choice = Integer.parseInt(input) - 1;
                    command = new InteractCommand(ctx, items, choice);
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
