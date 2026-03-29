package simcli.entities.models;

/**
 * Represents the WorkResult entity or state in the simulation.
 */
public class WorkResult {
    private final boolean success;
    private final String message;
    private final int earnings;
    private final boolean promoted;
    private final boolean overworked;

    private WorkResult(boolean success, String message, int earnings, boolean promoted, boolean overworked) {
        this.success = success;
        this.message = message;
        this.earnings = earnings;
        this.promoted = promoted;
        this.overworked = overworked;
    }

    public static WorkResult success(int earnings, boolean promoted, boolean overworked) {
        return new WorkResult(true, "Success", earnings, promoted, overworked);
    }

    public static WorkResult failure(String message) {
        return new WorkResult(false, message, 0, false, false);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getEarnings() { return earnings; }
    public boolean isPromoted() { return promoted; }
    public boolean isOverworked() { return overworked; }
}
