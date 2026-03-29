package simcli.entities.models;

import simcli.entities.actors.Sim;

/**
 * Encapsulates the social connection between two Sims, tracking a
 * numeric friendship score and a categorical {@link RelationshipStatus}.
 *
 * <p>The friendship score ranges from 0 to 100. As the score changes
 * via social interactions, calling {@link #updateStatus()} re-evaluates
 * the categorical status against predefined thresholds.</p>
 */
public class Relationship {

    /** The other Sim in this relationship. */
    private Sim targetSim;

    /** The numeric friendship score (0–100). */
    private int friendshipScore;

    /** The categorical status derived from the friendship score. */
    private RelationshipStatus status;

    /**
     * Constructs a new relationship with a starting score of 0
     * and status of {@link RelationshipStatus#STRANGER}.
     *
     * @param targetSim the other Sim in this relationship.
     */
    public Relationship(Sim targetSim) {
        this.targetSim = targetSim;
        this.friendshipScore = 0;
        this.status = RelationshipStatus.STRANGER;
    }

    /**
     * Returns the other Sim in this relationship.
     *
     * @return the target Sim.
     */
    public Sim getTargetSim() { return targetSim; }

    /**
     * Returns the numeric friendship score (0–100).
     *
     * @return the friendship score.
     */
    public int getFriendshipScore() { return friendshipScore; }

    /**
     * Returns the current categorical relationship status.
     *
     * @return the {@link RelationshipStatus}.
     */
    public RelationshipStatus getStatus() { return status; }

    /**
     * Sets the friendship score, clamped to the range [0, 100].
     *
     * @param friendshipScore the new friendship score.
     */
    public void setFriendshipScore(int friendshipScore) {
        this.friendshipScore = Math.max(0, Math.min(friendshipScore, 100));
    }

    /**
     * Re-evaluates the categorical status based on the current
     * friendship score thresholds:
     * <ul>
     *     <li>80+ → {@link RelationshipStatus#ROMANTIC}</li>
     *     <li>50–79 → {@link RelationshipStatus#FRIEND}</li>
     *     <li>20–49 → {@link RelationshipStatus#ACQUAINTANCE}</li>
     *     <li>0–19 → {@link RelationshipStatus#STRANGER}</li>
     * </ul>
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
