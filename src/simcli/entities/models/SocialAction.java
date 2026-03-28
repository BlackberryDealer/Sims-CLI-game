package simcli.entities.models;

/**
 * Represents the various social actions that a Sim can perform with another Sim.
 * Each action has predefined effects on relationship, happiness, energy, social stats, and skill XP.
 */
public enum SocialAction {
    /** A friendly chat that moderately increases relationship and social stats. */
    CHAT("Chat", 5, 10, -5, 30, 5),
    /** A joke that has a high chance of success, boosting relationship and social stats. */
    JOKE("Joke", 10, 15, -10, 70, 10),
    /** An argument that negatively impacts relationship and happiness. */
    ARGUE("Argue", -15, -10, -15, 20, 0),
    /** A romantic gesture that significantly improves relationship but requires more energy. */
    FLIRT("Flirt", 25, 20, -10, 50, 15);

    /** The human-readable name of the social action. */
    private final String displayName;
    /** The amount the relationship between Sims changes after this action. */
    private final int relationshipChange;
    /** The amount the Sim's happiness level changes after this action. */
    private final int happinessChange;
    /** The amount of energy consumed by performing this action (usually negative). */
    private final int energyChange;
    /** The amount the Sim's social need is restored by this action. */
    private final int socialChange;
    /** The amount of Charisma or Social skill XP gained from this action. */
    private final int skillXP;

    /**
     * Constructs a new SocialAction with the specified attribute changes.
     *
     * @param displayName the name to display for this action
     * @param relationshipChange the impact on Sim-to-Sim relationship
     * @param happinessChange the impact on the Sim's personal happiness
     * @param energyChange the energy cost or gain
     * @param socialChange the impact on the social need meter
     * @param skillXP the experience points gained for social skills
     */
    SocialAction(String displayName, int relationshipChange, int happinessChange, int energyChange, int socialChange, int skillXP) {
        this.displayName = displayName;
        this.relationshipChange = relationshipChange;
        this.happinessChange = happinessChange;
        this.energyChange = energyChange;
        this.socialChange = socialChange;
        this.skillXP = skillXP;
    }

    /** @return the display name of this social action */
    public String getDisplayName() { return displayName; }
    /** @return the relationship point change */
    public int getRelationshipChange() { return relationshipChange; }
    /** @return the happiness point change */
    public int getHappinessChange() { return happinessChange; }
    /** @return the energy cost or change */
    public int getEnergyChange() { return energyChange; }
    /** @return the social need restoration amount */
    public int getSocialChange() { return socialChange; }
    /** @return the skill experience points gained */
    public int getSkillXP() { return skillXP; }
}
