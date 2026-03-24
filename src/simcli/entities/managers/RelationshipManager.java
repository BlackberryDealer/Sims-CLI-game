package simcli.entities.managers;

import simcli.entities.actors.Sim;
import simcli.entities.models.Relationship;
import simcli.entities.models.RelationshipStatus;
import simcli.entities.models.Gender;
import simcli.engine.SimulationLogger;
import simcli.engine.SimulationException;
import simcli.utils.GameConstants;
import simcli.entities.models.Trait;
import simcli.entities.models.ActionState;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages all social bonds, interactions, and romantic relationships for a Sim.
 * Removes God-Object/Fat-Class functionality from the core Sim object.
 */
public class RelationshipManager {
    private Sim owner;
    private Map<Sim, Integer> relationships; // Legacy Tracker
    private List<Relationship> relationshipRegistry; // OOP Tracker
    private Sim spouse;

    /**
     * Initializes the Relationship component.
     * @param owner The Sim retaining this component.
     */
    public RelationshipManager(Sim owner) {
        this.owner = owner;
        this.relationships = new HashMap<>();
        this.relationshipRegistry = new ArrayList<>();
        this.spouse = null;
    }

    public List<Relationship> getRelationshipRegistry() { 
        return relationshipRegistry; 
    }
    
    public Sim getSpouse() { 
        return spouse; 
    }
    
    public int getRelationship(Sim otherSim) {
        return relationships.getOrDefault(otherSim, 0);
    }
    
    public void increaseRelationship(Sim otherSim, int amount) {
        relationships.put(otherSim, getRelationship(otherSim) + amount);
    }

    /**
     * Interacts with another Sim, modifying relationship scores.
     * @param otherSim The other Sim being interacted with.
     * @param action The string verb describing the action ("chat", "flirt", "argue").
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
     * Legacy social interaction handling need boosts and basic trackers.
     * @param otherSim The opposing Sim.
     */
    public void interactSocially(Sim otherSim) {
        if (otherSim == owner) return;
        relationships.putIfAbsent(otherSim, 0);
        
        int relBonus = GameConstants.RELATIONSHIP_BONUS;
        int socialBonus = GameConstants.SOCIAL_BONUS;
        int funBonus = GameConstants.FUN_BONUS;
        
        if (owner.hasTrait(Trait.SOCIALITE)) {
            relBonus = (int)(relBonus * GameConstants.BONUS_TIMES);
            socialBonus = (int)(socialBonus * GameConstants.BONUS_TIMES);
            funBonus = (int)(funBonus * GameConstants.BONUS_TIMES);
        }
        relationships.put(otherSim, relationships.get(otherSim) + relBonus);
        owner.setCurrentAction(ActionState.SOCIALIZING);
        
        SimulationLogger.log(owner.getName() + " socializes with " + otherSim.getName() + ".");
        owner.getSocial().increase(socialBonus);
        owner.getEnergy().decrease(10);
        owner.getFun().increase(funBonus);
        
        // Push legacy integration through new mechanic
        interactWith(otherSim, "chat");
    }

    /**
     * Evaluates and establishes a marriage bond between the owner and target.
     * @param otherSim Target sim.
     * @return boolean True if married.
     */
    public boolean marry(Sim otherSim) {
        if (otherSim == owner) return false;
        relationships.putIfAbsent(otherSim, 0);
        if (relationships.get(otherSim) >= GameConstants.MARRIAGE_THRESHOLD && this.spouse == null && otherSim.getRelationshipManager().getSpouse() == null) {
            this.spouse = otherSim;
            // Cross-update spouse manager
            otherSim.getRelationshipManager().setSpouseExternally(owner);
            SimulationLogger.log("\n*** WEDDING BELLS! " + owner.getName() + " and " + otherSim.getName() + " are now married! ***");
            return true;
        }
        return false;
    }

    /**
     * Private mutator allowing cross-assignment during marriage.
     */
    protected void setSpouseExternally(Sim spouse) {
        this.spouse = spouse;
    }

    /**
     * Produces a child if correctly married.
     * @return The child Sim.
     * @throws SimulationException if unmarried.
     */
    public Sim reproduce() throws SimulationException {
        if (this.spouse == null) {
            throw new SimulationException(owner.getName() + " is not married and cannot reproduce.");
        }
        if (owner.getGender() == this.spouse.getGender()) {
            throw new SimulationException(owner.getName() + " and " + this.spouse.getName() + " are of the same gender and cannot reproduce biologically.");
        }
        SimulationLogger.log("\n*** NEW LIFE! " + owner.getName() + " and " + this.spouse.getName() + " have had a baby! ***");
        return new Sim("Baby", 0, simcli.utils.GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE);
    }
}
