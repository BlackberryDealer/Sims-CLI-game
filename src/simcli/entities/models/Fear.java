package simcli.entities.models;

import simcli.entities.actors.Sim;

import java.util.function.BiPredicate;

/**
 * Something a Sim dreads today. Each morning every Sim picks up one fear;
 * if it comes true they lose satisfaction points and feel the
 * {@link Moodlet#WORST_FEAR} moodlet for a while.
 *
 * <p>Like {@link Want}s, fears are judged by comparing
 * {@link SimSnapshot}s in {@link simcli.engine.InnerLifeManager}.</p>
 */
public enum Fear {

    /** Being fired for missing work. */
    GETTING_FIRED("Getting fired", (was, now) -> now.timesFired > was.timesFired),

    /** Missing a day of work. */
    MISSING_WORK("Missing work", (was, now) -> now.daysMissed > was.daysMissed),

    /** Letting hunger get low. */
    GOING_HUNGRY("Going hungry", (was, now) -> now.hunger <= 15),

    /** Running out of energy. */
    EXHAUSTION("Running out of energy", (was, now) -> now.energy <= 10),

    /** Running low on money. */
    GOING_BROKE("Going broke", (was, now) -> now.money < 50),

    /** Being turned down by someone. */
    REJECTION("Being rejected", (was, now) -> now.anyRelationshipDroppedSince(was)),

    /** Someone close dying. */
    LOSING_A_LOVED_ONE("Losing a loved one",
            (was, now) -> !SimSnapshot.added(was.lostLovedOnes, now.lostLovedOnes).isEmpty());

    private final String text;
    private final BiPredicate<SimSnapshot, SimSnapshot> cameTrue;

    Fear(String text, BiPredicate<SimSnapshot, SimSnapshot> cameTrue) {
        this.text = text;
        this.cameTrue = cameTrue;
    }

    /** @return what the Sim fears, e.g. "Getting fired". */
    public String getText() {
        return text;
    }

    /**
     * Returns whether the fear has come true since it was rolled.
     *
     * @param atRoll the Sim when the fear was rolled.
     * @param now    the Sim now.
     * @return {@code true} if it came true.
     */
    public boolean cameTrue(SimSnapshot atRoll, SimSnapshot now) {
        return cameTrue.test(atRoll, now);
    }

    /**
     * Returns whether this fear makes sense for the Sim right now: a Sim
     * without a job can't fear losing it, and a hungry Sim's fear of
     * hunger would come true straight away.
     *
     * @param sim the Sim.
     * @return {@code true} if it could be rolled.
     */
    public boolean suits(Sim sim) {
        boolean employed = sim.canWork() && sim.getCareer() != Job.UNEMPLOYED;
        switch (this) {
            case GETTING_FIRED:
            case MISSING_WORK:
                return employed;
            case GOING_HUNGRY:
                return sim.getHunger().getValue() > 40;
            case EXHAUSTION:
                return sim.getEnergy().getValue() > 40;
            case GOING_BROKE:
                return sim.getMoney() >= 150;
            case LOSING_A_LOVED_ONE:
                return sim.getRelationshipManager().getSpouse() != null
                        || !sim.getRelationshipManager().getChildren().isEmpty();
            default:
                return true;
        }
    }
}
