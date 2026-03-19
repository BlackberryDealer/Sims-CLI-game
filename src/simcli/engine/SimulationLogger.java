package simcli.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * A central logger that domain objects can publish messages to.
 */
public class SimulationLogger {
    private static final List<String> logs = new ArrayList<>();
    private static simcli.entities.Sim simToAnimate = null;

    public static void log(String message) {
        logs.add(message);
    }

    public static void logWarning(String message) {
        logs.add("[WARNING] " + message);
    }

    public static void logAnimation(simcli.entities.Sim player) {
        simToAnimate = player;
    }
    
    public static void prompt(String message) {
        flushAndPrint();
        System.out.print(message);
    }

    public static void flushAndPrint() {
        for(String s : logs) {
            System.out.println(s);
        }
        logs.clear();
        if (simToAnimate != null) {
            simcli.ui.UIManager.displayActionAnimation(simToAnimate);
            simToAnimate = null;
        }
    }

}
