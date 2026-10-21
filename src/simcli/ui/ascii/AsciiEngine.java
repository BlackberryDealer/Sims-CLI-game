package simcli.ui.ascii;

import simcli.entities.models.ActionState;
import simcli.entities.actors.Sim;
import simcli.ui.Animator;
import simcli.ui.Style;
import simcli.world.Building;
import simcli.world.Commercial;
import simcli.world.Park;
import simcli.world.Residential;

import simcli.ui.ascii.providers.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The rendering engine responsible for selecting the correct ASCII art.
 * It uses the Strategy Pattern to dynamically map a Sim's current ActionState
 * to a specific IAsciiProvider. If the Sim is IDLE, it defaults to rendering
 * the Building they are currently inside.
 */
public class AsciiEngine {

    private final Map<ActionState, IAsciiProvider> actionProviders = new EnumMap<>(ActionState.class);
    private final IAsciiProvider dormProvider = new HouseAsciiProvider();
    private final IAsciiProvider bungalowProvider = new BungalowAsciiProvider();
    private final IAsciiProvider storeProvider = new StoreAsciiProvider();
    private final IAsciiProvider bookshopProvider = new BookshopAsciiProvider();
    private final IAsciiProvider parkProvider = new ParkAsciiProvider();
    private final IAsciiProvider defaultBuildingProvider = new DefaultBuildingAsciiProvider();

    public AsciiEngine() {
        actionProviders.put(ActionState.SLEEPING, new SleepingAsciiProvider());
        actionProviders.put(ActionState.EATING, new EatingAsciiProvider());
        actionProviders.put(ActionState.WORKING, new WorkingAsciiProvider());
        actionProviders.put(ActionState.STUDYING, new StudyingAsciiProvider());
        actionProviders.put(ActionState.SOCIALIZING, new SocializingAsciiProvider());
        actionProviders.put(ActionState.PLAYING, new PlayingAsciiProvider());
        actionProviders.put(ActionState.EXERCISING, new ExercisingAsciiProvider());
        actionProviders.put(ActionState.BATHING, new BathingAsciiProvider());
    }

    /**
     * Returns the still image for the Sim's current action, or for the
     * building they're in when idle — with colour tags already rendered.
     */
    public String render(Sim player, Building location) {
        return Style.render(providerFor(player, location).getAsciiArt(player, location));
    }

    /** Plays the animation for the Sim's current action (or shows the scene). */
    public void animate(Sim player, Building location) {
        List<String> frames = providerFor(player, location).getFrames(player, location);
        Animator.play(frames, 180);
    }

    private IAsciiProvider providerFor(Sim player, Building location) {
        ActionState action = player.getCurrentAction();
        if (action != null && actionProviders.containsKey(action)) {
            return actionProviders.get(action);
        }
        return locationProvider(location);
    }

    private IAsciiProvider locationProvider(Building location) {
        if (location instanceof Park) {
            return parkProvider;
        }
        if (location instanceof Residential) {
            // purchasable homes get the nicer house; the free starter home is the dorm
            return ((Residential) location).getPurchasePrice() > 0 ? bungalowProvider : dormProvider;
        }
        if (location instanceof Commercial) {
            String name = location.getName().toLowerCase();
            if (name.contains("book")) {
                return bookshopProvider;
            }
            if (name.contains("market") || name.contains("store") || name.contains("shop")) {
                return storeProvider;
            }
        }
        return defaultBuildingProvider;
    }
}
