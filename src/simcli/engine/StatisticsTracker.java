package simcli.engine;

import simcli.entities.actors.Sim;

import java.util.List;

/**
 * Tracks aggregate session statistics (money earned, items bought).
 * Extracted from GameEngine to enforce the Single Responsibility Principle.
 */
public class StatisticsTracker {
    private int sessionTotalMoney;
    private int sessionTotalItems;

    public StatisticsTracker() {
        this.sessionTotalMoney = 0;
        this.sessionTotalItems = 0;
    }

    /**
     * Aggregates statistics across all Sims in the neighborhood.
     * Typically called on game over or final save.
     *
     * @param neighborhood the list of household Sims
     */
    public void aggregate(List<Sim> neighborhood) {
        this.sessionTotalMoney = 0;
        this.sessionTotalItems = 0;
        for (Sim sim : neighborhood) {
            this.sessionTotalMoney += sim.getTotalMoneyEarned();
            this.sessionTotalItems += sim.getTotalItemsBought();
        }
    }

    public int getSessionTotalMoney() {
        return sessionTotalMoney;
    }

    public int getSessionTotalItems() {
        return sessionTotalItems;
    }
}
