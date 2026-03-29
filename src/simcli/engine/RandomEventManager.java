package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.SimState;
import simcli.utils.GameRandom;

/**
 * Fires random events each tick with a small probability.
 *
 * <p>Every tick, {@link #trigger(Sim, TimeManager)} is called by
 * {@link GameLoop}. There is a base 5&nbsp;% chance that an event fires;
 * the specific event is then chosen from a weighted distribution:</p>
 * <ul>
 *     <li><b>Found money</b> (20&nbsp;%) — +$50</li>
 *     <li><b>Stubbed toe</b> (15&nbsp;%) — happiness −15</li>
 *     <li><b>Stroke of inspiration</b> (15&nbsp;%) — energy +20</li>
 *     <li><b>Mild cold</b> (15&nbsp;%) — all needs −10</li>
 *     <li><b>Stove fire</b> (15&nbsp;%) — hunger −20, energy −25, health −10</li>
 *     <li><b>Lottery win</b> (20&nbsp;%) — +$100–500, happiness +30</li>
 * </ul>
 *
 * <p>Dead Sims are exempt from random events.</p>
 */
public class RandomEventManager {
    private final SimulationLogger logger;

    /**
     * Creates a new {@code RandomEventManager}.
     *
     * @param logger the simulation logger for event messages.
     */
    public RandomEventManager(SimulationLogger logger) {
        this.logger = logger;
    }

    /**
     * Rolls for a random event and applies its effects to the active player.
     *
     * @param activePlayer the currently controlled Sim (may be null-safe for dead Sims).
     * @param timeManager  the simulation clock (available for future time-based events).
     */
    public void trigger(Sim activePlayer, TimeManager timeManager) {
        if (activePlayer.getState() == SimState.DEAD) return;

        double chance = GameRandom.RANDOM.nextDouble();
        if (chance < 0.05) { // 5% chance per tick to trigger an event
            double eventRoll = GameRandom.RANDOM.nextDouble();
            if (eventRoll < 0.20) {
                logger.log("\n*** RANDOM EVENT! You found $50 on the ground! ***");
                activePlayer.setMoney(activePlayer.getMoney() + 50);
                activePlayer.addTotalMoneyEarned(50);
            } else if (eventRoll < 0.35) {
                logger.log("\n*** RANDOM EVENT! Oh no, you stubbed your toe! Happiness decreased. ***");
                activePlayer.getHappiness().decrease(15);
            } else if (eventRoll < 0.50) {
                logger.log("\n*** RANDOM EVENT! A sudden stroke of inspiration! Energy restored. ***");
                activePlayer.getEnergy().increase(20);
            } else if (eventRoll < 0.65) {
                logger.log("\n*** RANDOM EVENT! You got a mild cold. All needs suffer slightly. ***");
                activePlayer.getHunger().decrease(10);
                activePlayer.getEnergy().decrease(10);
                activePlayer.getHygiene().decrease(10);
                activePlayer.getHappiness().decrease(10);
                activePlayer.getSocial().decrease(10);
            } else if (eventRoll < 0.80) {
                // Stove catches fire (from proposal slide 24)
                logger.log("\n*** RANDOM EVENT! The stove catches fire! ***");
                logger.log("You rush to put it out, burning yourself in the process.");
                activePlayer.getHunger().decrease(20);
                activePlayer.getEnergy().decrease(25);
                activePlayer.setHealth(Math.max(0, activePlayer.getHealth() - 10));
            } else {
                // Lottery win (from proposal slide 24)
                int winnings = 100 + simcli.utils.GameRandom.RANDOM.nextInt(401); // $100-$500
                logger.log("\n*** RANDOM EVENT! LOTTERY WIN! You won $" + winnings + "! ***");
                activePlayer.setMoney(activePlayer.getMoney() + winnings);
                activePlayer.addTotalMoneyEarned(winnings);
                activePlayer.getHappiness().increase(30);
            }
        }
    }
}
