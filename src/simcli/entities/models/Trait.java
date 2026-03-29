package simcli.entities.models;

/**
 * Represents the Trait entity or state in the simulation.
 */
public enum Trait {
    ACTIVE(0.8),         // Uses less energy
    LAZY(1.5),           // Uses more energy
    FAST_LEARNER(1.0),   // Gains skills faster (Note: Checked externally by SkillManager)
    SOCIALITE(1.0);      // Better at socializing (Note: Checked externally)

    private final double energyDecayModifier;

    Trait(double energyDecayModifier) {
        this.energyDecayModifier = energyDecayModifier;
    }

    public double getEnergyDecayModifier() {
        return energyDecayModifier;
    }
}
