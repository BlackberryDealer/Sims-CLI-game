package simcli.entities.models;

public enum SocialAction {
    CHAT(10),
    FLIRT(25),
    ARGUE(-15);

    private final int relationshipChange;

    SocialAction(int relationshipChange) {
        this.relationshipChange = relationshipChange;
    }

    public int getRelationshipChange() {
        return relationshipChange;
    }
}
