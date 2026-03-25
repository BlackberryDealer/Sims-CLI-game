package simcli.entities.models;

public enum SocialAction {
    CHAT("Chat", 5, 10, -5, 30, 5),
    JOKE("Joke", 10, 15, -10, 70, 10),
    ARGUE("Argue", -15, -10, -15, 20, 0),
    FLIRT("Flirt", 25, 20, -10, 50, 15);

    private final String displayName;
    private final int relationshipChange;
    private final int happinessChange;
    private final int energyChange;
    private final int socialChange;
    private final int skillXP;

    SocialAction(String displayName, int relationshipChange, int happinessChange, int energyChange, int socialChange, int skillXP) {
        this.displayName = displayName;
        this.relationshipChange = relationshipChange;
        this.happinessChange = happinessChange;
        this.energyChange = energyChange;
        this.socialChange = socialChange;
        this.skillXP = skillXP;
    }

    public String getDisplayName() { return displayName; }
    public int getRelationshipChange() { return relationshipChange; }
    public int getHappinessChange() { return happinessChange; }
    public int getEnergyChange() { return energyChange; }
    public int getSocialChange() { return socialChange; }
    public int getSkillXP() { return skillXP; }
}
