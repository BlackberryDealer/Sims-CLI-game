package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.Job;
import simcli.entities.models.ActionState;
import simcli.ui.UIManager;

import java.util.Scanner;

/**
 * Command to send the active Sim to work a shift at their current job.
 * Validates employment status, life stage, hunger state, overwork limits,
 * and weekday restrictions before executing.
 */
public class WorkCommand extends BaseCommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final TimeManager timeManager;

    public WorkCommand(Sim activePlayer, Scanner scanner, TimeManager timeManager) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.timeManager = timeManager;
    }

    @Override
    protected CommandResult run() throws SimulationException {
        if (!activePlayer.canWork()) {
            UIManager.printMessage(activePlayer.getName() + " is in the " + activePlayer.getCurrentStageName() + " stage and cannot work!");
            return CommandResult.NO_TICK;
        }
        if (activePlayer.getCareer() == Job.UNEMPLOYED) {
            UIManager.printMessage(activePlayer.getName() + " is unemployed and cannot work!");
            return CommandResult.NO_TICK;
        }

        // Slide 5: HUNGRY or STARVING states restrict work (Aligned with Proposal Slides)
        if (activePlayer.getState() == simcli.entities.models.SimState.HUNGRY) {
            UIManager.printWarning(activePlayer.getName() + " is too hungry to focus on work! Eat something first.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }

        // Check for overwork warning
        if (activePlayer.getShiftsWorkedToday() >= 1 && !activePlayer.hasWarnedAboutOverwork()) {
            SimulationLogger.logWarning("Working multiple shifts in a single day drains stats significantly faster!");
            SimulationLogger.prompt("Are you sure you want to overwork? (Y/N)> ");
            String conf = scanner.nextLine().trim();
            activePlayer.setWarnedAboutOverwork(true);
            if (!conf.equalsIgnoreCase("Y")) {
                UIManager.printMessage("Work action cancelled.");
                return CommandResult.NO_TICK;
            }
        }

        // Weekday check (Time Expansion feature)
        String dayStr = timeManager.getDayOfWeek();
        if (dayStr.equals("Saturday") || dayStr.equals("Sunday")) {
            UIManager.printWarning("The office is closed on " + dayStr + "s! You cannot work on the weekend.");
            UIManager.printMessage("Spend the weekend socializing, studying, or upgrading your house.");
            pause(scanner);
            return CommandResult.NO_TICK;
        }

        // Execution Core
        activePlayer.setCurrentAction(ActionState.WORKING);
        UIManager.displayActionAnimation(activePlayer);

        simcli.entities.models.WorkResult result = activePlayer.performWork();
        
        if (result.isSuccess()) {
            UIManager.printMessage(activePlayer.getName() + " works a shift as a " + activePlayer.getCareer().getTitle() + " and earns $" + result.getEarnings() + "!");
            if (result.isOverworked()) {
                UIManager.printMessage(activePlayer.getName() + " feels the heavy strain of overworking!");
            }
        }

        timeManager.advanceTicks(activePlayer.getCareer().getWorkingHours() - 1);
        
        return CommandResult.TICK_FORWARD;
    }
}
