package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;

/**
 * Command to interact with a spouse from anywhere if they are in the household.
 * Supports dating, having babies, and feeding babies.
 *
 * <p>Previously coupled to {@code GameEngine} directly. Now uses
 * {@link CommandContext} for all data access and mutations.</p>
 */
public class SpouseInteractionCommand extends BaseCommand {

    /**
     * Constructs a {@code SpouseInteractionCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public SpouseInteractionCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Presents the marriage interaction menu (date, have baby, feed babies).
     *
     * @return {@link CommandResult#TICK_FORWARD} after dating or feeding babies,
     *         {@link CommandResult#NO_TICK} if the action is cancelled or unavailable.
     * @throws SimulationException if pregnancy rules prevent having a baby.
     */
    @Override
    protected CommandResult run() throws SimulationException {
        Sim activePlayer = ctx.getActivePlayer();
        Sim spouse = activePlayer.getRelationshipManager().getSpouse();
        
        if (spouse == null) {
            UIManager.printMessage("You are not married. Find someone special at the park first!");
            pause();
            return CommandResult.NO_TICK;
        }

        // Check if spouse is in the household
        if (!ctx.getNeighborhood().contains(spouse)) {
            UIManager.printMessage(spouse.getName() + " is currently busy and not at home.");
            pause();
            return CommandResult.NO_TICK;
        }

        UIManager.printMessage("\n=== Marriage Options ===");
        UIManager.printMessage("[1] Interact with Spouse (Date)");
        UIManager.printMessage("[2] Have a Baby");
        
        // Check for any baby (non-playable child) in household
        boolean hasBabies = hasBabiesInHousehold();
        if (hasBabies) {
            UIManager.printMessage("[3] Feed Babies");
        }
        
        UIManager.printMessage("[0] Back");
        UIManager.prompt("Select action> ");

        try {
            String input = ctx.getScanner().nextLine().trim();
            if (input.equals("0")) return CommandResult.NO_TICK;

            int choice = Integer.parseInt(input);
            if (choice == 1) {
                handleDate(activePlayer, spouse);
                return CommandResult.TICK_FORWARD;
            } else if (choice == 2) {
                return handleHaveBaby(activePlayer);
            } else if (choice == 3 && hasBabies) {
                handleFeedBabies(activePlayer);
                return CommandResult.TICK_FORWARD;
            }
        } catch (NumberFormatException e) {
            UIManager.printWarning("Invalid selection.");
        }

        return CommandResult.NO_TICK;
    }

    /** Checks whether any non-playable child (baby) exists in the household. */
    private boolean hasBabiesInHousehold() {
        for (Sim s : ctx.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a date interaction between the active Sim and their spouse.
     * Increases happiness, social, and relationship values for both partners.
     *
     * @param sim    the active player Sim.
     * @param spouse the spouse Sim.
     */
    private void handleDate(Sim sim, Sim spouse) {
        ctx.getLogger().log(sim.getName() + " and " + spouse.getName() + " spent a lovely time together on a date!");
        sim.getHappiness().increase(30);
        sim.getSocial().increase(50);
        spouse.getHappiness().increase(30);
        spouse.getSocial().increase(50);
        
        sim.getRelationshipManager().increaseRelationship(spouse, 10);
        spouse.getRelationshipManager().increaseRelationship(sim, 10);
    }

    /**
     * Handles the "Have a Baby" action — teleports both parents home and
     * attempts pregnancy via the relationship manager.
     *
     * @param sim the active player Sim.
     * @return {@link CommandResult#TICK_FORWARD} if a baby is born,
     *         {@link CommandResult#NO_TICK} otherwise.
     */
    private CommandResult handleHaveBaby(Sim sim) {
        // Teleport both to the first residential building (Home)
        Building home = null;
        for (Building b : ctx.getWorldManager().getCityMap()) {
            if (b instanceof Residential) {
                home = b;
                break;
            }
        }

        if (home != null) {
            ctx.getWorldManager().setCurrentLocation(home);
            sim.setCurrentRoom(((Residential) home).getRooms().get(0));
            
            Sim spouse = sim.getRelationshipManager().getSpouse();
            if (spouse != null) {
                spouse.setCurrentRoom(sim.getCurrentRoom());
            }
            
            ctx.getLogger().log("Teleported home to " + home.getName() + " to try for a baby...");
            
            try {
                Gender babyGender = sim.getRelationshipManager().attemptPregnancy(); 
                if (babyGender != null) {
                    UIManager.prompt("Enter a name for the new " + babyGender + " baby: ");
                    String babyName = ctx.getScanner().nextLine().trim();
                    if (babyName.isEmpty()) babyName = "Baby " + sim.getName();
                    
                    Sim child = sim.getRelationshipManager().finalizeBaby(babyName, babyGender);

                    ctx.getNeighborhood().add(child);
                    ctx.getLogger().log(child.getName() + " has been added to your household!");
                    return CommandResult.TICK_FORWARD;
                }
            } catch (SimulationException e) {
                ctx.getLogger().logWarning(e.getMessage());
            }
        }
        return CommandResult.NO_TICK;
    }

    /**
     * Feeds all babies (non-playable children) in the household, increasing
     * their hunger. Also grants a small social and happiness boost to the parent.
     *
     * @param parent the Sim performing the feeding.
     */
    private void handleFeedBabies(Sim parent) {
        int count = 0;
        for (Sim s : ctx.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable()) {
                s.getHunger().increase(50);
                count++;
            }
        }
        if (count > 0) {
            ctx.getLogger().log(parent.getName() + " feeds all " + count + " babies in the household.");
            parent.getSocial().increase(10);
            parent.getHappiness().increase(5);
        }
    }
}
