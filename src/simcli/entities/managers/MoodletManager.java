package simcli.entities.managers;

import simcli.entities.models.Moodlet;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks the {@link Moodlet}s a Sim is feeling and how long each has left.
 *
 * <p>Owned by {@link simcli.entities.actors.Sim}, which counts the total
 * towards the Sim's mood. A Sim with the Sunny Disposition reward feels
 * bad moodlets only half as much.</p>
 */
public class MoodletManager {

    /** Hours left on each active moodlet. */
    private final Map<Moodlet, Integer> active = new EnumMap<>(Moodlet.class);

    /** Whether bad moodlets count for half. */
    private boolean sunny;

    /**
     * Adds a moodlet for its full duration, or restarts it if already felt.
     *
     * @param moodlet the moodlet.
     */
    public void add(Moodlet moodlet) {
        active.put(moodlet, moodlet.getHours());
    }

    /**
     * Restores a moodlet with a given time left. Used when loading a save.
     *
     * @param moodlet   the moodlet.
     * @param hoursLeft hours left (ignored unless positive).
     */
    public void restore(Moodlet moodlet, int hoursLeft) {
        if (hoursLeft > 0) {
            active.put(moodlet, hoursLeft);
        }
    }

    /**
     * Removes a moodlet early.
     *
     * @param moodlet the moodlet.
     */
    public void remove(Moodlet moodlet) {
        active.remove(moodlet);
    }

    /**
     * Returns whether the Sim currently feels a moodlet.
     *
     * @param moodlet the moodlet.
     * @return {@code true} if active.
     */
    public boolean has(Moodlet moodlet) {
        return active.containsKey(moodlet);
    }

    /**
     * Returns the hours left on a moodlet.
     *
     * @param moodlet the moodlet.
     * @return hours left, or 0 if not active.
     */
    public int getHoursLeft(Moodlet moodlet) {
        return active.getOrDefault(moodlet, 0);
    }

    /** Counts one hour off every moodlet, dropping those that wear off. */
    public void tick() {
        active.replaceAll((m, hours) -> hours - 1);
        active.values().removeIf(hours -> hours <= 0);
    }

    /**
     * Returns how much a moodlet adds to mood for this Sim.
     *
     * @param moodlet the moodlet.
     * @return its mood value, halved for bad moodlets on a sunny Sim.
     */
    public int effectOf(Moodlet moodlet) {
        return sunny && moodlet.isNegative() ? moodlet.getMood() / 2 : moodlet.getMood();
    }

    /** @return the combined mood effect of every active moodlet. */
    public int getTotal() {
        int total = 0;
        for (Moodlet m : active.keySet()) {
            total += effectOf(m);
        }
        return total;
    }

    /** @return active moodlets, strongest effect first. */
    public List<Moodlet> getActive() {
        List<Moodlet> list = new ArrayList<>(active.keySet());
        list.sort((a, b) -> Math.abs(b.getMood()) - Math.abs(a.getMood()));
        return list;
    }

    /**
     * Sets whether bad moodlets count for half (the Sunny Disposition reward).
     *
     * @param sunny {@code true} to halve bad moodlets.
     */
    public void setSunny(boolean sunny) {
        this.sunny = sunny;
    }

    /** Removes every moodlet. */
    public void clear() {
        active.clear();
    }
}
