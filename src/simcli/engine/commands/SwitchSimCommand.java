package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.ui.ascii.Sprites;

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

        UIManager.title("Who do you want to play as?");
        if (neighborhood.size() <= 1) {
            UIManager.info("There is no one else in your household to switch to.");
            pause();
            return CommandResult.NO_TICK;
        }

        for (int i = 0; i < neighborhood.size(); i++) {
            Sim s = neighborhood.get(i);
            String status;
            if (s.getState() == SimState.DEAD) {
                status = "{k}" + Panel.CROSS + " passed away{/}";
            } else if (!s.isPlayable()) {
                status = "{k}baby, age " + s.getAge() + " - playable at 13{/}";
            } else {
                status = "{" + Sprites.moodColor(s) + "}" + Panel.STAR + "{/} {k}" + s.getAge() + ", "
                        + s.getCareer().getTitle() + ", " + Panel.HEART + " " + s.getHealth() + "{/}";
            }
            String marker = (s == activePlayer) ? "  {G}" + Panel.ARROW + " playing now{/}" : "";
            UIManager.option(String.valueOf(i + 1), Style.fit(s.getName(), 16) + status + marker);
        }
        UIManager.option("0", "Cancel");
        UIManager.prompt("\nPlay as> ");

        try {
            String input = ctx.getScanner().nextLine().trim();
            if (input.isEmpty()) return CommandResult.NO_TICK;
            int target = Integer.parseInt(input);
            if (target == 0) return CommandResult.NO_TICK;

            if (target > 0 && target <= neighborhood.size()) {
                Sim chosen = neighborhood.get(target - 1);
                if (chosen.getState() == SimState.DEAD) {
                    UIManager.info(chosen.getName() + " is deceased. Let them rest.");
                    pause();
                    return CommandResult.NO_TICK;
                }
                if (!chosen.isPlayable()) {
                    UIManager.info(chosen.getName() + " is too young to be controlled. They must reach the teen stage (age 13) first.");
                    pause();
                    return CommandResult.NO_TICK;
                }

                // callback fires back into GameEngine.setActivePlayer;
                // the next screen shows the newly selected Sim
                ctx.switchActivePlayer(chosen);
                return CommandResult.NO_TICK;
            } else {
                UIManager.error("Invalid selection.");
                pause();
                return CommandResult.NO_TICK;
            }

        } catch (NumberFormatException e) {
             UIManager.error("Invalid input.");
             pause();
             return CommandResult.NO_TICK;
        }
    }
}
