package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.entities.models.Memory;
import simcli.entities.models.SimState;
import simcli.ui.Panel;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Command that shows the family album: the moments each Sim remembers,
 * from their first job to their wedding day, including Sims who have
 * passed away.
 */
public class AlbumCommand extends BaseCommand {

    /**
     * Constructs an {@code AlbumCommand}.
     *
     * @param ctx shared command context.
     */
    public AlbumCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Lets the player pick a Sim (if there's more than one) and shows their memories.
     *
     * @return {@link CommandResult#NO_TICK} — looking at the album takes no time.
     */
    @Override
    protected CommandResult run() {
        List<Sim> household = ctx.getNeighborhood();
        Sim chosen = ctx.getActivePlayer();
        if (household.size() > 1) {
            UIManager.title("Family Album");
            for (int i = 0; i < household.size(); i++) {
                Sim sim = household.get(i);
                int count = sim.getMemories().size();
                UIManager.option(String.valueOf(i + 1), sim.getName()
                        + (sim.getState() == SimState.DEAD ? " {k}(R.I.P.){/}" : ""),
                        count + (count == 1 ? " memory" : " memories"));
            }
            UIManager.option("0", "Back");
            UIManager.prompt("\nWhose memories?> ");
            try {
                int pick = Integer.parseInt(ctx.getScanner().nextLine().trim());
                if (pick < 1 || pick > household.size()) {
                    return CommandResult.NO_TICK;
                }
                chosen = household.get(pick - 1);
            } catch (NumberFormatException e) {
                return CommandResult.NO_TICK;
            }
        }
        System.out.println(Panel.box("{W}{*}" + chosen.getName() + "'s memories{/}", 'm', lines(chosen)));
        pause();
        return CommandResult.NO_TICK;
    }

    /**
     * The album page for one Sim: one line per memory, oldest first.
     *
     * @param sim the Sim.
     * @return the lines to show.
     */
    public static List<String> lines(Sim sim) {
        List<String> lines = new ArrayList<>();
        if (sim.getMemories().isEmpty()) {
            lines.add("{k}No memories yet - go and live a little!{/}");
        }
        for (Memory m : sim.getMemories()) {
            lines.add(String.format("{k}Day %-4d{/} %s", m.getDay(), m.getText()));
        }
        return lines;
    }
}
