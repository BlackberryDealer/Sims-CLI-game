package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.entities.models.Job;
import simcli.entities.models.Trait;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.utils.GameConstants;
import simcli.world.Building;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command that displays a detailed status report of the active Sim.
 *
 * <p>Shows name, age, traits, money, job, health, all need values,
 * inventory capacity, current location, spouse, children and closest
 * relationships.</p>
 */
public class CharacterStatusCommand extends BaseCommand {

    /**
     * Constructs a {@code CharacterStatusCommand} with the given context.
     *
     * @param ctx shared command context providing access to the active player.
     */
    public CharacterStatusCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Prints the full character status and waits for the user to press ENTER.
     *
     * @return {@link CommandResult#NO_TICK} — viewing status does not advance time.
     */
    @Override
    protected CommandResult run() {
        Sim sim = ctx.getActivePlayer();
        Building currentLocation = ctx.getCurrentLocation();
        List<String> lines = new ArrayList<>();

        int daysToBirthday = GameConstants.DAYS_PER_AGE_TICK - (sim.getDaysAlive() % GameConstants.DAYS_PER_AGE_TICK);
        lines.add("{k}Age{/}        " + sim.getAge() + " " + Panel.DOT + " " + sim.getCurrentStageName()
                + "  {k}(next birthday in " + daysToBirthday + (daysToBirthday == 1 ? " day)" : " days)") + "{/}");
        lines.add("{k}Traits{/}     " + traits(sim));
        lines.add("{k}Money{/}      {G}$" + sim.getMoney() + "{/}  {k}(earned $" + sim.getTotalMoneyEarned() + " in total){/}");
        if (sim.canWork()) {
            if (sim.getCareer() == Job.UNEMPLOYED) {
                lines.add("{k}Job{/}        Unemployed {k}- visit the Job Market with [J]{/}");
            } else {
                lines.add("{k}Job{/}        " + sim.getCareer().getTitle() + " {k}(tier " + sim.getJobTier()
                        + "/" + sim.getCareer().getMaxTier() + ", $"
                        + sim.getCareer().getSalaryAtTier(sim.getJobTier()) + " per shift){/}");
            }
        }
        lines.add("{k}Location{/}   " + currentLocation.getName()
                + (sim.getCurrentRoom() != null && currentLocation.isResidential()
                        ? " " + Panel.ARROW + " " + sim.getCurrentRoom().getName() : ""));
        lines.add("{k}Bag{/}        " + sim.getInventory().size() + " / " + sim.getInventoryCapacity() + " items");
        lines.add("");

        int w = 18;
        lines.add(Panel.meter("Health", sim.getHealth(), w) + "   " + Panel.meter("Hunger", sim.getHunger().getValue(), w));
        lines.add(Panel.meter("Energy", sim.getEnergy().getValue(), w) + "   " + Panel.meter("Hygiene", sim.getHygiene().getValue(), w));
        lines.add(Panel.meter("Fun", sim.getHappiness().getValue(), w) + "   " + Panel.meter("Social", sim.getSocial().getValue(), w));
        lines.add("");

        Sim spouse = sim.getRelationshipManager().getSpouse();
        lines.add("{k}Spouse{/}     " + (spouse != null ? "{M}" + Panel.HEART + "{/} " + spouse.getName() : "{k}single{/}"));
        List<Sim> children = sim.getRelationshipManager().getChildren();
        if (!children.isEmpty()) {
            StringBuilder kids = new StringBuilder();
            for (Sim child : children) {
                if (kids.length() > 0) kids.append(", ");
                kids.append(child.getName()).append(" {k}(").append(child.getAge())
                        .append(child.isPlayable() ? "" : ", baby").append("){/}");
            }
            lines.add("{k}Children{/}   " + kids);
        }
        lines.add("{k}Friends{/}    " + closestFriends(sim));

        System.out.println(Panel.box("{W}{*}" + sim.getName() + "{/}", 'c', lines));
        pause();
        return CommandResult.NO_TICK;
    }

    private static String traits(Sim sim) {
        StringBuilder sb = new StringBuilder();
        for (Trait t : sim.getTraits()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(t.name().charAt(0)).append(t.name().substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /** The three Sims this Sim gets on with best, e.g. "Bob 80, Ann 45". */
    private static String closestFriends(Sim sim) {
        List<Map.Entry<Sim, Integer>> rels = new ArrayList<>(sim.getRelationshipManager().getRelationships().entrySet());
        rels.removeIf(e -> e.getValue() <= 0);
        if (rels.isEmpty()) {
            return "{k}none yet - meet people at the Park{/}";
        }
        rels.sort((a, b) -> b.getValue() - a.getValue());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, rels.size()); i++) {
            if (i > 0) sb.append(", ");
            int score = rels.get(i).getValue();
            sb.append(rels.get(i).getKey().getName()).append(" {").append(Style.levelColor(score)).append('}')
                    .append(score).append("{/}");
        }
        return sb.toString();
    }
}
