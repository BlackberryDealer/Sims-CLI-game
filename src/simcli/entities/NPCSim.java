package simcli.entities;

public class NPCSim extends Sim {
    private int relationshipScore;

    public NPCSim(String name, int age) {
        super(name, age, Math.random() < 0.5 ? Gender.MALE : Gender.FEMALE);
        this.relationshipScore = 0;
    }

    public int getRelationshipScore() {
        return relationshipScore;
    }

    public void increaseRelationship(int amount) {
        this.relationshipScore += amount;
    }
}
