package simcli.engine.commands;

import java.util.Scanner;
import simcli.engine.CommandResult;
import simcli.entities.models.Job;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

public class JobMarketCommand extends BaseCommand {
    private final CommandContext ctx;

    public JobMarketCommand(CommandContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();
        Scanner scanner = ctx.getScanner();

        if (activePlayer.canWork()) {
            UIManager.printMessage("\n=== JOB MARKET ===");
            UIManager.printMessage("Active Sim: " + activePlayer.getName() + " (Age: " + activePlayer.getAge() + ")");
            UIManager.printMessage("Current Job: " + activePlayer.getCareer().getTitle());
            UIManager.printMessage("[0] Retire / Become Unemployed");

            Job[] allJobs = Job.values();
            for (int i = 1; i < allJobs.length; i++) {
                Job j = allJobs[i];
                UIManager.printMessage("[" + i + "] " + j.getTitle() + 
                    " | Pay: $" + j.getBaseSalary() + 
                    " | Age: " + j.getMinAge() + "-" + j.getMaxAge());
            }
            UIManager.printMessage("[-1] Back");
            UIManager.prompt("Select Option> ");

            try {
                int jChoice = Integer.parseInt(scanner.nextLine().trim());
                if (jChoice == -1) return CommandResult.NO_TICK;

                if (jChoice == 0) {
                    if (activePlayer.getCareer() == Job.UNEMPLOYED) {
                        UIManager.printMessage("You are already unemployed.");
                    } else {
                        activePlayer.getCareerManager().changeJob(Job.UNEMPLOYED, activePlayer.getName());
                        UIManager.printMessage("You have successfully retired.");
                    }
                    pause(scanner);
                } else if (jChoice > 0 && jChoice < allJobs.length) {
                    Job targetJob = allJobs[jChoice];

                    if (targetJob == activePlayer.getCareer()) {
                        UIManager.printMessage("You already have this job.");
                    } else if (activePlayer.getAge() < targetJob.getMinAge()) {
                        UIManager.printMessage("Sorry, you are too young for this position.");
                    } else if (activePlayer.getAge() > targetJob.getMaxAge()) {
                        UIManager.printMessage("Sorry, you are past the maximum age (" + targetJob.getMaxAge() + ") for this career.");
                    } else {
                        activePlayer.getCareerManager().changeJob(targetJob, activePlayer.getName());
                        UIManager.printMessage("Congratulations on your new career as a " + targetJob.getTitle() + "!");
                    }
                    pause(scanner);
                }
            } catch (NumberFormatException e) {
                UIManager.printMessage("Invalid selection.");
                pause(scanner);
            }
        } else {
            UIManager.printMessage("Children and Teens cannot access the professional job market.");
            pause(scanner);
        }
        return CommandResult.NO_TICK;
    }
}