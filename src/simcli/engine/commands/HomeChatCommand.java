package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.entities.actors.Sim;
import simcli.entities.managers.RelationshipManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.entities.models.SocialAction;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for talking to someone else in the household while at home, so
 * family is a source of social and not only the park.
 *
 * <p>Uses the same social actions and odds as the park. When an action
 * lands, the other Sim enjoys it too: they gain half its social value and
 * half its relationship value back towards the speaker.</p>
 */
public class HomeChatCommand extends BaseCommand {

    /**
     * Constructs a {@code HomeChatCommand}.
     *
     * @param ctx shared command context.
     */
    public HomeChatCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Lets the player pick a household member and a social action.
     *
     * @return {@link CommandResult#TICK_FORWARD} after a conversation,
     *         {@link CommandResult#NO_TICK} if there's nobody to talk to or the player backs out.
     */
    @Override
    protected CommandResult run() {
        Sim sim = ctx.getActivePlayer();
        if (!ctx.getCurrentLocation().isResidential()) {
            UIManager.info("Your family is at home. {k}Travel home with [T] to talk to them.{/}");
            pause();
            return CommandResult.NO_TICK;
        }
        List<Sim> people = available(sim, ctx.getNeighborhood());
        if (people.isEmpty()) {
            UIManager.info("There's nobody awake at home to talk to. {k}Meet people at the City Park.{/}");
            pause();
            return CommandResult.NO_TICK;
        }

        RelationshipManager rm = sim.getRelationshipManager();
        UIManager.title("Talk to who?");
        for (int i = 0; i < people.size(); i++) {
            Sim other = people.get(i);
            int score = rm.getRelationship(other);
            UIManager.option(String.valueOf(i + 1), Style.fit(other.getName(), 12) + Panel.bar(score, 10)
                    + "  {k}social " + other.getSocial().getValue() + "{/}");
        }
        UIManager.option("0", "Never mind");
        UIManager.prompt("\nTalk to> ");
        Sim other;
        try {
            int pick = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (pick < 1 || pick > people.size()) {
                return CommandResult.NO_TICK;
            }
            other = people.get(pick - 1);
        } catch (NumberFormatException e) {
            return CommandResult.NO_TICK;
        }

        List<SocialAction> actions = new ArrayList<>(List.of(SocialAction.CHAT, SocialAction.JOKE));
        if (rm.canFlirtWith(other)) {
            actions.add(SocialAction.FLIRT);
        }
        UIManager.title("Talking to " + other.getName());
        for (int i = 0; i < actions.size(); i++) {
            SocialAction a = actions.get(i);
            UIManager.option(String.valueOf(i + 1), Style.fit(a.getDisplayName(), 10),
                    rm.getSuccessChance(other, a) + "% chance");
        }
        UIManager.option("0", "Never mind");
        UIManager.prompt("\nSay> ");
        SocialAction action;
        try {
            int pick = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (pick < 1 || pick > actions.size()) {
                return CommandResult.NO_TICK;
            }
            action = actions.get(pick - 1);
        } catch (NumberFormatException e) {
            return CommandResult.NO_TICK;
        }

        sim.setCurrentAction(ActionState.SOCIALIZING);
        ctx.getLogger().logAnimation(sim);
        boolean landed = rm.performSocialAction(other, action);
        if (landed) {
            other.getSocial().increase(action.getSocialChange() / 2);
            other.getRelationshipManager().increaseRelationship(sim, action.getRelationshipChange() / 2);
            ctx.getLogger().log(sim.getName() + " and " + other.getName() + " have a lovely "
                    + action.getDisplayName().toLowerCase() + " at home.");
        } else {
            ctx.getLogger().log(sim.getName() + " tries to " + action.getDisplayName().toLowerCase()
                    + " with " + other.getName() + ", but it falls flat.");
        }
        return CommandResult.TICK_FORWARD;
    }

    /** Household members who are alive, playable, awake, and not the speaker. */
    static List<Sim> available(Sim speaker, List<Sim> household) {
        List<Sim> people = new ArrayList<>();
        for (Sim s : household) {
            if (s != speaker && s.getState() != SimState.DEAD && s.isPlayable()
                    && s.getCurrentAction() != ActionState.SLEEPING) {
                people.add(s);
            }
        }
        return people;
    }
}
