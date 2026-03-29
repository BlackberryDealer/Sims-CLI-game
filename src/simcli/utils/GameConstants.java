package simcli.utils;

/**
 * Global constants used throughout the Sims CLI game.
 * This class contains configuration values for game mechanics, Sim attributes, 
 * needs decay, and various thresholds.
 */
public final class GameConstants {
    /** Private constructor to prevent instantiation of this utility class. */
    private GameConstants() {
    }

    // --- GameEngine ---
    /** The number of simulation ticks that represent one full day. */
    public static final int TICKS_PER_DAY = 24;
    /** The interval (in ticks) at which the game state is automatically saved. */
    public static final int AUTOSAVE_INTERVAL_TICKS = 10;
    /** The hour of the day (in 24h ticks) that a sleeping Sim wakes up. */
    public static final int MORNING_HOUR = 8;
    /** The maximum number of NPCs that can be in the park pool at once. */
    public static final int MAX_NPCS = 8;

    // --- Commands ---
    /** The bonus capacity added to inventory when upgraded. */
    public static final int UPGRADE_CAPACITY_BONUS = 20;
    /** The cost in money to perform an inventory upgrade. */
    public static final int UPGRADE_COST = 500;

    // --- Sim ---
    /** The amount of money a Sim starts with in a new game. */
    public static final int STARTING_MONEY = 500;
    /** The initial capacity of a Sim's inventory. */
    public static final int STARTING_INVENTORY_CAPACITY = 10;
    /** The age at which a Sim transitions from child to teen. */
    public static final int TEEN_AGE = 13;
    /** The age at which a Sim transitions from teen to adult. */
    public static final int ADULT_AGE = 18;
    /** The age at which a Sim transitions from adult to elder. */
    public static final int ELDER_AGE = 65;
    /** The base age at which a Sim is likely to die of old age. */
    public static final int DEATH_AGE = 90;
    /** Multiplier applied to energy costs as a Sim gets older. */
    public static final double AGE_ENERGY_PENALTY_MULTIPLIER = 0.05;
    /** How many game days pass for each year of age added to a Sim. */
    public static final int DAYS_PER_AGE_TICK = 3;
    /** The amount of pension income an elder Sim receives per tick. */
    public static final int RETIREMENT_PENSION_INCOME_AMOUNT = 100;

    // --- Relationship Manager ---
    /** Base bonus for positive relationship interactions. */
    public static final int RELATIONSHIP_BONUS = 10;
    /** Base bonus for social need restoration during interactions. */
    public static final int SOCIAL_BONUS = 25;
    /** Base bonus for happiness during positive social interactions. */
    public static final int HAPPINESS_BONUS = 10;
    /** Multiplier for social bonuses under certain conditions (e.g., traits). */
    public static final double BONUS_TIMES = 1.5;
    /** The relationship score required for two Sims to get married. */
    public static final int MARRIAGE_THRESHOLD = 100;
    /** The maximum possible relationship score between two Sims. */
    public static final int MAX_RELATIONSHIP_SCORE = 100;
    /** The percentage chance (0-100) of a successful reproduction attempt. */
    public static final int REPRODUCE_SUCCESS_CHANCE = 50;

    // --- Needs Tracker ---
    /** The number of ticks a Sim can survive at zero hunger before dying. */
    public static final int STARVING_DEATH_THRESHOLD = 3;
    /** The level below which a hunger warning is displayed to the user. */
    public static final int HUNGER_WARNING_LEVEL = 20;
    /** The level below which an energy warning is displayed to the user. */
    public static final int ENERGY_WARNING_LEVEL = 20;

    // --- Cross Penalty ---
    /** Hygiene level below which a relationship/social penalty is applied. */
    public static final int HYGIENE_PENALTY_THRESHOLD = 10;
    /** Amount of social penalty applied when hygiene is too low. */
    public static final int HYGIENE_SOCIAL_PENALTY_AMOUNT = 5;
    /** Happiness level below which an energy penalty is applied. */
    public static final int HAPPINESS_PENALTY_THRESHOLD = 15;
    /** Amount of energy penalty applied when happiness is too low. */
    public static final int HAPPINESS_ENERGY_PENALTY_AMOUNT = 3;
    /** Social level below which a happiness penalty is applied. */
    public static final int SOCIAL_PENALTY_THRESHOLD = 10;
    /** Amount of happiness penalty applied when social need is too low. */
    public static final int SOCIAL_HAPPINESS_PENALTY_AMOUNT = 3;
    /** Amount of energy penalty applied when social need is too low. */
    public static final int SOCIAL_ENERGY_PENALTY_AMOUNT = 2;

    // --- Needs Decay & Gain ---
    // Energy
    /** Base rate at which energy decays per tick. */
    public static final int ENERGY_BASE_DECAY_RATE = 2;
    /** Amount of energy restored per tick when a Sim is sleeping. */
    public static final int ENERGY_SLEEP_ADDED_AMOUNT = 10;
    /** Extra energy cost incurred when a Sim is working. */
    public static final int ENERGY_WORK_DECAY_AMOUNT = 2;

    // Happiness
    /** Base rate at which happiness decays per tick. */
    public static final int HAPPINESS_BASE_DECAY_RATE = 2;

    // Hunger
    /** Base rate at which hunger decays per tick. */
    public static final int HUNGER_BASE_DECAY_RATE = 2;
    /** Higher decay rate for hunger under certain conditions. */
    public static final int HUNGER_ACCELERATED_DECAY_RATE = 3;
    /** Lower decay rate for hunger (unused or state-dependent). */
    public static final int HUNGER_DECELERATED_DECAY_RATE = 3;

    // Hygiene
    /** Base rate at which hygiene decays per tick. */
    public static final int HYGIENE_BASE_DECAY_RATE = 2;
    /** Higher decay rate for hygiene (e.g., after working or exercising). */
    public static final int HYGIENE_ACCELERATED_DECAY_RATE = 4;

    // Social
    /** Base rate at which the social need decays per tick. */
    public static final int SOCIAL_BASE_DECAY_RATE = 5;
    /** Amount of social need restored by performing social actions. */
    public static final int SOCIAL_ADDED_AMOUNT = 15;

    // Health
    /** Base amount of health lost when critical needs (hunger/energy) are empty. */
    public static final int HEALTH_BASE_DAMAGE = 5;
    /** Multiplier for health damage when multiple needs are critical. */
    public static final int HEALTH_ACCELERATED_DAMAGE_MULTIPLIER = 2;
    /** Additional static health damage per tick under toxic conditions. */
    public static final int HEALTH_ADDED_DAMAGE = 2;
    /** Rate at which health is restored when all needs are satisfied. */
    public static final int HEALTH_REGENERATION_RATE = 1;
}
