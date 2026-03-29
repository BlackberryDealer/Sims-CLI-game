package simcli.entities.models;

/**
 * Personality traits randomly assigned to a Sim at creation.
 * Each trait modifies gameplay behaviour (e.g. energy decay,
 * skill gain speed, or social interaction bonuses).
 */
public enum Trait {
    ACTIVE(0.8), // Uses less energy
    LAZY(1.5), // Uses more energy
    FAST_LEARNER(1.0), // Gains skills faster (Note: Checked externally by SkillManager)
    SOCIALITE(1.0); // Better at socializing (Note: Checked externally)

    private final double energyDecayModifier;

    Trait(double energyDecayModifier) {
        this.energyDecayModifier = energyDecayModifier;
    }

    public double getEnergyDecayModifier() {
        return energyDecayModifier;
    }
}
