package simcli.ui.ascii;

import simcli.entities.models.ActionState;
import simcli.entities.actors.Sim;
import simcli.world.Building;

import simcli.ui.ascii.providers.*;

import java.util.HashMap;
import java.util.Map;

/**
 * UI component handing formatting or displaying of AsciiEngine.
 */
public class AsciiEngine {

    private Map<ActionState, IAsciiProvider> actionProviders;
    private IAsciiProvider houseProvider;
    private IAsciiProvider storeProvider;
    private IAsciiProvider defaultBuildingProvider;

    public AsciiEngine() {
        actionProviders = new HashMap<>();
        actionProviders.put(ActionState.SLEEPING, new SleepingAsciiProvider());
        actionProviders.put(ActionState.EATING, new EatingAsciiProvider());
        actionProviders.put(ActionState.WORKING, new WorkingAsciiProvider());
        actionProviders.put(ActionState.STUDYING, new StudyingAsciiProvider());
        actionProviders.put(ActionState.SOCIALIZING, new SocializingAsciiProvider());
        actionProviders.put(ActionState.PLAYING, new PlayingAsciiProvider()); // was missing

        houseProvider = new HouseAsciiProvider();
        storeProvider = new StoreAsciiProvider();
        defaultBuildingProvider = new DefaultBuildingAsciiProvider();
    }

    public String render(Sim player, Building location) {
        ActionState action = player.getCurrentAction();

        // Render Action if one is active
        if (action != null && action != ActionState.IDLE && actionProviders.containsKey(action)) {
            return actionProviders.get(action).getAsciiArt(player, location);
        }

        // Render Location if IDLE
        if (location != null) {
            if (location.isResidential()) {
                return houseProvider.getAsciiArt(player, location);
            }

            String locName = location.getName().toLowerCase();
            if (locName.contains("dorm") || locName.contains("home")) {
                return houseProvider.getAsciiArt(player, location);
            } else if (locName.contains("supermarket") || locName.contains("market")) {
                return storeProvider.getAsciiArt(player, location);
            }
        }

        return defaultBuildingProvider.getAsciiArt(player, location);
    }
}
