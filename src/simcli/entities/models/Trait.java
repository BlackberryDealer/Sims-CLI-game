package simcli.entities.models;

/**
 * Personality traits randomly assigned to a Sim at creation.
 *
 * <p>Each trait modifies gameplay behaviour through an energy decay
 * modifier. The {@link #SOCIALITE} trait is checked externally by
 * {@link simcli.entities.managers.RelationshipManager} for bonus
 * relationship and happiness gains during social interactions.</p>
 */
public enum Trait {

    /** Active Sims use less energy (0.8x decay modifier). */
    ACTIVE(0.8),

    /** Lazy Sims use more energy (1.5x decay modifier). */
    LAZY(1.5),

    /** Socialite Sims gain extra relationship and happiness from socializing (1.0x energy). */
    SOCIALITE(1.0);

    /** The multiplier applied to energy decay for this trait. */
    private final double energyDecayModifier;

    /**
     * Constructs a new Trait with the given energy decay modifier.
     *
     * @param energyDecayModifier the multiplier for energy decay rate.
     */
    Trait(double energyDecayModifier) {
        this.energyDecayModifier = energyDecayModifier;
    }

    /**
     * Returns the energy decay modifier for this trait.
     * Values below 1.0 slow decay; values above 1.0 accelerate it.
     *
     * @return the energy decay multiplier.
     */
    public double getEnergyDecayModifier() {
        return energyDecayModifier;
    }
}
