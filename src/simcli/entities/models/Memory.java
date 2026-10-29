package simcli.entities.models;

/**
 * A moment a Sim remembers — their first job, a wedding, a loss — and the
 * day it happened. Shown in the family album.
 */
public final class Memory {

    private final int day;
    private final String text;

    /**
     * Creates a memory.
     *
     * @param day  the day it happened.
     * @param text what happened, e.g. "Married Bob".
     */
    public Memory(int day, String text) {
        this.day = day;
        this.text = text;
    }

    /** @return the day it happened. */
    public int getDay() {
        return day;
    }

    /** @return what happened. */
    public String getText() {
        return text;
    }
}
