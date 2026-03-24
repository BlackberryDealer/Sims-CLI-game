package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.actors.Job;
import simcli.entities.actors.ActionState;

import java.util.Scanner;

public class WorkCommand implements ICommand {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final TimeManager timeManager;

    public WorkCommand(Sim activePlayer, Scanner scanner, TimeManager timeManager) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.timeManager = timeManager;
    }

    @Override
    public CommandResult execute() throws SimulationException {
        if (!activePlayer.canWork()) {
            simcli.ui.UIManager.printMessage(activePlayer.getName() + " is in the " + activePlayer.getCurrentStageName() + " stage and cannot work!");
            return CommandResult.NO_TICK;
        }
        if (activePlayer.getCareer() == Job.UNEMPLOYED) {
            simcli.ui.UIManager.printMessage(activePlayer.getName() + " is unemployed and cannot work!");
            return CommandResult.NO_TICK;
        }

        // Slide 5: HUNGRY or STARVING states restrict work (Aligned with Proposal Slides)
        if (activePlayer.getState() == simcli.entities.actors.SimState.HUNGRY) {
            simcli.ui.UIManager.printWarning(activePlayer.getName() + " is too hungry to focus on work! Eat something first.");
            simcli.ui.UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        }

        // Check for overwork warning
        if (activePlayer.getShiftsWorkedToday() >= 1 && !activePlayer.hasWarnedAboutOverwork()) {
            SimulationLogger.logWarning("Working multiple shifts in a single day drains stats significantly faster!");
            SimulationLogger.prompt("Are you sure you want to overwork? (Y/N)> ");
            String conf = scanner.nextLine().trim();
            activePlayer.setWarnedAboutOverwork(true);
            if (!conf.equalsIgnoreCase("Y")) {
                simcli.ui.UIManager.printMessage("Work action cancelled.");
                return CommandResult.NO_TICK;
            }
        }

        // Weekday check (Time Expansion feature)
        String dayStr = timeManager.getDayOfWeek();
        if (dayStr.equals("Saturday") || dayStr.equals("Sunday")) {
            simcli.ui.UIManager.printWarning("The office is closed on " + dayStr + "s! You cannot work on the weekend.");
            simcli.ui.UIManager.printMessage("Spend the weekend socializing, studying, or upgrading your house.");
            simcli.ui.UIManager.prompt("\nPress ENTER to return...");
            scanner.nextLine();
            return CommandResult.NO_TICK;
        }

        // Execution Core
        activePlayer.setCurrentAction(ActionState.WORKING);
        simcli.ui.UIManager.displayActionAnimation(activePlayer);

        int dailyPay = activePlayer.getCareer().getSalaryAtTier(activePlayer.getJobTier());
        simcli.ui.UIManager.printMessage(activePlayer.getName() + " works a shift as a " + activePlayer.getCareer().getTitle() + " and earns $" + dailyPay + "!");

        int multiplier = 1 + activePlayer.getShiftsWorkedToday();
        if (multiplier > 1) {
            simcli.ui.UIManager.printMessage(activePlayer.getName() + " feels the heavy strain of overworking!");
        }

        activePlayer.getEnergy().decrease(activePlayer.getCareer().getEnergyDrain() * multiplier);
        activePlayer.getHunger().decrease(20 * multiplier);
        activePlayer.getHygiene().decrease(30 * multiplier);

        activePlayer.setMoney(activePlayer.getMoney() + dailyPay);
        activePlayer.addTotalMoneyEarned(dailyPay);
        
        activePlayer.incrementShiftsWorkedToday();
        activePlayer.resetConsecutiveDaysMissed();

        // Promotion chance after completing a shift
        if (activePlayer.getJobTier() < activePlayer.getCareer().getMaxTier()) {
            if (simcli.utils.GameRandom.RANDOM.nextDouble() < 0.25) {
                activePlayer.promote();
            }
        }

        timeManager.advanceTicks(activePlayer.getCareer().getWorkingHours() - 1);
        
        return CommandResult.TICK_FORWARD;
    }
}
