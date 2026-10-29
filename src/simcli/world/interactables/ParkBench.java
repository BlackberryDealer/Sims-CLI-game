package simcli.world.interactables;

import java.util.List;
import java.util.Scanner;

import simcli.engine.ActionCancelledException;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.engine.TimeManager;
import simcli.entities.actors.NPCSim;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.managers.RelationshipManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.SocialAction;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;

/**
 * Represents a park-bench interactable where the active Sim can socialize
 * with NPC visitors.
 *
 * <p>Previously held a direct reference to {@code GameEngine}, creating a
 * circular dependency through {@code WorldManager}. Now receives only the
 * fine-grained collaborators it actually needs: an {@link NPCManager} for
 * the NPC pool and a mutable {@code neighborhood} list for adding married
 * NPCs to the household.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Display the NPC socialization menu.</li>
 *     <li>Apply relationship and need changes for social actions.</li>
 *     <li>Handle marriage proposals — removes NPC from pool, adds to household.</li>
 * </ul>
 */
public class ParkBench implements Interactable {

    /** Provides and manages the pool of park NPCs. */
    private final NPCManager npcManager;

    /** The mutable household list — married NPCs are added here. */
    private final List<Sim> neighborhood;

    /**
     * Creates a new {@code ParkBench}.
     *
     * @param npcManager   manages the NPC visitor pool.
     * @param neighborhood the mutable household list for adding married NPCs.
     */
    public ParkBench(NPCManager npcManager, List<Sim> neighborhood) {
        this.npcManager  = npcManager;
        this.neighborhood = neighborhood;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        List<NPCSim> visitors = npcManager.getActiveNPCs();

        UIManager.title("City Park - who do you want to talk to?");
        for (int i = 0; i < visitors.size(); i++) {
            NPCSim npc = visitors.get(i);
            int relScore = sim.getRelationshipManager().getRelationship(npc);
            String who = Style.fit(npc.getName(), 9) + Style.fit("{k}" + npc.getAge() + ", "
                    + npc.getCareer().getTitle() + "{/}", 27) + Style.fit(personality(npc), 10);
            UIManager.option(String.valueOf(i + 1), who + Panel.bar(relScore, 10) + "  " + bond(relScore));
        }
        UIManager.option("0", "Go back");
        UIManager.prompt("\nTalk to> ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= visitors.size()) {
                NPCSim target = visitors.get(choice - 1);
                int relScore = sim.getRelationshipManager().getRelationship(target);

                RelationshipManager rm = sim.getRelationshipManager();
                SocialAction[] actions = {SocialAction.CHAT, SocialAction.JOKE, SocialAction.FLIRT, SocialAction.ARGUE};
                boolean canFlirt = rm.canFlirtWith(target);
                String[] hints = {
                        "safe and friendly",
                        "big boost if it lands (" + rm.getSuccessChance(target, SocialAction.JOKE) + "%)",
                        canFlirt ? "romantic, " + rm.getSuccessChance(target, SocialAction.FLIRT) + "% chance"
                                : "{k}" + flirtBlocker(sim, target) + "{/}",
                        "why would you?"};
                UIManager.title("Talking to " + target.getName());
                for (int i = 0; i < actions.length; i++) {
                    UIManager.option(String.valueOf(i + 1), Style.fit(actions[i].getDisplayName(), 10), hints[i]);
                }

                boolean canPropose = relScore >= GameConstants.MARRIAGE_THRESHOLD
                        && sim.getRelationshipManager().getSpouse() == null
                        && target.getRelationshipManager().getSpouse() == null;
                int proposeChoice = actions.length + 1;
                if (canPropose) {
                    UIManager.option(String.valueOf(proposeChoice), "{M}" + Panel.HEART + " Propose marriage{/}");
                } else if (relScore >= GameConstants.MARRIAGE_THRESHOLD - 20) {
                    UIManager.printMessage("  {k}(" + target.getName() + " seems to like you a lot...){/}");
                }
                UIManager.option("0", "Never mind");

                UIManager.prompt("\nSay> ");
                int actionChoice = Integer.parseInt(scanner.nextLine().trim());
                if (actionChoice == 0) {
                    throw new ActionCancelledException();
                }

                if (actionChoice > 0 && actionChoice <= actions.length) {
                    SocialAction selectedAction = actions[actionChoice - 1];
                    if (selectedAction == SocialAction.FLIRT && !canFlirt) {
                        UIManager.info(sim.getName() + " can't flirt with " + target.getName() + ": "
                                + flirtBlocker(sim, target) + ".");
                        throw new ActionCancelledException();
                    }
                    sim.setCurrentAction(ActionState.SOCIALIZING);
                    SimulationLogger.getInstance().logAnimation(sim);
                    boolean landed = rm.performSocialAction(target, selectedAction);
                    SimulationLogger.getInstance().log(describe(sim, target, selectedAction, landed));
                } else if (actionChoice == proposeChoice && canPropose) {
                    handleProposal(sim, target);
                } else {
                    UIManager.error("Invalid action selection.");
                    throw new ActionCancelledException();
                }

                int now = sim.getRelationshipManager().getRelationship(target);
                SimulationLogger.getInstance().log("Relationship with " + target.getName() + " is now "
                        + now + "/100 (" + Style.plain(bond(now)) + ").");
            } else {
                // went back (or picked nobody) — no time passes
                throw new ActionCancelledException();
            }
        } catch (NumberFormatException e) {
            UIManager.error("Invalid selection.");
            throw new ActionCancelledException();
        }
    }

    /** The visitor's personality, coloured by how easy they are to win over. */
    private static String personality(Sim npc) {
        if (npc.hasTrait(Trait.SOCIALITE)) return "{G}socialite{/}";
        if (npc.hasTrait(Trait.LAZY)) return "{Y}lazy{/}";
        return "{k}active{/}";
    }

    /** Says why the Sim can't flirt with the target right now. */
    private static String flirtBlocker(Sim sim, Sim target) {
        if (sim.getAge() < GameConstants.ADULT_AGE || target.getAge() < GameConstants.ADULT_AGE) {
            return "adults only";
        }
        if (sim.getRelationshipManager().getSpouse() != null) {
            return "you're married";
        }
        if (target.getRelationshipManager().getSpouse() != null) {
            return "they're married";
        }
        return "get to know them first (" + GameConstants.FLIRT_MIN_RELATIONSHIP + "+)";
    }

    /** One line describing how a social action went. */
    private static String describe(Sim sim, Sim target, SocialAction action, boolean landed) {
        String verb = action.getDisplayName().toLowerCase();
        if (landed) {
            return sim.getName() + " " + (action == SocialAction.ARGUE ? "argues" : verb + "s") + " with "
                    + target.getName() + (action == SocialAction.JOKE ? " - it lands!"
                    : action == SocialAction.FLIRT ? " - " + target.getName() + " blushes." : ".");
        }
        return sim.getName() + " tries to " + verb + " with " + target.getName()
                + ", but it falls flat. Awkward...";
    }

    /** Friendly label for a relationship score. */
    private static String bond(int score) {
        if (score >= GameConstants.MARRIAGE_THRESHOLD) return "{M}" + Panel.HEART + " soulmate{/}";
        if (score >= 80) return "{M}close friend{/}";
        if (score >= 50) return "{G}friend{/}";
        if (score >= 20) return "{Y}acquaintance{/}";
        return "{k}stranger{/}";
    }

    /**
     * Handles the marriage proposal flow. On success, removes the NPC from
     * the park pool, replenishes, and adds the new spouse to the household.
     *
     * @param sim    the proposing Sim.
     * @param target the NPC being proposed to.
     */
    private void handleProposal(Sim sim, NPCSim target) {
        boolean married = sim.getRelationshipManager().marry(target);
        if (married) {
            // marrying into the family makes them part of the same generation
            target.setGeneration(sim.getGeneration());
            npcManager.removeNPC(target);
            npcManager.replenishNPCs(GameConstants.MAX_NPCS);

            if (!neighborhood.contains(target)) {
                neighborhood.add(target);
                SimulationLogger.getInstance().log(target.getName() + " has joined the household and is now playable!");
            }
        } else {
            SimulationLogger.getInstance().log("The proposal was rejected. Build a stronger relationship first.");
        }
    }

    @Override
    public String getObjectName() {
        return "Socialize with NPCs";
    }
}
