package simcli.engine;

import simcli.entities.Sim;
import simcli.entities.SimState;

public class RandomEventManager {
    public static void trigger(Sim activePlayer, TimeManager timeManager) {
        if (activePlayer.getState() == SimState.DEAD) return;

        double chance = Math.random();
        if (chance < 0.05) { // 5% chance per tick to trigger an event
            double eventRoll = Math.random();
            if (eventRoll < 0.3) {
                SimulationLogger.log("\n*** RANDOM EVENT! You found $50 on the ground! ***");
                activePlayer.setMoney(activePlayer.getMoney() + 50);
                activePlayer.addTotalMoneyEarned(50);
            } else if (eventRoll < 0.6) {
                SimulationLogger.log("\n*** RANDOM EVENT! Oh no, you stubbed your toe! Happiness decreased. ***");
                activePlayer.getHappiness().decrease(15);
            } else if (eventRoll < 0.8) {
                SimulationLogger.log("\n*** RANDOM EVENT! A sudden stroke of inspiration! Energy restored. ***");
                activePlayer.getEnergy().increase(20);
            } else {
                SimulationLogger.log("\n*** RANDOM EVENT! You got a mild cold. All needs suffer slightly. ***");
                activePlayer.getHunger().decrease(10);
                activePlayer.getEnergy().decrease(10);
                activePlayer.getHygiene().decrease(10);
                activePlayer.getHappiness().decrease(10);
            }
        }
    }
}
