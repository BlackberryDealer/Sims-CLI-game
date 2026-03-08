package simcli.entities;

import simcli.engine.SimulationException;

public class NPCSim extends Sim {
    private int relationshipScore;

    public NPCSim(String name, int age) {
        super(name, age);
        this.relationshipScore = 0;
    }

    public int getRelationshipScore() {
        return relationshipScore;
    }

    public void increaseRelationship(int amount) {
        this.relationshipScore += amount;
    }

    @Override
    public void performActivity(String activityType) throws SimulationException {
        // NPCs don't do much active work yet in this basic implementation
    }
}
