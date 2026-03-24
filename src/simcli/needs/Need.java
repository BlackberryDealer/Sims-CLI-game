package simcli.needs;

import simcli.entities.actors.Sim;

/**
 * Abstract representation of a Sim's biological or emotional need.
 * Encapsulates limits and manages decay based on the Sim's context.
 */
public abstract class Need {
    private String name;
    private int value;
    private final int baseDecayRate;
    public static final int MAX_VALUE = 100;

    /**
     * Constructs a Need.
     * @param name The name of the need.
     * @param baseDecayRate The base integer amount by which the need decays.
     */
    public Need(String name, int baseDecayRate) {
        this.name = name;
        this.baseDecayRate = baseDecayRate;
        this.value = MAX_VALUE;
    }

    /**
     * Calculates the dynamic decay of this need per game tick 
     * based on the specific condition of the Sim instance and age multiplier.
     * @param sim The Sim instance whose need needs to decay.
     * @param multiplier Decay rate multiplier.
     */
    public abstract void calculateDecay(Sim sim, double multiplier);
    /**
     * Increases the need value safely up to MAX_VALUE limit.
     * @param amount The integer amount to increase by.
     */
    public void increase(int amount) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
    }

    /**
     * Decreases the need safely down to a minimum of 0.
     * @param amount The integer amount to decrease by.
     */
    public void decrease(int amount) {
        this.value = Math.max(this.value - amount, 0);
    }

    /**
     * Gets the base decay rate configured for this need.
     * @return The integer base base decay rate.
     */
    protected int getBaseDecayRate() {
        return this.baseDecayRate;
    }

    /**
     * Retrieves the current value of the need.
     * @return The current value (0 - 100).
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Sets the value of the need explicitly within safe boundaries.
     * @param value The value to set (will be clamped between 0 and 100).
     */
    public void setValue(int value) {
        this.value = Math.max(0, Math.min(value, MAX_VALUE));
    }

    /**
     * Retrieves the name of the need.
     * @return The Need name.
     */
    public String getName() {
        return this.name;
    }
}