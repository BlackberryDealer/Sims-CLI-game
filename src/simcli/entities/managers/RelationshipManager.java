package simcli.entities.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Relationship;
import simcli.entities.models.Trait;
import simcli.entities.models.SocialAction;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;

/**
 * Manages all social bonds, interactions, and romantic relationships
 * for a single Sim.
 *
 * <p>Tracks both a legacy integer-based relationship map (used for
 * quick threshold checks) and a richer {@link Relationship} registry
 * that models progression through {@link simcli.entities.models.RelationshipStatus}
 * tiers. Also handles marriage, reproduction, and child registration.</p>
 *
 * <p>Extracted from {@link Sim} to follow the Single Responsibility
 * Principle.</p>
 */
public class RelationshipManager {

    /** The Sim that owns this relationship manager. */
    private Sim owner;

    /** Legacy integer-based relationship scores keyed by target Sim. */
    private Map<Sim, Integer> relationships;

    /** Rich relationship model objects for detailed status tracking. */
    private List<Relationship> relationshipRegistry;

    /** The owner's spouse, or {@code null} if unmarried. */
    private Sim spouse;

    /** The list of children produced by the owner. */
    private List<Sim> children;

    /**
     * Constructs a new RelationshipManager for the given Sim.
     *
     * @param owner the Sim that owns this relationship manager.
     */
    public RelationshipManager(Sim owner) {
        this.owner = owner;
        this.relationships = new HashMap<>();
        this.relationshipRegistry = new ArrayList<>();
        this.spouse = null;
        this.children = new ArrayList<>();
    }

    /**
     * Returns the full list of rich {@link Relationship} objects
     * tracked by this manager.
     *
     * @return the relationship registry.
     */
    public List<Relationship> getRelationshipRegistry() {
        return relationshipRegistry;
    }

    /**
     * Returns the owner's spouse, or {@code null} if unmarried.
     *
     * @return the spouse Sim, or {@code null}.
     */
    public Sim getSpouse() {
        return spouse;
    }

    /**
     * Sets the owner's spouse. Used when establishing or restoring
     * a marriage bond.
     *
     * @param spouse the Sim to marry, or {@code null} to clear.
     */
    public void setSpouse(Sim spouse) {
        this.spouse = spouse;
    }

    /**
     * Returns the list of children produced by the owner.
     *
     * @return the children list.
     */
    public List<Sim> getChildren() {
        return children;
    }

    /**
     * Registers a new child under this relationship manager.
     *
     * @param child the child Sim to add.
     */
    public void addChild(Sim child) {
        this.children.add(child);
    }

    /**
     * Returns the legacy integer relationship score with another Sim.
     *
     * @param otherSim the target Sim.
     * @return the relationship score (0 if no prior interaction).
     */
    public int getRelationship(Sim otherSim) {
        return relationships.getOrDefault(otherSim, 0);
    }

    /**
     * Returns the full map of legacy relationship scores.
     *
     * @return a map of target Sims to integer scores.
     */
    public Map<Sim, Integer> getRelationships() {
        return relationships;
    }

    /**
     * Increases the legacy relationship score with another Sim,
     * clamped to {@code [0, MAX_RELATIONSHIP_SCORE]}.
     *
     * @param otherSim the target Sim.
     * @param amount   the amount to add to the score.
     */
    public void increaseRelationship(Sim otherSim, int amount) {
        int current = getRelationship(otherSim);
        int newScore = Math.max(0, Math.min(current + amount, GameConstants.MAX_RELATIONSHIP_SCORE));
        relationships.put(otherSim, newScore);
    }

    /**
     * Performs a social action with another Sim, updating the rich
     * {@link Relationship} model's friendship score and status.
     *
     * <p>If no relationship exists yet, a new one is created as
     * {@link simcli.entities.models.RelationshipStatus#STRANGER}.</p>
     *
     * @param otherSim the Sim to interact with.
     * @param action   the social action to perform.
     */
    public void interactWith(Sim otherSim, SocialAction action) {
        if (otherSim == owner)
            return;

        Relationship rel = null;
        for (Relationship r : relationshipRegistry) {
            if (r.getTargetSim() == otherSim) {
                rel = r;
                break;
            }
        }
        if (rel == null) {
            rel = new Relationship(otherSim);
            relationshipRegistry.add(rel);
        }

        rel.setFriendshipScore(rel.getFriendshipScore() + action.getRelationshipChange());

        rel.updateStatus();
    }

    /**
     * Legacy social interaction handling — applies need boosts, trait
     * bonuses, and basic relationship score increases. Also delegates
     * to {@link #interactWith(Sim, SocialAction)} for the rich model.
     *
     * @param otherSim the Sim to socialize with.
     */
    public void interactSocially(Sim otherSim) {
        if (otherSim == owner)
            return;
        relationships.putIfAbsent(otherSim, 0);

        int relBonus = GameConstants.RELATIONSHIP_BONUS;
        int socialBonus = GameConstants.SOCIAL_BONUS;
        int happinessBonus = GameConstants.HAPPINESS_BONUS;

        if (owner.hasTrait(Trait.SOCIALITE)) {
            relBonus = (int) (relBonus * GameConstants.BONUS_TIMES);
            socialBonus = (int) (socialBonus * GameConstants.BONUS_TIMES);
            happinessBonus = (int) (happinessBonus * GameConstants.BONUS_TIMES);
        }

        int current = relationships.get(otherSim);
        int newScore = Math.min(current + relBonus, GameConstants.MAX_RELATIONSHIP_SCORE);
        relationships.put(otherSim, newScore);

        owner.setCurrentAction(ActionState.SOCIALIZING);

        SimulationLogger.getInstance().log(owner.getName() + " socializes with " + otherSim.getName() + ".");
        owner.increaseSocial(socialBonus);
        owner.decreaseEnergy(10);
        owner.increaseHappiness(happinessBonus);

        interactWith(otherSim, SocialAction.CHAT);
    }

    /**
     * Evaluates and establishes a marriage bond between the owner and
     * the target Sim.
     *
     * <p>Marriage requires a relationship score of at least
     * {@link GameConstants#MARRIAGE_THRESHOLD}, and neither Sim may
     * already be married.</p>
     *
     * @param otherSim the Sim to marry.
     * @return {@code true} if the marriage was successful.
     */
    public boolean marry(Sim otherSim) {
        if (otherSim == owner)
            return false;
        relationships.putIfAbsent(otherSim, 0);
        if (relationships.get(otherSim) >= GameConstants.MARRIAGE_THRESHOLD && this.spouse == null
                && otherSim.getRelationshipManager().getSpouse() == null) {
            this.spouse = otherSim;
            otherSim.getRelationshipManager().setSpouse(owner);
            SimulationLogger.getInstance().log(
                    "\n*** WEDDING BELLS! " + owner.getName() + " and " + otherSim.getName() + " are now married! ***");
            return true;
        }
        return false;
    }

    /**
     * Attempts to conceive a child with the owner's spouse.
     *
     * <p>Step 1 of the two-step reproduction process: checks the
     * eligibility rules (must be married, different genders) and rolls
     * a 50% success chance. If successful, returns the randomly
     * determined gender of the baby.</p>
     *
     * @return the {@link Gender} of the baby if successful, or
     *         {@code null} if the attempt fails.
     * @throws SimulationException if the owner is unmarried or the
     *         couple is of the same gender.
     */
    public Gender attemptPregnancy() throws SimulationException {
        if (this.spouse == null) {
            throw new SimulationException(owner.getName() + " is not married and cannot reproduce.");
        }
        if (owner.getGender() == this.spouse.getGender()) {
            throw new SimulationException(owner.getName() + " and " + this.spouse.getName()
                    + " are of the same gender and cannot reproduce biologically.");
        }

        // 50% success rate
        if (GameRandom.RANDOM.nextInt(100) < GameConstants.REPRODUCE_SUCCESS_CHANCE) {
            // Log the immediate success message before creating the baby
            SimulationLogger.getInstance()
                    .log("\nSuccess! " + owner.getName() + " and " + this.spouse.getName() + " are expecting a baby!");
            return GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        } else {
            SimulationLogger.getInstance().log("\n" + owner.getName() + " and " + this.spouse.getName()
                    + " tried to have a baby, but were unsuccessful this time.");
            return null;
        }
    }

    /**
     * Creates the actual child Sim from the pregnancy result.
     *
     * <p>Step 2 of the two-step reproduction process: receives the
     * baby's name (chosen by the player via the UI) and gender (from
     * {@link #attemptPregnancy()}), creates the Sim, and registers
     * the child under both parents.</p>
     *
     * @param childName   the name chosen for the baby.
     * @param childGender the gender determined during pregnancy.
     * @return the newly created child {@link Sim}.
     */
    public Sim finalizeBaby(String childName, Gender childGender) {
        Sim child = new Sim(childName, 0, childGender);
        child.setChildSim(true);

        this.addChild(child);
        spouse.getRelationshipManager().addChild(child);

        // Updated to match the requested final format
        SimulationLogger.getInstance().log("\n*** NEW LIFE! " + child.getName() + " has been born! ***");
        return child;
    }
}
