package simcli.world;

import java.util.ArrayList;
import java.util.List;

import simcli.entities.actors.Sim;
import simcli.world.interactables.Interactable;

/**
 * Abstract base class for all visitable locations in the game world.
 *
 * <p>Subclasses include {@link Residential} (homes with rooms),
 * {@link Commercial} (shops), and {@link Park} (social areas).
 * Each building holds a list of {@link Interactable} objects that the
 * player can interact with when visiting.</p>
 */
public abstract class Building {
    /** The only unlock rule: open once someone in the household has worked a shift. */
    public static final String OPENS_AFTER_FIRST_PAYCHECK = "firstPaycheck";

    private String name;
    private List<Interactable> interactables;
    private String opensAfter;

    public Building(String name) {
        this.name = name;
        this.interactables = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    // what has to happen before this building opens (null = always open)
    public String getOpensAfter() {
        return opensAfter;
    }

    public void setOpensAfter(String rule) {
        if (rule != null && !rule.equals(OPENS_AFTER_FIRST_PAYCHECK)) {
            throw new IllegalArgumentException("unknown opensAfter rule '" + rule + "' for " + name);
        }
        this.opensAfter = rule;
    }

    /**
     * Returns whether the household may visit this building yet.
     *
     * @param household every Sim in the household.
     * @return {@code true} if it's open to them.
     */
    public boolean isOpenTo(List<Sim> household) {
        if (opensAfter == null) {
            return true;
        }
        for (Sim sim : household) {
            if (sim.getCareerManager().getTotalShiftsWorked() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isResidential() {
        return false;
    }

    public abstract void enter(Sim sim);

    public void addInteractable(Interactable item) {
        this.interactables.add(item);
    }

    public List<Interactable> getInteractables() {
        return this.interactables;
    }
}