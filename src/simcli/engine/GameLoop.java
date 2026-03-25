package simcli.engine;

import simcli.entities.actors.Sim;
import java.util.List;

public class GameLoop {
    private final TimeManager timeManager;
    private final List<Sim> neighborhood;
    private final RandomEventManager eventManager;

    public GameLoop(TimeManager timeManager, List<Sim> neighborhood, RandomEventManager eventManager) {
        this.timeManager = timeManager;
        this.neighborhood = neighborhood;
        this.eventManager = eventManager;
    }

    public void processTick(Sim activePlayer) {
        int previousDay = timeManager.getCurrentDay();
        
        for (Sim sim : neighborhood) {
            sim.tick();
        }
        
        timeManager.advanceTick();
        eventManager.trigger(activePlayer, timeManager);
        
        if (timeManager.getCurrentDay() > previousDay) {
            processDayBoundary();
        }
    }

    private void processDayBoundary() {
        simcli.ui.UIManager.printMessage("\n*** A new day has begun! (Day " + timeManager.getCurrentDay() + ") ***");
        for (Sim s : neighborhood) {
            s.growOlderDaily();
            s.getCareerManager().checkTruancy(s.getName());
        }
    }
}
