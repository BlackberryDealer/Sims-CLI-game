package simcli.entities.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Relationship;
import simcli.entities.models.RelationshipStatus;
import simcli.entities.models.Reward;
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
 * <p>Keeps one {@link Relationship} per Sim the owner knows, holding the
 * relationship score (0–100) and the
 * {@link simcli.entities.models.RelationshipStatus} it implies. Also
 * handles marriage, reproduction, and child registration.</p>
 *
 * <p>Extracted from {@link Sim} to follow the Single Responsibility
 * Principle.</p>
 */
public class RelationshipManager {

    /** The Sim that owns this relationship manager. */
    private Sim owner;

    /** How the owner gets on with each Sim they know, keyed by that Sim. */
    private final Map<Sim, Relationship> relationships;

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
        this.relationships = new LinkedHashMap<>();
        this.spouse = null;
        this.children = new ArrayList<>();
    }

    /**
     * Returns the owner's relationship with another Sim, creating a new
     * {@link simcli.entities.models.RelationshipStatus#STRANGER} one if
     * they haven't met.
     *
     * @param otherSim the other Sim.
     * @return the relationship record.
     */
    public Relationship getRelationshipWith(Sim otherSim) {
        return relationships.computeIfAbsent(otherSim, Relationship::new);
    }

    /**
     * Returns the relationship status with another Sim (stranger if they
     * haven't met).
     *
     * @param otherSim the other Sim.
     * @return the status.
     */
    public RelationshipStatus getStatus(Sim otherSim) {
        Relationship rel = relationships.get(otherSim);
        return rel == null ? RelationshipStatus.STRANGER : rel.getStatus();
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
     * a marriage bond; the relationship with the spouse is marked
     * {@link simcli.entities.models.RelationshipStatus#MARRIED}.
     *
     * @param spouse the Sim to marry, or {@code null} to clear.
     */
    public void setSpouse(Sim spouse) {
        if (this.spouse != null && relationships.containsKey(this.spouse)) {
            relationships.get(this.spouse).setMarried(false);
        }
        this.spouse = spouse;
        if (spouse != null) {
            getRelationshipWith(spouse).setMarried(true);
        }
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
     * Returns the relationship score with another Sim.
     *
     * @param otherSim the target Sim.
     * @return the relationship score (0 if no prior interaction).
     */
    public int getRelationship(Sim otherSim) {
        Relationship rel = relationships.get(otherSim);
        return rel == null ? 0 : rel.getFriendshipScore();
    }

    /**
     * Returns the relationship score with every Sim the owner knows, as a
     * read-only snapshot.
     *
     * @return a map of target Sims to scores, in the order they were met.
     */
    public Map<Sim, Integer> getRelationships() {
        Map<Sim, Integer> scores = new LinkedHashMap<>();
        for (Map.Entry<Sim, Relationship> entry : relationships.entrySet()) {
            scores.put(entry.getKey(), entry.getValue().getFriendshipScore());
        }
        return Collections.unmodifiableMap(scores);
    }

    /**
     * Changes the relationship score with another Sim, clamped to
     * {@code [0, MAX_RELATIONSHIP_SCORE]}, and updates the status.
     *
     * @param otherSim the target Sim.
     * @param amount   the amount to add to the score (negative to lower it).
     */
    public void increaseRelationship(Sim otherSim, int amount) {
        Relationship rel = getRelationshipWith(otherSim);
        rel.setFriendshipScore(Math.min(rel.getFriendshipScore() + amount, GameConstants.MAX_RELATIONSHIP_SCORE));
        rel.updateStatus();
    }

    /**
     * Changes the relationship with another Sim by a social action's
     * relationship value, without any need effects.
     *
     * @param otherSim the Sim to interact with.
     * @param action   the social action to perform.
     */
    public void interactWith(Sim otherSim, SocialAction action) {
        if (otherSim == owner)
            return;
        increaseRelationship(otherSim, action.getRelationshipChange());
    }

    /**
     * Simple social interaction: applies need boosts, trait bonuses and a
     * {@link GameConstants#RELATIONSHIP_BONUS} relationship increase.
     *
     * @param otherSim the Sim to socialize with.
     */
    public void interactSocially(Sim otherSim) {
        if (otherSim == owner)
            return;

        int relBonus = GameConstants.RELATIONSHIP_BONUS;
        int socialBonus = GameConstants.SOCIAL_BONUS;
        int happinessBonus = GameConstants.HAPPINESS_BONUS;

        if (owner.hasTrait(Trait.SOCIALITE)) {
            relBonus = (int) (relBonus * GameConstants.BONUS_TIMES);
            socialBonus = (int) (socialBonus * GameConstants.BONUS_TIMES);
            happinessBonus = (int) (happinessBonus * GameConstants.BONUS_TIMES);
        }

        increaseRelationship(otherSim, relBonus);

        owner.setCurrentAction(ActionState.SOCIALIZING);

        SimulationLogger.getInstance().log(owner.getName() + " socializes with " + otherSim.getName() + ".");
        owner.increaseSocial(socialBonus);
        owner.decreaseEnergy(10);
        owner.increaseHappiness(happinessBonus);
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
     * points; Socialites are easier to win over
     * ({@link GameConstants#SOCIALITE_TARGET_BONUS}) and Lazy Sims harder
     * ({@link GameConstants#LAZY_TARGET_PENALTY}). The chance stays
     * between 5% and 95%.</p>
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
        int chance = base + charisma * GameConstants.CHARISMA_SUCCESS_BONUS_PER_LEVEL;
        // the other Sim's personality matters too
        if (otherSim.hasTrait(Trait.SOCIALITE)) {
            chance += GameConstants.SOCIALITE_TARGET_BONUS;
        }
        if (otherSim.hasTrait(Trait.LAZY)) {
            chance -= GameConstants.LAZY_TARGET_PENALTY;
        }
        if (owner.hasPerk(Reward.SILVER_TONGUE)) {
            chance += GameConstants.SILVER_TONGUE_BONUS;
        }
        return Math.max(5, Math.min(95, chance));
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
        getRelationshipWith(otherSim);
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
        if (getRelationship(otherSim) >= GameConstants.MARRIAGE_THRESHOLD && this.spouse == null
                && otherSim.getRelationshipManager().getSpouse() == null) {
            setSpouse(otherSim);
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
        int parentGeneration = spouse == null ? owner.getGeneration()
                : Math.max(owner.getGeneration(), spouse.getGeneration());
        child.setGeneration(parentGeneration + 1);

        this.addChild(child);
        if (spouse != null) {
            spouse.getRelationshipManager().addChild(child);
        }

        // Updated to match the requested final format
        SimulationLogger.getInstance().log("\n*** NEW LIFE! " + child.getName() + " has been born! ***");
        return child;
    }
}
