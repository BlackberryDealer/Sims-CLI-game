package simcli.entities.models;

import simcli.entities.actors.Sim;

/**
 * Encapsulates the social connection a Sim holds with an opposing Sim entity.
 */
public class Relationship {
    private Sim targetSim;
    private int friendshipScore;
    private RelationshipStatus status;

    /**
     * Bootstraps a neutral relationship model.
     * @param targetSim The secondary Sim receiving the bond.
     */
    public Relationship(Sim targetSim) {
        this.targetSim = targetSim;
        this.friendshipScore = 0;
        this.status = RelationshipStatus.STRANGER;
    }

    /** @return Reference to the opposing Sim */
    public Sim getTargetSim() { return targetSim; }
    
    /** @return The underlying integer score (0-100 typical) */
    public int getFriendshipScore() { return friendshipScore; }
    
    /** @return The resolved categorical state */
    public RelationshipStatus getStatus() { return status; }

    /** Used by framework actions natively modifying arrays */
    public void setFriendshipScore(int friendshipScore) { 
        this.friendshipScore = friendshipScore; 
    }

    /**
     * Adjusts Enum status flag by re-verifying score thresholds.
     */
    public void updateStatus() {
        if (this.friendshipScore >= 80) {
            this.status = RelationshipStatus.ROMANTIC;
        } else if (this.friendshipScore >= 50) {
            this.status = RelationshipStatus.FRIEND;
        } else if (this.friendshipScore >= 20) {
            this.status = RelationshipStatus.ACQUAINTANCE;
        } else {
            this.status = RelationshipStatus.STRANGER;
        }
    }
}
