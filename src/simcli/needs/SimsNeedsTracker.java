package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.SimState;
import simcli.engine.SimulationLogger;
import simcli.utils.GameConstants;

/**
 * Centralised tracker for all five Sim needs (Hunger, Energy, Hygiene,
 * Happiness, Social), health, and the resulting {@link SimState}.
 *
 * <p>Each tick, all needs decay based on the Sim's current activity,
 * cross-penalties are applied (e.g. low hygiene reduces social), and
 * health is drained or regenerated depending on hunger level.</p>
 */
public class SimsNeedsTracker {
    private Need hunger;
    private Need energy;
    private Need hygiene;
    private Need happiness;
    private Need social;
    private SimState state;
    private int health;
    private int starvingTicks;

    public SimsNeedsTracker() {
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.hygiene = new Hygiene();
        this.happiness = new Happiness();
        this.social = new Social();
        this.state = SimState.HEALTHY;
        this.health = 100;
        this.starvingTicks = 0;
    }

    /**
     * Processes one simulation tick: decays all needs, applies cross-penalties
     * between needs, drains or regenerates health, and updates the SimState.
     *
     * @param sim                the Sim whose needs are being tracked.
     * @param ageMultiplier      age-based decay modifier (older Sims decay faster).
     * @param stageEnergyModifier life-stage energy decay modifier.
     * @param simName            display name for warning log messages.
     */
    public void tick(Sim sim, double ageMultiplier, double stageEnergyModifier, String simName) {
        if (this.state == SimState.DEAD)
            return;

        this.hunger.calculateDecay(sim, ageMultiplier);
        this.energy.calculateDecay(sim, ageMultiplier * stageEnergyModifier);
        this.hygiene.calculateDecay(sim, ageMultiplier);
        this.happiness.calculateDecay(sim, ageMultiplier);
        this.social.calculateDecay(sim, ageMultiplier);
        this.applyCrossPenalties();
        this.applyHealthDrain(simName);
        this.updateState();
    }

    private void applyCrossPenalties() {
        if (this.hygiene.getValue() <= GameConstants.HYGIENE_PENALTY_THRESHOLD) {
            this.social.decrease(GameConstants.HYGIENE_SOCIAL_PENALTY_AMOUNT);
        }
        if (this.happiness.getValue() <= GameConstants.HAPPINESS_PENALTY_THRESHOLD) {
            this.energy.decrease(GameConstants.HAPPINESS_ENERGY_PENALTY_AMOUNT);
        }
        if (this.social.getValue() <= GameConstants.SOCIAL_PENALTY_THRESHOLD) {
            this.happiness.decrease(GameConstants.SOCIAL_HAPPINESS_PENALTY_AMOUNT);
            this.energy.decrease(GameConstants.SOCIAL_ENERGY_PENALTY_AMOUNT);
        }
    }

    /**
     * Drains health when hunger is critically low instead of causing instant death.
     * The longer the Sim stays starving, the faster health drains.
     * 
     * @param simName The name of the Sim (for logging warnings).
     */
    private void applyHealthDrain(String simName) {
        if (this.hunger.getValue() <= 0) {
            this.starvingTicks++;
            int healthLoss = GameConstants.HEALTH_BASE_DAMAGE
                    + (this.starvingTicks * GameConstants.HEALTH_ACCELERATED_DAMAGE_MULTIPLIER); // Accelerating damage
            this.health = Math.max(0, this.health - healthLoss);
            SimulationLogger.getInstance()
                    .logWarning(simName + " is STARVING! Health dropping rapidly! (-" + healthLoss + " HP)");
        } else if (this.hunger.getValue() <= GameConstants.HUNGER_WARNING_LEVEL) {
            // Slow health drain when hungry but not starving
            this.health = Math.max(0, this.health - GameConstants.HEALTH_ADDED_DAMAGE);
            this.starvingTicks = 0; // Reset accelerator when eating again
        } else {
            // Slowly regenerate health when well-fed
            this.starvingTicks = 0;
            if (this.health < 100) {
                this.health = Math.min(100, this.health + GameConstants.HEALTH_REGENERATION_RATE);
            }
        }
    }

    /**
     * Resolves value thresholds into strict Enum definitions sequentially.
     * Death now only occurs when health reaches 0, not from any single need.
     */
    public void updateState() {
        if (this.health <= 0) {
            this.state = SimState.DEAD;
        } else if (this.energy.getValue() <= GameConstants.ENERGY_WARNING_LEVEL) {
            this.state = SimState.TIRED;
        } else if (this.hunger.getValue() <= GameConstants.HUNGER_WARNING_LEVEL) {
            this.state = SimState.HUNGRY;
        } else {
            this.state = SimState.HEALTHY;
        }
    }

    public void increaseSocial(int amount) {
        this.social.increase(amount);
    }

    /**
     * Decreases the Sim's energy by the given amount.
     *
     * @param amount the energy points to drain.
     */
    public void decreaseEnergy(int amount) {
        this.energy.decrease(amount);
    }

    public void increaseHappiness(int amount) {
        this.happiness.increase(amount);
    }

    public Need getHunger() {
        return hunger;
    }

    public Need getEnergy() {
        return energy;
    }

    public Need getHygiene() {
        return hygiene;
    }

    public Need getHappiness() {
        return happiness;
    }

    public Need getSocial() {
        return social;
    }

    public SimState getState() {
        return state;
    }

    public void setState(SimState state) {
        this.state = state;
    }

    public int getHealth() {
        return health;
    }

    public int getStarvingTicks() {
        return starvingTicks;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
