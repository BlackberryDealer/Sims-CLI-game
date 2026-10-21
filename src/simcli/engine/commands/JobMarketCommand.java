package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.models.Job;
import simcli.entities.actors.Sim;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;

/**
 * Command that opens the job market menu for the active Sim.
 *
 * <p>Displays all available jobs with salary, hours and age-range
 * information. The player can switch careers or quit their job (become
 * unemployed). Children cannot work; teens can take part-time jobs.</p>
 */
public class JobMarketCommand extends BaseCommand {

    /**
     * Constructs a {@code JobMarketCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public JobMarketCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Presents the job selection menu and processes the player's choice.
     *
     * @return {@link CommandResult#NO_TICK} — changing jobs does not advance time.
     */
    @Override
    protected CommandResult run() {
        Sim activePlayer = ctx.getActivePlayer();

        if (!activePlayer.canWork()) {
            UIManager.info("Children can't work yet. {k}At " + GameConstants.TEEN_AGE
                    + " they can take a part-time job.{/}");
            pause();
            return CommandResult.NO_TICK;
        }

        UIManager.title("Job Market");
        UIManager.printMessage("  {k}" + activePlayer.getName() + ", age " + activePlayer.getAge()
                + " " + Panel.DOT + " currently: {/}{W}" + activePlayer.getCareer().getTitle() + "{/}\n");
        UIManager.printMessage("  {k}     " + Style.fit("Career", 24) + Style.fit("Pay", 10)
                + Style.fit("Hours", 8) + Style.fit("Ages", 8) + "Skill{/}");

        Job[] allJobs = Job.values();
        for (int i = 1; i < allJobs.length; i++) {
            Job j = allJobs[i];
            String row = Style.fit(j.getTitle(), 24)
                    + Style.fit("{G}$" + j.getBaseSalary() + "{/}", 10)
                    + Style.fit(j.getWorkingHours() + "h", 8)
                    + Style.fit(j.getMinAge() + "-" + j.getMaxAge(), 8)
                    + Style.fit(j.getPrimarySkill().getDisplayName(), 10);
            if (j.isPartTime()) {
                row += "{k}part-time{/} ";
            }
            if (j == activePlayer.getCareer()) {
                row += "{C}" + Panel.ARROW + " your job{/}";
            } else if (activePlayer.getAge() < j.getMinAge() || activePlayer.getAge() > j.getMaxAge()) {
                row += "{k}not eligible{/}";
            }
            UIManager.option(String.valueOf(i), row);
        }
        System.out.println();
        if (activePlayer.getCareer() != Job.UNEMPLOYED) {
            UIManager.option("R", "Quit job / retire");
        }
        UIManager.option("0", "Back");
        UIManager.prompt("\nApply for> ");

        String input = ctx.getScanner().nextLine().trim();
        if (input.isEmpty() || input.equals("0")) {
            return CommandResult.NO_TICK;
        }

        if (input.equalsIgnoreCase("R")) {
            if (activePlayer.getCareer() == Job.UNEMPLOYED) {
                UIManager.info("You are already unemployed.");
            } else {
                activePlayer.getCareerManager().changeJob(Job.UNEMPLOYED, activePlayer.getName());
                UIManager.success("You have left your job. Enjoy the free time!");
            }
            pause();
            return CommandResult.NO_TICK;
        }

        try {
            int jChoice = Integer.parseInt(input);
            if (jChoice > 0 && jChoice < allJobs.length) {
                Job targetJob = allJobs[jChoice];

                if (targetJob == activePlayer.getCareer()) {
                    UIManager.info("You already have this job.");
                } else if (activePlayer.getAge() < targetJob.getMinAge()) {
                    UIManager.error("Sorry, you are too young for this position.");
                } else if (activePlayer.getAge() > targetJob.getMaxAge()) {
                    UIManager.error("Sorry, you are past the maximum age (" + targetJob.getMaxAge() + ") for this career.");
                } else {
                    activePlayer.getCareerManager().changeJob(targetJob, activePlayer.getName());
                    UIManager.success("Congratulations on your new career as a {W}" + targetJob.getTitle() + "{/}!");
                    UIManager.info("Shifts are " + targetJob.getWorkingHours() + " hours, Monday to Friday. Press [W] to work.");
                }
            } else {
                UIManager.error("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            UIManager.error("Invalid selection.");
        }
        pause();
        return CommandResult.NO_TICK;
    }
}
