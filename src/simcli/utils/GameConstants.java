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

    /** Base percentage chance (0-100) that a joke lands. */
    public static final int JOKE_BASE_SUCCESS_CHANCE = 60;
    /** Base percentage chance (0-100) that a flirt lands with an acquaintance. */
    public static final int FLIRT_BASE_SUCCESS_CHANCE = 40;
    /** Relationship score needed before a Sim can flirt with someone. */
    public static final int FLIRT_MIN_RELATIONSHIP = 20;
    /** Relationship lost when a joke or flirt falls flat. */
    public static final int SOCIAL_FAIL_RELATIONSHIP_PENALTY = 5;
    /** Happiness lost when a joke or flirt falls flat. */
    public static final int SOCIAL_FAIL_HAPPINESS_PENALTY = 5;
    /** Relationship gained from any gift, before its value is added. */
    public static final int GIFT_BASE_RELATIONSHIP = 5;
    /** Every this many Simoleons a gift is worth adds 1 relationship point. */
    public static final int GIFT_PRICE_PER_RELATIONSHIP_POINT = 10;
    /** Most relationship a single gift can add. */
    public static final int GIFT_MAX_RELATIONSHIP = 25;
    /** Happiness the receiver gains from a gift. */
    public static final int GIFT_HAPPINESS = 15;

    // --- Skills ---
    /** Experience points needed for each skill level. */
    public static final int SKILL_XP_PER_LEVEL = 20;
    /** The highest level a skill can reach. */
    public static final int MAX_SKILL_LEVEL = 10;
    /** Fitness experience from one workout. */
    public static final int WORKOUT_FITNESS_XP = 10;
    /** Logic experience from one study session. */
    public static final int STUDY_LOGIC_XP = 10;
    /** Charisma experience from a successful social action. */
    public static final int SOCIAL_CHARISMA_XP = 4;
    /** Experience in the job's skill from one work shift. */
    public static final int WORK_SKILL_XP = 3;
    /** Extra pay per level of the job's skill, as a fraction of the base pay. */
    public static final double SKILL_PAY_BONUS_PER_LEVEL = 0.05;
    /** Chance of promotion per shift with no skill. */
    public static final double BASE_PROMOTION_CHANCE = 0.25;
    /** Extra promotion chance per level of the job's skill. */
    public static final double PROMOTION_CHANCE_PER_SKILL_LEVEL = 0.05;
    /** Highest promotion chance per shift. */
    public static final double MAX_PROMOTION_CHANCE = 0.75;
    /** Skill levels needed per tier above 2 (tier 3 needs 2, tier 4 needs 4, ...). */
    public static final int SKILL_LEVELS_PER_TIER = 2;
    /** Extra percentage points of social success chance per charisma level. */
    public static final int CHARISMA_SUCCESS_BONUS_PER_LEVEL = 4;
    /** Extra relationship gain per charisma level, as a fraction. */
    public static final double CHARISMA_RELATIONSHIP_BONUS_PER_LEVEL = 0.05;
    /** Energy saved on a workout per fitness level. */
    public static final int FITNESS_WORKOUT_ENERGY_SAVING = 1;

    // --- Autonomy ---
    /** Idle Sims at or below this hunger eat from the fridge. */
    public static final int AUTONOMY_HUNGER_THRESHOLD = 35;
    /** Idle Sims at or below this energy go to bed. */
    public static final int AUTONOMY_SLEEP_THRESHOLD = 25;
    /** Autonomously sleeping Sims wake up once their energy reaches this. */
    public static final int AUTONOMY_WAKE_THRESHOLD = 90;
    /** Idle Sims at or below this hygiene take a shower. */
    public static final int AUTONOMY_HYGIENE_THRESHOLD = 20;
    /** Babies at or below this hunger get fed by an idle Sim. */
    public static final int AUTONOMY_BABY_HUNGER_THRESHOLD = 40;
    /** Hunger restored when a baby is fed. */
    public static final int BABY_FEED_AMOUNT = 50;

    // --- Food & Bills ---
    /** Days shop food stays fresh in a fridge (it goes off twice as fast elsewhere). */
    public static final int FOOD_SHELF_LIFE_DAYS = 5;
    /** Days raw ingredients stay fresh in a fridge. */
    public static final int INGREDIENT_SHELF_LIFE_DAYS = 6;
    /** Days a cooked meal stays fresh in a fridge. */
    public static final int MEAL_SHELF_LIFE_DAYS = 3;
    /** Freshness lost per day by food stored in a fridge. */
    public static final int FRIDGE_SPOIL_RATE = 1;
    /** Freshness lost per day by food kept anywhere else. */
    public static final int UNREFRIGERATED_SPOIL_RATE = 2;
    /** Happiness the cook gains from making a meal. */
    public static final int COOKING_HAPPINESS = 10;
    /** Happiness every household Sim loses when the bills can't be paid. */
    public static final int UNPAID_BILLS_HAPPINESS_PENALTY = 20;

    // --- Children ---
    /** Days per year of age for children (under {@link #TEEN_AGE}). */
    public static final int CHILD_DAYS_PER_AGE_TICK = 1;
    /** Logic experience a teen gains from a day at school. */
    public static final int SCHOOL_LOGIC_XP = 8;

    // --- Park ---
    /** Visitors who leave the park each morning, to be replaced. */
    public static final int DAILY_VISITORS_LEAVING = 3;
    /** Percentage chance (0-100) that a replacement visitor is someone the town already knows. */
    public static final int RETURNING_VISITOR_CHANCE = 50;
    /** Townsfolk move away once they reach this age. */
    public static final int NPC_MOVE_AWAY_AGE = 80;
    /** Extra success chance (percentage points) when joking or flirting with a Socialite. */
    public static final int SOCIALITE_TARGET_BONUS = 15;
    /** Lost success chance (percentage points) when joking or flirting with a Lazy Sim. */
    public static final int LAZY_TARGET_PENALTY = 10;

    // --- Household Events ---
    /** Percentage chance (0-100) per turn of a household-wide event. */
    public static final int HOUSEHOLD_EVENT_CHANCE = 4;

    // --- Goals ---
    /** Money awarded for fulfilling a life aspiration. */
    public static final int ASPIRATION_REWARD_MONEY = 1000;
    /** Children needed for the Family aspiration. */
    public static final int FAMILY_ASPIRATION_CHILDREN = 3;
    /** Friends (relationship 50+) needed for the Social Butterfly aspiration. */
    public static final int SOCIAL_ASPIRATION_FRIENDS = 5;
    /** Relationship score that counts as a friend for goals. */
    public static final int FRIEND_THRESHOLD = 50;
    /** Household savings that count as a fortune. */
    public static final int FORTUNE_AMOUNT = 10000;

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
