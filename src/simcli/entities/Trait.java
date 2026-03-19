package simcli.entities;

public enum Trait {
    ACTIVE(0.8),         // Uses less energy
    LAZY(1.5),           // Uses more energy
    FAST_LEARNER(1.0),   // Gains skills faster
    SOCIALITE(1.0);      // Better at socializing

    private final double energyDecayModifier;

    Trait(double energyDecayModifier) {
        this.energyDecayModifier = energyDecayModifier;
    }

    public double getEnergyDecayModifier() {
        return energyDecayModifier;
    }
}
