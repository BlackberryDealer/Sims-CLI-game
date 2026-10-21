package simcli.ui;

import simcli.entities.actors.Sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A snapshot of a Sim's needs, health and money, used to show how much they
 * changed over a long action such as a work shift or a night's sleep
 * (e.g. "Hunger 72 → 49").
 */
public final class NeedSummary {

    private static final String[] LABELS = {"Hunger", "Energy", "Hygiene", "Fun", "Social", "Health"};

    private final int[] values;
    private final int money;

    private NeedSummary(int[] values, int money) {
        this.values = values;
        this.money = money;
    }

    /**
     * Records the Sim's current needs, health and money.
     *
     * @param sim the Sim to snapshot.
     * @return the snapshot.
     */
    public static NeedSummary capture(Sim sim) {
        return new NeedSummary(new int[] {
                sim.getHunger().getValue(), sim.getEnergy().getValue(), sim.getHygiene().getValue(),
                sim.getHappiness().getValue(), sim.getSocial().getValue(), sim.getHealth()}, sim.getMoney());
    }

    /**
     * Describes every value that changed since this snapshot, one per entry,
     * e.g. {@code "Hunger 72 → 49"} coloured red for a drop and green for a rise.
     *
     * @param now a later snapshot of the same Sim.
     * @return the changes, or an empty list if nothing changed.
     */
    public List<String> changesTo(NeedSummary now) {
        List<String> changes = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] != now.values[i]) {
                changes.add(change(LABELS[i], values[i], now.values[i], ""));
            }
        }
        if (money != now.money) {
            changes.add(change("Money", money, now.money, "$"));
        }
        return changes;
    }

    private static String change(String label, int before, int after, String prefix) {
        String colour = after > before ? "{G}" : "{R}";
        return "{k}" + label + "{/} " + prefix + before + " " + Panel.ARROW + " " + colour + prefix + after + "{/}";
    }

    /**
     * Prints what changed for the Sim over {@code hours} hours, as a small panel.
     *
     * @param sim    the Sim.
     * @param before the snapshot taken before the action.
     * @param hours  how long the action took.
     */
    public static void print(Sim sim, NeedSummary before, int hours) {
        List<String> changes = before.changesTo(capture(sim));
        if (changes.isEmpty()) {
            return;
        }
        System.out.println(Panel.box("{W}" + sim.getName() + "{/} {k}after " + hours
                + (hours == 1 ? " hour" : " hours") + "{/}", 'k', Panel.columns(changes, 24)));
    }
}
