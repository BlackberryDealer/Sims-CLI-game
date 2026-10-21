package simcli.entities.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Relationship;
import simcli.entities.models.SkillType;
import simcli.entities.models.Trait;
import simcli.entities.models.SocialAction;
import simcli.needs.Need;
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
     * Checks whether the owner may flirt with another Sim: both must be
     * adults, the owner must already know them
     * ({@link GameConstants#FLIRT_MIN_RELATIONSHIP}), and neither may be
     * married to someone else.
     *
     * @param otherSim the Sim to flirt with.
     * @return {@code true} if flirting is allowed.
     */
    public boolean canFlirtWith(Sim otherSim) {
        if (otherSim == owner) {
            return false;
        }
        Sim theirSpouse = otherSim.getRelationshipManager().getSpouse();
        return owner.getAge() >= GameConstants.ADULT_AGE
                && otherSim.getAge() >= GameConstants.ADULT_AGE
                && getRelationship(otherSim) >= GameConstants.FLIRT_MIN_RELATIONSHIP
                && (this.spouse == null || this.spouse == otherSim)
                && (theirSpouse == null || theirSpouse == owner);
    }

    /**
     * Returns the percentage chance (0–100) that a social action lands.
     *
     * <p>Chats and arguments always "land". Jokes succeed
     * {@link GameConstants#JOKE_BASE_SUCCESS_CHANCE}% of the time, and
     * flirts {@link GameConstants#FLIRT_BASE_SUCCESS_CHANCE}% plus half of
     * every relationship point above the flirting minimum. Each Charisma
     * level adds {@link GameConstants#CHARISMA_SUCCESS_BONUS_PER_LEVEL}
     * points. The chance never goes above 95%.</p>
     *
     * @param otherSim the Sim on the receiving end.
     * @param action   the social action.
     * @return the success chance, 0–100.
     */
    public int getSuccessChance(Sim otherSim, SocialAction action) {
        int base;
        switch (action) {
            case JOKE:
                base = GameConstants.JOKE_BASE_SUCCESS_CHANCE;
                break;
            case FLIRT:
                base = GameConstants.FLIRT_BASE_SUCCESS_CHANCE
                        + Math.max(0, getRelationship(otherSim) - GameConstants.FLIRT_MIN_RELATIONSHIP) / 2;
                break;
            default:
                return 100;
        }
        int charisma = owner.getSkillLevel(SkillType.CHARISMA);
        return Math.min(95, base + charisma * GameConstants.CHARISMA_SUCCESS_BONUS_PER_LEVEL);
    }

    /**
     * Performs a social action with another Sim and applies its effects.
     *
     * <p>If the action lands, the owner gains the action's happiness and
     * social values and the relationship changes by its relationship
     * value. Socialites get {@link GameConstants#BONUS_TIMES} of all of
     * these, and each Charisma level adds
     * {@link GameConstants#CHARISMA_RELATIONSHIP_BONUS_PER_LEVEL} to
     * positive relationship gains. If it falls flat, the relationship and
     * happiness drop a little instead. Friendly actions train Charisma
     * either way (half as much on a failure).</p>
     *
     * @param otherSim the Sim to interact with.
     * @param action   the social action to perform.
     * @return {@code true} if the action landed.
     */
    public boolean performSocialAction(Sim otherSim, SocialAction action) {
        if (otherSim == owner) {
            return false;
        }
        relationships.putIfAbsent(otherSim, 0);
        double traitMultiplier = owner.hasTrait(Trait.SOCIALITE) ? GameConstants.BONUS_TIMES : 1.0;
        boolean landed = GameRandom.RANDOM.nextInt(100) < getSuccessChance(otherSim, action);

        if (landed) {
            int relChange = (int) (action.getRelationshipChange() * traitMultiplier);
            if (relChange > 0) {
                int charisma = owner.getSkillLevel(SkillType.CHARISMA);
                relChange = (int) Math.round(relChange
                        * (1 + charisma * GameConstants.CHARISMA_RELATIONSHIP_BONUS_PER_LEVEL));
            }
            increaseRelationship(otherSim, relChange);
            changeNeed(owner.getHappiness(), (int) (action.getHappinessChange() * traitMultiplier));
            changeNeed(owner.getSocial(), (int) (action.getSocialChange() * traitMultiplier));
        } else {
            increaseRelationship(otherSim, -GameConstants.SOCIAL_FAIL_RELATIONSHIP_PENALTY);
            owner.getHappiness().decrease(GameConstants.SOCIAL_FAIL_HAPPINESS_PENALTY);
            owner.getSocial().increase(action.getSocialChange() / 2);
        }
        owner.getEnergy().decrease((int) (-action.getEnergyChange() * traitMultiplier));

        if (action.getRelationshipChange() > 0) {
            int xp = landed ? GameConstants.SOCIAL_CHARISMA_XP : GameConstants.SOCIAL_CHARISMA_XP / 2;
            owner.trainSkill(SkillType.CHARISMA, xp);
        }
        return landed;
    }

    /** Raises or lowers a need, keeping it within 0–100 either way. */
    private static void changeNeed(Need need, int amount) {
        if (amount >= 0) {
            need.increase(amount);
        } else {
            need.decrease(-amount);
        }
    }

    /**
     * Gives an item from the owner's bag to another Sim.
     *
     * <p>The item moves into the receiver's bag if it has room (otherwise
     * it is used up). The receiver gains {@link GameConstants#GIFT_HAPPINESS}
     * happiness and the relationship rises — in both directions — by
     * {@link GameConstants#GIFT_BASE_RELATIONSHIP} plus one point per
     * {@link GameConstants#GIFT_PRICE_PER_RELATIONSHIP_POINT} Simoleons the
     * item is worth, up to {@link GameConstants#GIFT_MAX_RELATIONSHIP}.</p>
     *
     * @param otherSim the Sim receiving the gift.
     * @param gift     an item from the owner's bag.
     * @return the relationship points gained.
     * @throws SimulationException if the item is not in the owner's bag.
     */
    public int giveGift(Sim otherSim, Item gift) throws SimulationException {
        if (otherSim == owner || !owner.getInventory().remove(gift)) {
            throw new SimulationException(owner.getName() + " doesn't have that to give.");
        }
        if (otherSim.getInventory().size() < otherSim.getInventoryCapacity()) {
            otherSim.addItem(gift);
        }
        int relGain = Math.min(GameConstants.GIFT_MAX_RELATIONSHIP, GameConstants.GIFT_BASE_RELATIONSHIP
                + gift.getPrice() / GameConstants.GIFT_PRICE_PER_RELATIONSHIP_POINT);
        increaseRelationship(otherSim, relGain);
        otherSim.getRelationshipManager().increaseRelationship(owner, relGain);
        otherSim.getHappiness().increase(GameConstants.GIFT_HAPPINESS);
        owner.getHappiness().increase(GameConstants.GIFT_HAPPINESS / 3);
        return relGain;
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
        // Newborns don't come with the adult starting cash.
        child.setMoney(0);
        child.setTotalMoneyEarned(0);

        this.addChild(child);
        spouse.getRelationshipManager().addChild(child);

        // Updated to match the requested final format
        SimulationLogger.getInstance().log("\n*** NEW LIFE! " + child.getName() + " has been born! ***");
        return child;
    }
}
