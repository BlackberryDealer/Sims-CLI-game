package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.GoalManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.Achievement;
import simcli.entities.models.SimState;
import simcli.ui.Panel;
import simcli.ui.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Command that shows the household's goals: each Sim's life aspiration
 * with their progress, and which achievements have been unlocked.
 */
public class GoalsCommand extends BaseCommand {

    private final GoalManager goals;

    /**
     * Constructs a {@code GoalsCommand}.
     *
     * @param ctx   shared command context.
     * @param goals the household's goal tracker.
     */
    public GoalsCommand(CommandContext ctx, GoalManager goals) {
        super(ctx);
        this.goals = goals;
    }

    /**
     * Prints the goals screen and waits for the user to press ENTER.
     *
     * @return {@link CommandResult#NO_TICK} — viewing goals does not advance time.
     */
    @Override
    protected CommandResult run() {
        List<String> lines = new ArrayList<>();
        lines.add("{W}Aspirations{/}");
        for (Sim sim : ctx.getNeighborhood()) {
            if (sim.getState() == SimState.DEAD) {
                continue;
            }
            String name = Style.fit("{W}" + sim.getName() + "{/}", 10);
            if (!sim.isPlayable()) {
                lines.add(name + "{k}too young for a life goal yet{/}");
                continue;
            }
            GoalManager.Progress p = goals.progress(sim, sim.getAspiration());
            String status = sim.isAspirationFulfilled()
                    ? "{G}" + Panel.STAR + " fulfilled{/}"
                    : Panel.bar(p.getPercent(), 8) + " {k}" + p.getCurrent() + "/" + p.getTarget() + "{/}";
            lines.add(name + Style.fit("{M}" + sim.getAspiration().getDisplayName() + "{/}", 18) + status);
            lines.add("          {k}" + sim.getAspiration().getGoal() + "{/}");
        }
        if (goals.isLegacyComplete()) {
            lines.add("");
            lines.add("{G}" + Panel.STAR + " Legacy complete - every Sim has fulfilled their aspiration!{/}");
        }

        lines.add("");
        lines.add("{W}Achievements{/}  {k}" + goals.getUnlocked().size() + " of "
                + Achievement.values().length + " unlocked{/}");
        for (Achievement a : Achievement.values()) {
            String mark = goals.isUnlocked(a) ? "{G}" + Panel.CHECK + "{/} " : "{k}" + Panel.DOT + "{/} ";
            String title = goals.isUnlocked(a) ? "{W}" + a.getDisplayName() + "{/}" : "{k}" + a.getDisplayName() + "{/}";
            lines.add(mark + Style.fit(title, 20) + "{k}" + a.getDescription() + "{/}");
        }

        System.out.println(Panel.box("{W}{*}Goals{/}", 'm', lines));
        pause();
        return CommandResult.NO_TICK;
    }
}
