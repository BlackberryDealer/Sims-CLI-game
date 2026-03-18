package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.Sim;
import simcli.ui.UIManager;
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
        if (activePlayer.canWork()) {
            // Check for overwork warning
            if (activePlayer.getShiftsWorkedToday() >= 1 && !activePlayer.hasWarnedAboutOverwork()) {
                UIManager.printWarning("Working multiple shifts in a single day drains stats significantly faster!");
                UIManager.prompt("Are you sure you want to overwork? (Y/N)> ");
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
                UIManager.prompt("\nPress ENTER to return...");
                scanner.nextLine();
                return CommandResult.NO_TICK;
            }

            // Normal Execution
            activePlayer.performActivity("Work");
            timeManager.advanceTicks(activePlayer.getCareer().getWorkingHours() - 1);
        } else {
            // Unemployed / Children logic
            activePlayer.performActivity("Work");
        }
        return CommandResult.TICK_FORWARD;
    }
}
