package simcli.utils;

public final class GameConstants {
    private GameConstants() {} // Prevent instantiation
    
    // Sim
    public static final int STARTING_MONEY = 500;
    public static final int STARTING_INVENTORY_CAPACITY = 10;
    public static final double AGE_ENERGY_PENALTY_MULTIPLIER = 0.05;
    public static final int DAYS_PER_AGE_TICK = 3;
    
    // Needs Tracker
    public static final int STARVING_DEATH_THRESHOLD = 3;
    public static final int HUNGER_WARNING_LEVEL = 20;
    
    // GameEngine
    public static final int TICKS_PER_DAY = 24;
    public static final int AUTOSAVE_INTERVAL_TICKS = 10;
    
    // Commands
    public static final int UPGRADE_CAPACITY_BONUS = 20;
    public static final int UPGRADE_COST = 500;
}
