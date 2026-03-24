package simcli.utils;

public final class GameConstants {
    private GameConstants() {
    } // Prevent instantiation

    // Sim
    public static final int STARTING_MONEY = 500;
    public static final int STARTING_INVENTORY_CAPACITY = 10;
    public static final int TEEN_AGE = 13;
    public static final int ADULT_AGE = 18;
    public static final int ELDER_AGE = 65;
    public static final int DEATH_AGE = 81;
    public static final double AGE_ENERGY_PENALTY_MULTIPLIER = 0.05;
    public static final int DAYS_PER_AGE_TICK = 3;

    // Bonus
    public static final int relBonus = 10;
    public static final int socialBonus = 25;
    public static final int funBonus = 10;
    public static final double bonusTimes = 1.5;

    // Needs Tracker
    public static final int STARVING_DEATH_THRESHOLD = 3;
    public static final int HUNGER_WARNING_LEVEL = 20;

    // Needs
    // Energy
    public static final int ENERGY_BASE_DECAY_RATE = 2;

    // Fun
    public static final int FUN_BASE_DECAY_RATE = 2;

    // Happiness
    public static final int HAPPINESS_BASE_DECAY_RATE = 2;

    // Hunger
    public static final int HUNGER_BASE_DECAY_RATE = 5;

    // Hygiene
    public static final int HYGIENE_BASE_DECAY_RATE = 5;

    // Social
    public static final int SOCIAL_BASE_DECAY_RATE = 5;

    // GameEngine
    public static final int TICKS_PER_DAY = 24;
    public static final int AUTOSAVE_INTERVAL_TICKS = 10;

    // Commands
    public static final int UPGRADE_CAPACITY_BONUS = 20;
    public static final int UPGRADE_COST = 500;
}
