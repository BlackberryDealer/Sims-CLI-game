package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.IGameEngine;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.Scanner;

/**
 * Command to interact with a spouse from anywhere if they are in the household.
 * Supports dating, having babies, and feeding babies.
 */
public class SpouseInteractionCommand extends BaseCommand {
    private final CommandContext ctx;

    public SpouseInteractionCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected CommandResult run() throws SimulationException {
        IGameEngine engine = ctx.getEngine();
        Sim activePlayer = ctx.getActivePlayer();
        Scanner scanner = ctx.getScanner();
        Sim spouse = activePlayer.getRelationshipManager().getSpouse();

        if (spouse == null) {
            UIManager.printMessage("You are not married. Find someone special at the park first!");
            pause(scanner);
            return CommandResult.NO_TICK;
        }

        if (!engine.getNeighborhood().contains(spouse)) {
            UIManager.printMessage(spouse.getName() + " is currently busy and not at home.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }

        UIManager.printMessage("\n=== Marriage Options ===");
        UIManager.printMessage("[1] Interact with Spouse (Date)");
        UIManager.printMessage("[2] Have a Baby");

        boolean hasBabies = hasBabiesInHousehold(engine);
        if (hasBabies) {
            UIManager.printMessage("[3] Feed Babies");
        }

        UIManager.printMessage("[0] Back");
        UIManager.prompt("Select action> ");

        try {
            String input = scanner.nextLine().trim();
            if (input.equals("0")) return CommandResult.NO_TICK;

            int choice = Integer.parseInt(input);
            if (choice == 1) {
                handleDate(activePlayer, spouse);
                return CommandResult.TICK_FORWARD;
            } else if (choice == 2) {
                return handleHaveBaby(activePlayer, engine, scanner);
            } else if (choice == 3 && hasBabies) {
                handleFeedBabies(activePlayer, engine);
                return CommandResult.TICK_FORWARD;
            }
        } catch (NumberFormatException e) {
            UIManager.printWarning("Invalid selection.");
        }

        return CommandResult.NO_TICK;
    }

    private boolean hasBabiesInHousehold(IGameEngine engine) {
        for (Sim s : engine.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable()) {
                return true;
            }
        }
        return false;
    }

    private void handleDate(Sim sim, Sim spouse) {
        SimulationLogger.log(sim.getName() + " and " + spouse.getName() + " spent a lovely time together on a date!");
        sim.getHappiness().increase(30);
        sim.getSocial().increase(50);
        spouse.getHappiness().increase(30);
        spouse.getSocial().increase(50);

        sim.getRelationshipManager().increaseRelationship(spouse, 10);
        spouse.getRelationshipManager().increaseRelationship(sim, 10);
    }

    private CommandResult handleHaveBaby(Sim sim, IGameEngine engine, Scanner scanner) {
        Building home = null;
        for (Building b : engine.getWorldManager().getCityMap()) {
            if (b instanceof Residential) {
                home = b;
                break;
            }
        }

        if (home != null) {
            engine.getWorldManager().setCurrentLocation(home);
            sim.setCurrentRoom(((Residential) home).getRooms().get(0));

            Sim spouse = sim.getRelationshipManager().getSpouse();
            if (spouse != null) {
                spouse.setCurrentRoom(sim.getCurrentRoom());
            }

            SimulationLogger.log("Teleported home to " + home.getName() + " to try for a baby...");

            try {
                Gender babyGender = sim.getRelationshipManager().attemptPregnancy();
                if (babyGender != null) {
                    UIManager.prompt("Enter a name for the new " + babyGender + " baby: ");
                    String babyName = scanner.nextLine().trim();
                    if (babyName.isEmpty()) babyName = "Baby " + sim.getName();

                    Sim child = sim.getRelationshipManager().finalizeBaby(babyName, babyGender);

                    engine.getNeighborhood().add(child);
                    SimulationLogger.log(child.getName() + " has been added to your household!");
                    return CommandResult.TICK_FORWARD;
                }
            } catch (SimulationException e) {
                SimulationLogger.logWarning(e.getMessage());
            }
        }
        return CommandResult.NO_TICK;
    }

    private void handleFeedBabies(Sim parent, IGameEngine engine) {
        int count = 0;
        for (Sim s : engine.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable()) {
                s.getHunger().increase(50);
                count++;
            }
        }
        if (count > 0) {
            SimulationLogger.log(parent.getName() + " feeds all " + count + " babies in the household.");
            parent.getSocial().increase(10);
            parent.getHappiness().increase(5);
        }
    }
}
