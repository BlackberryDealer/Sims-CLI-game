package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.models.Job;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.entities.models.WorkResult;
import simcli.ui.UIManager;

/**
 * Command to send the active Sim to work a shift at their current job.
 * Validates employment status, life stage, hunger state, overwork limits,
 * and weekday restrictions before executing.
 */
public class WorkCommand extends BaseCommand {

    /**
     * Constructs a {@code WorkCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public WorkCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Validates work preconditions (employment, age, hunger, weekday) and
     * executes a work shift. Advances the clock by the job's working hours.
     *
     * @return {@link CommandResult#TICK_FORWARD} on success,
     *         {@link CommandResult#NO_TICK} if any precondition fails.
     * @throws SimulationException if an unexpected simulation rule is violated.
     */
    @Override
    protected CommandResult run() throws SimulationException {
        Sim activePlayer = ctx.getActivePlayer();

        if (!activePlayer.canWork()) {
            UIManager.printMessage(activePlayer.getName() + " is in the " + activePlayer.getCurrentStageName() + " stage and cannot work!");
            return CommandResult.NO_TICK;
        }
        if (activePlayer.getCareer() == Job.UNEMPLOYED) {
            UIManager.printMessage(activePlayer.getName() + " is unemployed and cannot work!");
            return CommandResult.NO_TICK;
        }

        // Slide 5: HUNGRY or STARVING states restrict work (Aligned with Proposal Slides)
        if (activePlayer.getState() == SimState.HUNGRY) {
            UIManager.printWarning(activePlayer.getName() + " is too hungry to focus on work! Eat something first.");
            pause();
            return CommandResult.NO_TICK;
        }

        // Check for overwork warning
        if (activePlayer.getShiftsWorkedToday() >= 1 && !activePlayer.hasWarnedAboutOverwork()) {
            ctx.getLogger().logWarning("Working multiple shifts in a single day drains stats significantly faster!");
            ctx.getLogger().prompt("Are you sure you want to overwork? (Y/N)> ");
            String conf = ctx.getScanner().nextLine().trim();
            activePlayer.setWarnedAboutOverwork(true);
            if (!conf.equalsIgnoreCase("Y")) {
                UIManager.printMessage("Work action cancelled.");
                return CommandResult.NO_TICK;
            }
        }

        // Weekday check (Time Expansion feature)
        String dayStr = ctx.getTimeManager().getDayOfWeek();
        if (dayStr.equals("Saturday") || dayStr.equals("Sunday")) {
            UIManager.printWarning("The office is closed on " + dayStr + "s! You cannot work on the weekend.");
            UIManager.printMessage("Spend the weekend socializing, studying, or upgrading your house.");
            pause();
            return CommandResult.NO_TICK;
        }

        // Execution Core
        activePlayer.setCurrentAction(ActionState.WORKING);
        UIManager.displayActionAnimation(activePlayer);

        WorkResult result = activePlayer.performWork();
        
        if (result.isSuccess()) {
            UIManager.printMessage(activePlayer.getName() + " works a shift as a " + activePlayer.getCareer().getTitle() + " and earns $" + result.getEarnings() + "!");
            if (result.isOverworked()) {
                UIManager.printMessage(activePlayer.getName() + " feels the heavy strain of overworking!");
            }
        }

        ctx.getTimeManager().advanceTicks(activePlayer.getCareer().getWorkingHours() - 1);
        
        return CommandResult.TICK_FORWARD;
    }
}
