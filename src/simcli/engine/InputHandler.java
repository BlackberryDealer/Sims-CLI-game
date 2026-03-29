package simcli.engine;

import simcli.engine.commands.*;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Factory and dispatcher: maps raw user input to the right Command object.
 *
 * <p>Builds one shared {@link CommandContext} per turn, then instantiates
 * the matching command. Never holds a reference to {@code GameEngine} —
 * only the specific dependencies it needs (Dependency Inversion).</p>
 */
public class InputHandler implements IInputHandler {
    private final IWorldManager worldManager;
    private final TimeManager timeManager;
    private final List<Sim> neighborhood;
    private final Consumer<Sim> setActivePlayer;
    private final SimulationLogger logger;

    /**
     * Creates a new InputHandler.
     *
     * @param worldManager    provides current location and city map.
     * @param timeManager     provides time/day information.
     * @param neighborhood    the full list of Sims in the household.
     * @param setActivePlayer callback to switch the active player on the engine.
     * @param logger          the simulation logger for command messages.
     */
    public InputHandler(IWorldManager worldManager, TimeManager timeManager,
            List<Sim> neighborhood, Consumer<Sim> setActivePlayer,
            SimulationLogger logger) {
        this.worldManager = worldManager;
        this.timeManager = timeManager;
        this.neighborhood = neighborhood;
        this.setActivePlayer = setActivePlayer;
        this.logger = logger;
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

        // one context shared by whichever command we instantiate below
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(activePlayer)
                .neighborhood(neighborhood)
                .scanner(scanner)
                .timeManager(timeManager)
                .worldManager(worldManager)
                .currentLocation(currentLocation)
                .availableItems(items)
                .setActivePlayer(setActivePlayer)
                .logger(logger)
                .build();

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
                    command = new InteractCommand(ctx, choice);
                    break;
            }

            // polymorphic dispatch — every command handles execute() the same way
            return command.execute();

            // exceptions become results — keeps the game loop clean
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
