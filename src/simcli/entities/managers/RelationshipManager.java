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
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;

/**
 * Manages all social bonds, interactions, and romantic relationships for a Sim.
 */
public class RelationshipManager {
    private Sim owner;
    private Map<Sim, Integer> relationships;
    private List<Relationship> relationshipRegistry;
    private Sim spouse;
    private List<Sim> children;

    public RelationshipManager(Sim owner) {
        this.owner = owner;
        this.relationships = new HashMap<>();
        this.relationshipRegistry = new ArrayList<>();
        this.spouse = null;
        this.children = new ArrayList<>();
    }

    public List<Relationship> getRelationshipRegistry() { 
        return relationshipRegistry; 
    }
    
    public Sim getSpouse() { 
        return spouse; 
    }

    public void setSpouse(Sim spouse) {
        this.spouse = spouse;
    }
    
    public List<Sim> getChildren() {
        return children;
    }

    public void addChild(Sim child) {
        this.children.add(child);
    }
    
    public int getRelationship(Sim otherSim) {
        return relationships.getOrDefault(otherSim, 0);
    }

    public Map<Sim, Integer> getRelationships() {
        return relationships;
    }
    
    public void increaseRelationship(Sim otherSim, int amount) {
        int current = getRelationship(otherSim);
        int newScore = Math.max(0, Math.min(current + amount, GameConstants.MAX_RELATIONSHIP_SCORE));
        relationships.put(otherSim, newScore);
    }

    /**
     * Interacts with another Sim, modifying relationship scores.
     */
    public void interactWith(Sim otherSim, String action) {
        if (otherSim == owner) return;
        
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

        if (action.equalsIgnoreCase("chat")) {
            rel.setFriendshipScore(rel.getFriendshipScore() + 10);
        } else if (action.equalsIgnoreCase("flirt")) {
            rel.setFriendshipScore(rel.getFriendshipScore() + 25);
        } else if (action.equalsIgnoreCase("argue")) {
            rel.setFriendshipScore(rel.getFriendshipScore() - 15);
        }
        
        rel.updateStatus();
    }

    /**
     * Legacy social interaction handling — need boosts and basic trackers.
     */
    public void interactSocially(Sim otherSim) {
        if (otherSim == owner) return;
        relationships.putIfAbsent(otherSim, 0);
        
        int relBonus = GameConstants.RELATIONSHIP_BONUS;
        int socialBonus = GameConstants.SOCIAL_BONUS;
        int happinessBonus = GameConstants.HAPPINESS_BONUS;
        
        if (owner.hasTrait(Trait.SOCIALITE)) {
            relBonus = (int)(relBonus * GameConstants.BONUS_TIMES);
            socialBonus = (int)(socialBonus * GameConstants.BONUS_TIMES);
            happinessBonus = (int)(happinessBonus * GameConstants.BONUS_TIMES);
        }

        int current = relationships.get(otherSim);
        int newScore = Math.min(current + relBonus, GameConstants.MAX_RELATIONSHIP_SCORE);
        relationships.put(otherSim, newScore);

        owner.setCurrentAction(ActionState.SOCIALIZING);
        
        SimulationLogger.log(owner.getName() + " socializes with " + otherSim.getName() + ".");
        owner.increaseSocial(socialBonus);
        owner.decreaseEnergy(10);
        owner.increaseHappiness(happinessBonus);
        
        interactWith(otherSim, "chat");
    }

    /**
     * Evaluates and establishes a marriage bond between the owner and target.
     * @return boolean True if married.
     */
    public boolean marry(Sim otherSim) {
        if (otherSim == owner) return false;
        relationships.putIfAbsent(otherSim, 0);
        if (relationships.get(otherSim) >= GameConstants.MARRIAGE_THRESHOLD && this.spouse == null && otherSim.getRelationshipManager().getSpouse() == null) {
            this.spouse = otherSim;
            otherSim.getRelationshipManager().setSpouse(owner);
            SimulationLogger.log("\n*** WEDDING BELLS! " + owner.getName() + " and " + otherSim.getName() + " are now married! ***");
            return true;
        }
        return false;
    }

    /**
     * Produces a child if correctly married and passes the 50% success check.
     * @return The child Sim, or null if the attempt fails (50% chance).
     * @throws SimulationException if unmarried or same gender.
     */
    /**
     * Step 1: Checks the rules and rolls the 50% chance.
     * @return The Gender of the baby if successful, or null if it fails.
     * @throws SimulationException if unmarried or same gender.
     */
    public Gender attemptPregnancy() throws SimulationException {
        if (this.spouse == null) {
            throw new SimulationException(owner.getName() + " is not married and cannot reproduce.");
        }
        if (owner.getGender() == this.spouse.getGender()) {
            throw new SimulationException(owner.getName() + " and " + this.spouse.getName() + " are of the same gender and cannot reproduce biologically.");
        }

        // 50% success rate
        if (GameRandom.RANDOM.nextInt(100) < GameConstants.REPRODUCE_SUCCESS_CHANCE) {
            // Log the immediate success message before creating the baby
            SimulationLogger.log("\nSuccess! " + owner.getName() + " and " + this.spouse.getName() + " are expecting a baby!");
            return GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        } else {
            SimulationLogger.log("\n" + owner.getName() + " and " + this.spouse.getName() + " tried to have a baby, but were unsuccessful this time.");
            return null;
        }
    }

    /**
     * Step 2: Creates the actual Sim object using the name provided by the UI.
     */
    public Sim finalizeBaby(String childName, Gender childGender) {
        Sim child = new Sim(childName, 0, childGender);
        child.setChildSim(true);
        
        this.addChild(child);
        spouse.getRelationshipManager().addChild(child);
        
        // Updated to match the requested final format
        SimulationLogger.log("\n*** NEW LIFE! " + child.getName() + " has been born! ***");
        return child;
    }
}
