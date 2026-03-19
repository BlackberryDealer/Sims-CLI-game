package simcli.engine;

import simcli.entities.Sim;
import simcli.entities.SimState;

public class RandomEventManager {
    public void trigger(Sim activePlayer, TimeManager timeManager) {
        if (activePlayer.getState() == SimState.DEAD) return;

        double chance = simcli.utils.GameRandom.RANDOM.nextDouble();
        if (chance < 0.05) { // 5% chance per tick to trigger an event
            double eventRoll = simcli.utils.GameRandom.RANDOM.nextDouble();
            if (eventRoll < 0.3) {
                SimulationLogger.log("\n*** RANDOM EVENT! You found $50 on the ground! ***");
                activePlayer.setMoney(activePlayer.getMoney() + 50);
                activePlayer.addTotalMoneyEarned(50);
            } else if (eventRoll < 0.6) {
                SimulationLogger.log("\n*** RANDOM EVENT! Oh no, you stubbed your toe! Fun decreased. ***");
                activePlayer.getFun().decrease(15);
            } else if (eventRoll < 0.8) {
                SimulationLogger.log("\n*** RANDOM EVENT! A sudden stroke of inspiration! Energy restored. ***");
                activePlayer.getEnergy().increase(20);
            } else {
                SimulationLogger.log("\n*** RANDOM EVENT! You got a mild cold. All needs suffer slightly. ***");
                activePlayer.getHunger().decrease(10);
                activePlayer.getEnergy().decrease(10);
                activePlayer.getHygiene().decrease(10);
                activePlayer.getFun().decrease(10);
                activePlayer.getSocial().decrease(10);
            }
        }
    }
}
