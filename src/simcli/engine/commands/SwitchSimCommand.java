package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

import java.util.List;
import simcli.entities.models.SimState;

// Switches the "active" Sim in the household.
// Key integration point: uses ctx.switchActivePlayer() callback
// so we mutate GameEngine state without importing GameEngine.
public class SwitchSimCommand extends BaseCommand {

    /**
     * Constructs a {@code SwitchSimCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public SwitchSimCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Displays the household roster and switches control to the selected Sim.
     * Dead and non-playable Sims cannot be selected.
     *
     * @return {@link CommandResult#NO_TICK} — switching Sims does not advance time.
     */
    @Override
    protected CommandResult run() {
        List<Sim> neighborhood = ctx.getNeighborhood();
        Sim activePlayer = ctx.getActivePlayer();

        UIManager.printMessage("\n=== Switch Sim ===");
        if (neighborhood.size() <= 1) {
            UIManager.printMessage("There is no one else in your household to switch to.");
            pause();
            return CommandResult.NO_TICK;
        }

        UIManager.printMessage("Current Household:");
        for (int i = 0; i < neighborhood.size(); i++) {
            Sim s = neighborhood.get(i);
            String status = s.getState() == SimState.DEAD ? "[DEAD]" : 
                           (!s.isPlayable() ? "[NOT PLAYABLE - Age: " + s.getAge() + "]" : "[Alive]");
            String marker = (s == activePlayer) ? " (Current)" : "";
            UIManager.printMessage("[" + (i + 1) + "] " + s.getName() + " " + status + marker);
        }
        UIManager.printMessage("[0] Cancel");
        UIManager.prompt("Select Sim to control> ");

        try {
            int target = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (target == 0) return CommandResult.NO_TICK;
            
            if (target > 0 && target <= neighborhood.size()) {
                Sim chosen = neighborhood.get(target - 1);
                if (chosen.getState() == SimState.DEAD) {
                    UIManager.printMessage(chosen.getName() + " is deceased. Let them rest.");
                    pause();
                    return CommandResult.NO_TICK;
                }
                if (!chosen.isPlayable()) {
                    UIManager.printMessage(chosen.getName() + " is too young to be controlled. They must reach the teen stage (age 13) first.");
                    pause();
                    return CommandResult.NO_TICK;
                }
                
                // callback fires back into GameEngine.setActivePlayer
                ctx.switchActivePlayer(chosen);
                UIManager.printMessage("Switched control to " + chosen.getName() + ".");
                pause();
                
                return CommandResult.NO_TICK;
            } else {
                UIManager.printWarning("Invalid selection.");
                pause();
                return CommandResult.NO_TICK;
            }

        } catch (NumberFormatException e) {
             UIManager.printWarning("Invalid input.");
             pause();
             return CommandResult.NO_TICK;
        }
    }
}
