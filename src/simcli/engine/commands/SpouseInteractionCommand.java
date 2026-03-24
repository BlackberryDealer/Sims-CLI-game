package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.GameEngine;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.Scanner;

/**
 * Command to interact with a spouse from anywhere if they are in the household.
 */
public class SpouseInteractionCommand implements ICommand {
    private final GameEngine engine;
    private final Scanner scanner;

    public SpouseInteractionCommand(GameEngine engine, Scanner scanner) {
        this.engine = engine;
        this.scanner = scanner;
    }

    @Override
    public CommandResult execute() throws SimulationException {
        Sim activePlayer = engine.getActivePlayer();
        Sim spouse = activePlayer.getRelationshipManager().getSpouse();
        
        if (spouse == null) {
            UIManager.printMessage("You are not married. Find someone special at the park first!");
            UIManager.prompt("\nPress ENTER to continue...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        }

        // Check if spouse is in the household
        if (!engine.getNeighborhood().contains(spouse)) {
            UIManager.printMessage(spouse.getName() + " is currently busy and not at home.");
            UIManager.prompt("\nPress ENTER to continue...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        }

        UIManager.printMessage("\n=== Interaction with " + spouse.getName() + " ===");
        UIManager.printMessage("[1] Go on a Date");
        UIManager.printMessage("[2] Have a Baby");
        
        // Check for any baby (non-playable child) in household
        Sim baby = findBabyInHousehold();
        if (baby != null) {
            UIManager.printMessage("[3] Feed Baby (" + baby.getName() + ")");
        }
        
        UIManager.printMessage("[0] Cancel");
        UIManager.prompt("Select action> ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 1) {
                handleDate(activePlayer, spouse);
                return CommandResult.TICK_FORWARD;
            } else if (choice == 2) {
                handleHaveBaby(activePlayer);
                return CommandResult.TICK_FORWARD;
            } else if (choice == 3 && baby != null) {
                handleFeedBaby(activePlayer, baby);
                return CommandResult.TICK_FORWARD;
            }
        } catch (NumberFormatException e) {
            UIManager.printWarning("Invalid selection.");
        }

        return CommandResult.NO_TICK;
    }

    private Sim findBabyInHousehold() {
        for (Sim s : engine.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable()) {
                return s;
            }
        }
        return null;
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

    private void handleHaveBaby(Sim sim) {
        UIManager.prompt("Enter a name for the baby: ");
        String babyName = scanner.nextLine().trim();
        if (babyName.isEmpty()) babyName = "Baby";

        // Teleport both to the first residential building (Home)
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
                Sim child = sim.getRelationshipManager().reproduce(babyName);
                if (child != null) {
                    engine.getNeighborhood().add(child);
                    SimulationLogger.log(child.getName() + " has been added to your household!");
                }
            } catch (SimulationException e) {
                SimulationLogger.logWarning(e.getMessage());
            }
        }
    }

    private void handleFeedBaby(Sim parent, Sim baby) {
        SimulationLogger.log(parent.getName() + " feeds " + baby.getName() + " some warm milk.");
        baby.getHunger().increase(50);
        parent.getSocial().increase(10);
        parent.getHappiness().increase(5);
    }
}
