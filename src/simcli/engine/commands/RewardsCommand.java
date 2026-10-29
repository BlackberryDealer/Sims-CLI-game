package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.entities.managers.WantsManager;
import simcli.entities.models.Fear;
import simcli.entities.models.Reward;
import simcli.entities.models.Want;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Command that shows the active Sim's wants and fear for today and their
 * satisfaction points, and lets them spend the points in the reward store.
 */
public class RewardsCommand extends BaseCommand {

    /**
     * Constructs a {@code RewardsCommand}.
     *
     * @param ctx shared command context.
     */
    public RewardsCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Prints the wants and reward store, then buys whatever the player picks.
     *
     * @return {@link CommandResult#NO_TICK} — shopping for rewards takes no time.
     */
    @Override
    protected CommandResult run() {
        Sim sim = ctx.getActivePlayer();
        WantsManager wants = sim.getWants();
        Reward[] rewards = Reward.values();
        List<String> lines = new ArrayList<>();

        lines.add("{W}Today's wants{/}  {k}(new ones every day){/}");
        if (wants.getWants().isEmpty()) {
            lines.add("{k}nothing yet - wants arrive with the next turn{/}");
        }
        for (Want want : wants.getWants()) {
            boolean done = wants.isDone(want);
            lines.add((done ? "{G}" + Panel.CHECK + "{/} {k}" : "{k}" + Panel.DOT + "{/} ")
                    + Style.fit(want.getText(), 30) + (done ? "{/}" : "") + "{Y}+" + want.getPoints() + "{/}");
        }
        Fear fear = wants.getFear();
        if (fear != null) {
            lines.add("{R}" + Panel.WARN + "{/} {k}Fears{/} " + fear.getText().toLowerCase()
                    + (wants.hasFearCameTrue() ? "  {R}(came true){/}" : "  {k}(costs points if it happens){/}"));
        }
        lines.add("");
        lines.add("{W}Reward store{/}  {k}" + sim.getName() + " has{/} {Y}" + wants.getPoints()
                + " satisfaction points{/}");
        for (int i = 0; i < rewards.length; i++) {
            Reward r = rewards[i];
            boolean owned = r.isPerk() && sim.hasPerk(r);
            String price = owned ? "{G}" + Panel.CHECK + " owned{/}" : "{Y}" + r.getCost() + "{/}";
            lines.add("{C}[" + (i + 1) + "]{/} " + Style.fit((owned ? "{k}" : "{W}") + r.getDisplayName() + "{/}", 19)
                    + Style.fit(price, 9) + "{k}" + r.getDescription() + (r.isPerk() ? "" : " (one use)") + "{/}");
        }

        System.out.println(Panel.box("{W}{*}Wants & Rewards{/}", 'y', lines));
        UIManager.prompt("\nBuy a reward (0 to go back)> ");
        try {
            int pick = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (pick >= 1 && pick <= rewards.length) {
                Reward r = rewards[pick - 1];
                if (r.isPerk() && sim.hasPerk(r)) {
                    UIManager.printWarning(sim.getName() + " already has " + r.getDisplayName() + ".");
                } else if (sim.redeem(r)) {
                    UIManager.success(sim.getName() + " got {W}" + r.getDisplayName() + "{/}! "
                            + "{k}(" + wants.getPoints() + " points left){/}");
                } else {
                    UIManager.printWarning("Not enough satisfaction points - fulfil more wants first.");
                }
                pause();
            }
        } catch (NumberFormatException e) {
            // anything else means "go back"
        }
        return CommandResult.NO_TICK;
    }
}
