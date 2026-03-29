package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.needs.Need;
import simcli.ui.UIManager;
import simcli.world.Building;

/**
 * Command that displays a detailed status report of the active Sim.
 *
 * <p>Shows name, age, traits, money, job, health, all need values,
 * inventory capacity, current location, spouse, and children.</p>
 */
public class CharacterStatusCommand extends BaseCommand {

    /**
     * Constructs a {@code CharacterStatusCommand} with the given context.
     *
     * @param ctx shared command context providing access to the active player.
     */
    public CharacterStatusCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Prints the full character status and waits for the user to press ENTER.
     *
     * @return {@link CommandResult#NO_TICK} — viewing status does not advance time.
     */
    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();

        UIManager.printMessage("\n=== CHARACTER STATUS ===");
        UIManager.printMessage("Name: " + activePlayer.getName());
        UIManager.printMessage("Age: " + activePlayer.getAge());
        UIManager.printMessage("Traits: " + activePlayer.getTraits());
        UIManager.printMessage("Money: $" + activePlayer.getMoney());
        if (activePlayer.canWork()) {
            UIManager.printMessage("Job: " + activePlayer.getCareer().getTitle() + " (Tier " + activePlayer.getJobTier() + ")");
            UIManager.printMessage("Salary: $" + activePlayer.getCareer().getSalaryAtTier(activePlayer.getJobTier()) + " per shift");
        }
        UIManager.printMessage("Health: " + activePlayer.getHealth() + "%");
        UIManager.printMessage("Hunger: " + activePlayer.getHunger().getValue() + " / " + Need.MAX_VALUE);
        UIManager.printMessage("Energy: " + activePlayer.getEnergy().getValue() + " / " + Need.MAX_VALUE);
        UIManager.printMessage("Hygiene: " + activePlayer.getHygiene().getValue() + " / " + Need.MAX_VALUE);
        UIManager.printMessage("Happiness: " + activePlayer.getHappiness().getValue() + " / " + Need.MAX_VALUE);
        UIManager.printMessage("Social: " + activePlayer.getSocial().getValue() + " / " + Need.MAX_VALUE);
        UIManager.printMessage(
                "Inventory Items: " + activePlayer.getInventory().size() + " / " + activePlayer.getInventoryCapacity());
        UIManager.printMessage("Location: " + currentLocation.getName());
        if (activePlayer.getRelationshipManager().getSpouse() != null) {
            UIManager.printMessage("Spouse: " + activePlayer.getRelationshipManager().getSpouse().getName());
        }
        if (!activePlayer.getRelationshipManager().getChildren().isEmpty()) {
            StringBuilder childInfo = new StringBuilder("Children: ");
            for (Sim child : activePlayer.getRelationshipManager().getChildren()) {
                childInfo.append(child.getName()).append(" (Age: ").append(child.getAge())
                         .append(", ").append(child.isPlayable() ? "Playable" : "Not Playable")
                         .append(") ");
            }
            UIManager.printMessage(childInfo.toString().trim());
        }
        UIManager.printMessage("==================");
        
        pause();
        return CommandResult.NO_TICK;
    }
}
