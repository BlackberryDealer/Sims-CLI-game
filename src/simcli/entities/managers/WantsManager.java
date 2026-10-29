package simcli.entities.managers;

import simcli.entities.models.Fear;
import simcli.entities.models.Reward;
import simcli.entities.models.Want;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A Sim's wants and fear for today, the satisfaction points they've
 * earned, and the lifelong perks they've bought with them.
 *
 * <p>Owned by {@link simcli.entities.actors.Sim}. The wants are rolled and
 * checked by {@link simcli.engine.InnerLifeManager}.</p>
 */
public class WantsManager {

    /** The day today's wants were rolled, or -1 if never. */
    private int day = -1;
    private final List<Want> wants = new ArrayList<>();
    private final Set<Want> done = EnumSet.noneOf(Want.class);
    private Fear fear;
    private boolean fearCameTrue;
    private int points;
    private final Set<Reward> perks = EnumSet.noneOf(Reward.class);

    /**
     * Starts a new day's wants and fear.
     *
     * @param day   the day.
     * @param wants the Sim's wants for the day.
     * @param fear  the Sim's fear for the day, or {@code null} for none.
     */
    public void startDay(int day, List<Want> wants, Fear fear) {
        this.day = day;
        this.wants.clear();
        this.wants.addAll(wants);
        this.done.clear();
        this.fear = fear;
        this.fearCameTrue = false;
    }

    /** @return the day the current wants were rolled, or -1 if never. */
    public int getDay() {
        return day;
    }

    /** @return today's wants, in the order rolled. */
    public List<Want> getWants() {
        return Collections.unmodifiableList(wants);
    }

    /**
     * @param want a want.
     * @return {@code true} if it's done today.
     */
    public boolean isDone(Want want) {
        return done.contains(want);
    }

    /**
     * Marks one of today's wants done. Used by the inner-life manager and
     * when loading a save.
     *
     * @param want the want.
     */
    public void markDone(Want want) {
        done.add(want);
    }

    /** @return how many of today's wants are done. */
    public int getDoneCount() {
        return done.size();
    }

    /** @return today's fear, or {@code null}. */
    public Fear getFear() {
        return fear;
    }

    /** @return {@code true} if today's fear has come true. */
    public boolean hasFearCameTrue() {
        return fearCameTrue;
    }

    /** Records that today's fear came true. */
    public void markFearCameTrue() {
        fearCameTrue = true;
    }

    /** @return the Sim's unspent satisfaction points. */
    public int getPoints() {
        return points;
    }

    /**
     * Adds satisfaction points.
     *
     * @param amount the points to add.
     */
    public void addPoints(int amount) {
        points += amount;
    }

    /**
     * Takes away satisfaction points, never going below zero.
     *
     * @param amount the points to take.
     * @return how many were actually taken.
     */
    public int losePoints(int amount) {
        int lost = Math.min(points, amount);
        points -= lost;
        return lost;
    }

    /**
     * Sets the point balance. Used when loading a save.
     *
     * @param points the balance.
     */
    public void setPoints(int points) {
        this.points = Math.max(0, points);
    }

    /**
     * @param reward a reward.
     * @return {@code true} if the Sim owns this perk.
     */
    public boolean hasPerk(Reward reward) {
        return perks.contains(reward);
    }

    /**
     * Records that the Sim owns a perk.
     *
     * @param reward the perk.
     */
    public void addPerk(Reward reward) {
        perks.add(reward);
    }

    /** @return every perk the Sim owns. */
    public Set<Reward> getPerks() {
        return Collections.unmodifiableSet(perks);
    }
}
