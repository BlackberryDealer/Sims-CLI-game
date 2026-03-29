package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

import java.util.List;
import simcli.entities.models.SimState;

/**
 * Command to switch control to another playable Sim in the household.
 * Displays all household members with their status and validates the selection.
 *
 * <p>Previously coupled to {@code GameEngine} directly. Now uses
 * {@link CommandContext#getNeighborhood()} and
 * {@link CommandContext#switchActivePlayer(Sim)} to read and mutate state
 * without knowing about the engine.</p>
 */
public class SwitchSimCommand extends BaseCommand {

    public SwitchSimCommand(CommandContext ctx) {
        super(ctx);
    }

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
