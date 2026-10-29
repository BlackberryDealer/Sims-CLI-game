package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.GoalManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.Achievement;
import simcli.entities.models.Aspiration;
import simcli.entities.models.SimState;
import simcli.entities.models.StarterGoal;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Command that shows the household's goals: the first week's starter
 * goals, each Sim's life aspiration with their progress, the household's
 * legacy score, and which achievements have been unlocked.
 *
 * <p>If the active Sim has fulfilled their aspiration, they can choose a
 * new one from here.</p>
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
     * Prints the goals screen, then offers the active Sim a new aspiration
     * if they've fulfilled theirs.
     *
     * @return {@link CommandResult#NO_TICK} — viewing goals does not advance time.
     */
    @Override
    protected CommandResult run() {
        List<Sim> household = ctx.getNeighborhood();
        List<String> lines = new ArrayList<>();

        if (goals.getStartersDone().size() < StarterGoal.values().length) {
            lines.add("{W}First steps{/}");
            for (StarterGoal goal : StarterGoal.values()) {
                boolean done = goals.isStarterDone(goal);
                lines.add((done ? "{G}" + Panel.CHECK + "{/} " : "{k}" + Panel.DOT + "{/} ")
                        + Style.fit(done ? "{k}" + goal.getDescription() + "{/}" : goal.getDescription(), 44)
                        + "{G}+$" + goal.getReward() + "{/}");
            }
            lines.add("");
        }

        lines.add("{W}Aspirations{/}");
        for (Sim sim : household) {
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
            if (!sim.getPastAspirations().isEmpty()) {
                lines.add("          {k}also fulfilled: " + pastNames(sim) + "{/}");
            }
        }
        lines.add("");
        lines.add("{W}Legacy{/}  score {Y}" + goals.getLegacyScore(household) + "{/}  {k}" + Panel.DOT
                + " generation " + GoalManager.getHighestGeneration(household)
                + (goals.isLegacyComplete() ? " " + Panel.DOT + " every founder's dream came true" : "") + "{/}");

        lines.add("");
        lines.add("{W}Achievements{/}  {k}" + goals.getUnlocked().size() + " of "
                + Achievement.values().length + " unlocked{/}");
        for (Achievement a : Achievement.values()) {
            String mark = goals.isUnlocked(a) ? "{G}" + Panel.CHECK + "{/} " : "{k}" + Panel.DOT + "{/} ";
            String title = goals.isUnlocked(a) ? "{W}" + a.getDisplayName() + "{/}" : "{k}" + a.getDisplayName() + "{/}";
            lines.add(mark + Style.fit(title, 20) + "{k}" + a.getDescription() + "{/}");
        }

        System.out.println(Panel.box("{W}{*}Goals{/}", 'm', lines));

        Sim active = ctx.getActivePlayer();
        List<Aspiration> next = goals.nextAspirations(active);
        if (active.isPlayable() && active.isAspirationFulfilled() && !next.isEmpty()) {
            chooseNext(active, next);
        } else {
            pause();
        }
        return CommandResult.NO_TICK;
    }

    /** Lets a Sim who has fulfilled their aspiration pick their next one. */
    private void chooseNext(Sim sim, List<Aspiration> next) {
        UIManager.printMessage("\n  {W}" + sim.getName() + "{/} has fulfilled their aspiration. What's next?");
        for (int i = 0; i < next.size(); i++) {
            UIManager.option(String.valueOf(i + 1), Style.fit(next.get(i).getDisplayName(), 18), next.get(i).getGoal());
        }
        UIManager.option("0", "Not yet");
        UIManager.prompt("\nNext aspiration> ");
        try {
            int pick = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (pick >= 1 && pick <= next.size()) {
                sim.chooseNextAspiration(next.get(pick - 1));
                UIManager.success(sim.getName() + " now dreams of being a {M}"
                        + sim.getAspiration().getDisplayName() + "{/}.");
                pause();
            }
        } catch (NumberFormatException e) {
            // anything else means "not yet"
        }
    }

    private static String pastNames(Sim sim) {
        StringBuilder sb = new StringBuilder();
        for (Aspiration a : sim.getPastAspirations()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(a.getDisplayName());
        }
        return sb.toString();
    }
}
