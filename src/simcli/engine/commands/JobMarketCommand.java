package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Job;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

import java.util.Scanner;

public class JobMarketCommand extends BaseCommand {
    private final Sim activePlayer;
    private final Scanner scanner;

    public JobMarketCommand(Sim activePlayer, Scanner scanner) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
    }

    @Override
    public CommandResult execute() {
        if (activePlayer.canWork()) {
            UIManager.printMessage("\n=== JOB MARKET ===");
            UIManager.printMessage("Current Job: " + activePlayer.getCareer().getTitle());
            UIManager.printMessage("[0] Quit Current Job (Become Unemployed)");

            Job[] allJobs = Job.values();
            for (int i = 1; i < allJobs.length; i++) {
                Job j = allJobs[i];
                UIManager.printMessage("[" + i + "] " + j.getTitle() + " (Start: $" + j.getBaseSalary()
                        + ", Req Age: " + j.getMinAge() + "-" + j.getMaxAge() + ")");
            }
            UIManager.printMessage("[-1] Back");
            UIManager.prompt("Select Job> ");
            try {
                int jChoice = Integer.parseInt(scanner.nextLine().trim());
                if (jChoice == -1) {
                    // Do nothing
                } else if (jChoice == 0) {
                    Job targetJob = allJobs[jChoice];
                    if (targetJob == activePlayer.getCareer()) {
                        UIManager.printMessage("You are already unemployed.");
                        pause(scanner);
                    } else {
                        activePlayer.changeJob(Job.UNEMPLOYED);
                    }
                } else if (jChoice > 0 && jChoice < allJobs.length) {
                    Job targetJob = allJobs[jChoice];

                    // Do not allow Sim to reapply for the same job
                    if (targetJob == activePlayer.getCareer()) {
                        UIManager.printMessage("You already have this job.");
                        pause(scanner);
                    } else if (activePlayer.getAge() >= targetJob.getMinAge()
                            && activePlayer.getAge() <= targetJob.getMaxAge()) {
                        activePlayer.changeJob(targetJob);
                    } else {
                        UIManager.printMessage("You don't meet the age requirements for this job.");
                        pause(scanner);
                    }
                } else {
                    UIManager.printMessage("Invalid choice.");
                    pause(scanner);
                }
            } catch (NumberFormatException e) {
                UIManager.printMessage("Invalid input.");
                pause(scanner);
            }
        } else {
            UIManager.printMessage("Only adults can access the job market.");
            pause(scanner);
        }
        return CommandResult.NO_TICK;
    }

}
