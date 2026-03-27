package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.SimState;
import simcli.utils.GameRandom;

public class RandomEventManager {
    public void trigger(Sim activePlayer, TimeManager timeManager) {
        if (activePlayer.getState() == SimState.DEAD) return;

        double chance = GameRandom.RANDOM.nextDouble();
        if (chance < 0.05) { // 5% chance per tick to trigger an event
            double eventRoll = GameRandom.RANDOM.nextDouble();
            if (eventRoll < 0.20) {
                SimulationLogger.log("\n*** RANDOM EVENT! You found $50 on the ground! ***");
                activePlayer.setMoney(activePlayer.getMoney() + 50);
                activePlayer.addTotalMoneyEarned(50);
            } else if (eventRoll < 0.35) {
                SimulationLogger.log("\n*** RANDOM EVENT! Oh no, you stubbed your toe! Happiness decreased. ***");
                activePlayer.getHappiness().decrease(15);
            } else if (eventRoll < 0.50) {
                SimulationLogger.log("\n*** RANDOM EVENT! A sudden stroke of inspiration! Energy restored. ***");
                activePlayer.getEnergy().increase(20);
            } else if (eventRoll < 0.65) {
                SimulationLogger.log("\n*** RANDOM EVENT! You got a mild cold. All needs suffer slightly. ***");
                activePlayer.getHunger().decrease(10);
                activePlayer.getEnergy().decrease(10);
                activePlayer.getHygiene().decrease(10);
                activePlayer.getHappiness().decrease(10);
                activePlayer.getSocial().decrease(10);
            } else if (eventRoll < 0.80) {
                // Stove catches fire (from proposal slide 24)
                SimulationLogger.log("\n*** RANDOM EVENT! The stove catches fire! ***");
                SimulationLogger.log("You rush to put it out, burning yourself in the process.");
                activePlayer.getHunger().decrease(20);
                activePlayer.getEnergy().decrease(25);
                activePlayer.setHealth(Math.max(0, activePlayer.getHealth() - 10));
            } else {
                // Lottery win (from proposal slide 24)
                int winnings = 100 + simcli.utils.GameRandom.RANDOM.nextInt(401); // $100-$500
                SimulationLogger.log("\n*** RANDOM EVENT! LOTTERY WIN! You won $" + winnings + "! ***");
                activePlayer.setMoney(activePlayer.getMoney() + winnings);
                activePlayer.addTotalMoneyEarned(winnings);
                activePlayer.getHappiness().increase(30);
            }
        }
    }
}
