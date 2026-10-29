package simcli.entities.actors;

import simcli.entities.items.Consumable;
import simcli.entities.models.*;

import simcli.needs.SimsNeedsTracker;
import simcli.entities.managers.CareerManager;
import simcli.entities.managers.InventoryManager;
import simcli.entities.managers.MoodletManager;
import simcli.entities.managers.RelationshipManager;
import simcli.entities.managers.SkillManager;
import simcli.entities.managers.WantsManager;
import simcli.entities.items.Item;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import simcli.engine.SimulationLogger;
import simcli.entities.lifecycle.AdultStage;
import simcli.entities.lifecycle.ChildStage;
import simcli.entities.lifecycle.ElderStage;
import simcli.entities.lifecycle.LifeStage;
import simcli.entities.lifecycle.TeenStage;
import simcli.needs.Need;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Room;

/**
 * The core actor in the simulation — a virtual person with needs, a career,
 * an inventory, relationships, and a lifecycle governed by the State Pattern.
 *
 * <p>Each Sim tracks hunger, energy, hygiene, happiness, and social needs
 * via a {@link SimsNeedsTracker}. Career progression is managed by
 * {@link CareerManager}, and lifecycle transitions (Child → Teen → Adult →
 * Elder) are handled through the {@link LifeStage} interface.</p>
 *
 * @see simcli.engine.LifecycleManager
 * @see SimsNeedsTracker
 */
public class Sim implements ISimBehaviour {

    private String name;
    private int age;
    private Gender gender;
    private int money;
    private ActionState currentAction;
    private Room currentRoom;
    private int daysAlive;
    private LifeStage currentStage;
    private boolean isChildSim;

    // Component Managers
    private SimsNeedsTracker needsTracker;
    private CareerManager careerManager;
    private InventoryManager inventoryManager;
    private SkillManager skillManager;
    private List<Trait> traits;

    private RelationshipManager relationshipManager;

    // Inner life: feelings, daily wants and fears, and memories
    private final MoodletManager moodletManager = new MoodletManager();
    private final WantsManager wantsManager = new WantsManager();
    private final List<Memory> memories = new ArrayList<>();

    // Life goal
    private Aspiration aspiration;
    private boolean aspirationFulfilled;
    private final Set<Aspiration> pastAspirations = EnumSet.noneOf(Aspiration.class);

    // Family and household history
    private int generation = 1;
    private int mealsCooked;
    private int pregnancyDueDay = -1;
    private String expectedBabyName;
    private Gender expectedBabyGender;
    private int lastBirthDay = -1;

    // World Stats trackers
    private int totalMoneyEarned;
    private int totalItemsBought;

    /**
     * Constructs a new Sim with starting money, a random trait, and
     * an appropriate life stage based on age.
     *
     * @param name   the display name of this Sim.
     * @param age    the starting age (determines initial life stage).
     * @param gender the gender of this Sim.
     */
    public Sim(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.money = GameConstants.STARTING_MONEY;
        this.daysAlive = 0;
        this.currentAction = ActionState.IDLE;
        this.isChildSim = false;

        this.needsTracker = new SimsNeedsTracker();
        this.careerManager = new CareerManager();
        this.inventoryManager = new InventoryManager(GameConstants.STARTING_INVENTORY_CAPACITY);
        this.skillManager = new SkillManager();
        this.traits = new ArrayList<>();
        Trait[] allTraits = Trait.values();
        this.traits.add(allTraits[GameRandom.RANDOM.nextInt(allTraits.length)]);

        this.relationshipManager = new RelationshipManager(this);
        Aspiration[] allAspirations = Aspiration.values();
        this.aspiration = allAspirations[GameRandom.RANDOM.nextInt(allAspirations.length)];
        this.aspirationFulfilled = false;
        this.totalMoneyEarned = money;
        this.totalItemsBought = 0;

        if (this.age < GameConstants.TEEN_AGE) {
            this.setLifeStage(new ChildStage());
        } else if (this.age < GameConstants.ADULT_AGE) {
            this.setLifeStage(new TeenStage());
        } else if (this.age < GameConstants.ELDER_AGE) {
            this.setLifeStage(new AdultStage());
        } else {
            this.setLifeStage(new ElderStage());
        }
    }

    /**
     * Constructs a new Sim with a pre-assigned career.
     *
     * @param name   the display name of this Sim.
     * @param age    the starting age.
     * @param gender the gender of this Sim.
     * @param career the starting career (e.g. {@link Job#UNEMPLOYED}).
     */
    public Sim(String name, int age, Gender gender, Job career) {
        this(name, age, gender);
        this.careerManager.setCareer(career);
    }

    // -------------------------------------------------------------------------
    // Property Getters / Setters
    // -------------------------------------------------------------------------

    /**
     * Returns the display name of this Sim.
     *
     * @return the Sim's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this Sim.
     *
     * @param name the new display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the current age of this Sim in years.
     *
     * @return the Sim's age.
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns the gender of this Sim.
     *
     * @return the Sim's {@link Gender}.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Returns the current money balance of this Sim.
     *
     * @return the Sim's money balance.
     */
    public int getMoney() {
        return money;
    }

    /**
     * Sets the money balance of this Sim.
     *
     * @param money the new money balance.
     */
    public void setMoney(int money) {
        this.money = money;
    }

    /**
     * Returns the list of personality traits assigned to this Sim.
     *
     * @return the Sim's trait list.
     */
    public List<Trait> getTraits() {
        return traits;
    }

    /**
     * Checks whether the Sim possesses the given trait.
     *
     * @param t the trait to check for.
     * @return {@code true} if the Sim has the trait.
     */
    public boolean hasTrait(Trait t) {
        return traits.contains(t);
    }

    /**
     * Returns this Sim's life aspiration (randomly chosen at creation unless
     * the player picks one).
     *
     * @return the Sim's {@link Aspiration}.
     */
    public Aspiration getAspiration() {
        return aspiration;
    }

    /**
     * Sets this Sim's life aspiration and marks it as not yet fulfilled.
     *
     * @param aspiration the new aspiration.
     */
    public void setAspiration(Aspiration aspiration) {
        this.aspiration = aspiration;
        this.aspirationFulfilled = false;
    }

    /**
     * Returns whether this Sim has fulfilled their aspiration.
     *
     * @return {@code true} once fulfilled.
     */
    public boolean isAspirationFulfilled() {
        return aspirationFulfilled;
    }

    /**
     * Marks this Sim's aspiration as fulfilled (or not, when loading a save).
     *
     * @param fulfilled whether the aspiration is fulfilled.
     */
    public void setAspirationFulfilled(boolean fulfilled) {
        this.aspirationFulfilled = fulfilled;
    }

    /**
     * Returns the aspirations this Sim fulfilled before their current one.
     *
     * @return earlier fulfilled aspirations (mutable, for save/load).
     */
    public Set<Aspiration> getPastAspirations() {
        return pastAspirations;
    }

    /**
     * Moves on to a new aspiration after fulfilling the current one,
     * remembering the old one.
     *
     * @param next the new aspiration (one not fulfilled before).
     * @throws IllegalStateException if the current aspiration isn't fulfilled yet.
     */
    public void chooseNextAspiration(Aspiration next) {
        if (!aspirationFulfilled) {
            throw new IllegalStateException(name + " hasn't fulfilled their current aspiration yet");
        }
        pastAspirations.add(aspiration);
        setAspiration(next);
    }

    /**
     * Returns how many aspirations this Sim has fulfilled in their life.
     *
     * @return the number of fulfilled aspirations, including the current one if done.
     */
    public int getAspirationsFulfilled() {
        return pastAspirations.size() + (aspirationFulfilled ? 1 : 0);
    }

    // -------------------------------------------------------------------------
    // Mood
    // -------------------------------------------------------------------------

    /**
     * Returns this Sim's overall mood: the average of their five needs,
     * plus or minus whatever their {@link Moodlet}s add.
     *
     * @return the mood, 0–100.
     */
    public int getMood() {
        int needs = (getHunger().getValue() + getEnergy().getValue() + getHygiene().getValue()
                + getHappiness().getValue() + getSocial().getValue()) / 5;
        return Math.max(0, Math.min(100, needs + moodletManager.getTotal()));
    }

    /**
     * Returns the feelings this Sim currently carries.
     *
     * @return the Sim's {@link MoodletManager}.
     */
    public MoodletManager getMoodlets() {
        return moodletManager;
    }

    /**
     * Returns this Sim's wants and fear for today, satisfaction points and perks.
     *
     * @return the Sim's {@link WantsManager}.
     */
    public WantsManager getWants() {
        return wantsManager;
    }

    /**
     * Returns whether this Sim owns a reward-store perk.
     *
     * @param perk the perk.
     * @return {@code true} if owned.
     */
    public boolean hasPerk(Reward perk) {
        return wantsManager.hasPerk(perk);
    }

    /**
     * Gives this Sim a perk without charging for it, switching on any
     * lasting effect. Used by {@link #redeem(Reward)} and when loading a save.
     *
     * @param perk the perk.
     */
    public void grantPerk(Reward perk) {
        wantsManager.addPerk(perk);
        if (perk == Reward.SUNNY_DISPOSITION) {
            moodletManager.setSunny(true);
        }
    }

    /**
     * Spends satisfaction points on a reward: a perk is kept for life, a
     * boost takes effect at once.
     *
     * @param reward the reward.
     * @return {@code true} if bought; {@code false} if the Sim can't afford
     *         it or already owns the perk.
     */
    public boolean redeem(Reward reward) {
        if (wantsManager.getPoints() < reward.getCost() || (reward.isPerk() && hasPerk(reward))) {
            return false;
        }
        wantsManager.losePoints(reward.getCost());
        switch (reward) {
            case INSPIRATION:
                moodletManager.add(Moodlet.INSPIRED);
                break;
            case SECOND_WIND:
                getEnergy().increase(GameConstants.SECOND_WIND_ENERGY);
                break;
            default:
                grantPerk(reward);
                break;
        }
        return true;
    }

    /**
     * Returns everything this Sim remembers, oldest first.
     *
     * @return the Sim's memories (mutable, for save/load).
     */
    public List<Memory> getMemories() {
        return memories;
    }

    /**
     * Adds a memory.
     *
     * @param day  the day it happened.
     * @param text what happened.
     */
    public void remember(int day, String text) {
        memories.add(new Memory(day, text));
    }

    /**
     * Returns whether the Sim is in a great mood, which makes them learn
     * faster and earn more.
     *
     * @return {@code true} if the mood is at least {@link GameConstants#GREAT_MOOD_THRESHOLD}.
     */
    public boolean isInGreatMood() {
        return getState() != SimState.DEAD && getMood() >= GameConstants.GREAT_MOOD_THRESHOLD;
    }

    // -------------------------------------------------------------------------
    // Family and household history
    // -------------------------------------------------------------------------

    /**
     * Returns which generation of the household this Sim belongs to:
     * 1 for the founders, and one more than their parents for children.
     *
     * @return the generation.
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Sets this Sim's generation.
     *
     * @param generation the generation (1 for founders).
     */
    public void setGeneration(int generation) {
        this.generation = Math.max(1, generation);
    }

    /** @return how many meals this Sim has cooked. */
    public int getMealsCooked() {
        return mealsCooked;
    }

    /** Records that this Sim cooked a meal. */
    public void recordMealCooked() {
        mealsCooked++;
    }

    /**
     * Sets the meals-cooked counter. Used for save/load restoration.
     *
     * @param mealsCooked the number of meals cooked.
     */
    public void setMealsCooked(int mealsCooked) {
        this.mealsCooked = mealsCooked;
    }

    /** @return {@code true} if this Sim is expecting a baby. */
    public boolean isPregnant() {
        return pregnancyDueDay >= 0;
    }

    /** @return the day the baby is due, or -1 if not expecting. */
    public int getPregnancyDueDay() {
        return pregnancyDueDay;
    }

    /** @return the name chosen for the expected baby, or {@code null}. */
    public String getExpectedBabyName() {
        return expectedBabyName;
    }

    /** @return the expected baby's gender, or {@code null}. */
    public Gender getExpectedBabyGender() {
        return expectedBabyGender;
    }

    /**
     * Starts a pregnancy.
     *
     * @param dueDay   the day the baby will be born.
     * @param babyName the name chosen for the baby.
     * @param gender   the baby's gender.
     */
    public void startPregnancy(int dueDay, String babyName, Gender gender) {
        this.pregnancyDueDay = dueDay;
        this.expectedBabyName = babyName;
        this.expectedBabyGender = gender;
    }

    /**
     * Ends the pregnancy, recording the day of the birth.
     *
     * @param day the day the baby was born.
     */
    public void endPregnancy(int day) {
        this.pregnancyDueDay = -1;
        this.expectedBabyName = null;
        this.expectedBabyGender = null;
        this.lastBirthDay = day;
    }

    /** @return the day this Sim last had a baby, or -1 if never. */
    public int getLastBirthDay() {
        return lastBirthDay;
    }

    /**
     * Sets the day this Sim last had a baby. Used for save/load restoration.
     *
     * @param day the day, or -1 for never.
     */
    public void setLastBirthDay(int day) {
        this.lastBirthDay = day;
    }

    // -------------------------------------------------------------------------
    // Delegate Getters for Needs (Exterior Compatibility)
    // -------------------------------------------------------------------------

    /**
     * Returns the Sim's hunger need.
     *
     * @return the {@link Need} object tracking hunger.
     */
    public Need getHunger() {
        return needsTracker.getHunger();
    }

    /**
     * Returns the Sim's energy need.
     *
     * @return the {@link Need} object tracking energy.
     */
    public Need getEnergy() {
        return needsTracker.getEnergy();
    }

    /**
     * Returns the Sim's hygiene need.
     *
     * @return the {@link Need} object tracking hygiene.
     */
    public Need getHygiene() {
        return needsTracker.getHygiene();
    }

    /**
     * Returns the Sim's happiness need.
     *
     * @return the {@link Need} object tracking happiness.
     */
    public Need getHappiness() {
        return needsTracker.getHappiness();
    }

    /**
     * Returns the Sim's social need.
     *
     * @return the {@link Need} object tracking social.
     */
    public Need getSocial() {
        return needsTracker.getSocial();
    }

    /**
     * Returns the Sim's current state (HEALTHY, HUNGRY, TIRED, or DEAD).
     *
     * @return the current {@link SimState}.
     */
    public SimState getState() {
        return needsTracker.getState();
    }

    /**
     * Re-evaluates the Sim's state based on current need values
     * and updates it accordingly.
     */
    public void updateState() {
        needsTracker.updateState();
    }

    /**
     * Returns the Sim's current health points (0–100).
     *
     * @return the health value.
     */
    public int getHealth() {
        return needsTracker.getHealth();
    }

    /**
     * Returns the number of consecutive ticks this Sim has been starving.
     *
     * @return the starving tick count.
     */
    public int getStarvingTicks() {
        return needsTracker.getStarvingTicks();
    }

    /**
     * Sets the starving tick counter. Used for save/load restoration.
     *
     * @param starvingTicks the number of consecutive starving ticks.
     */
    public void setStarvingTicks(int starvingTicks) {
        needsTracker.setStarvingTicks(starvingTicks);
    }

    /**
     * Sets the Sim's health points directly. Used for save/load restoration.
     *
     * @param health the health value (0–100).
     */
    public void setHealth(int health) {
        needsTracker.setHealth(health);
    }

    // -------------------------------------------------------------------------
    // Career Delegates
    // -------------------------------------------------------------------------

    /**
     * Returns the Sim's current career.
     *
     * @return the current {@link Job}.
     */
    public Job getCareer() {
        return careerManager.getCareer();
    }

    /**
     * Returns the Sim's current promotion tier within their career.
     *
     * @return the job tier (1-indexed).
     */
    public int getJobTier() {
        return careerManager.getJobTier();
    }

    /**
     * Sets the Sim's promotion tier. Used for save/load restoration.
     *
     * @param tier the job tier to set.
     */
    public void setJobTier(int tier) {
        careerManager.setJobTier(tier);
    }

    /**
     * Returns the number of work shifts completed today.
     *
     * @return shifts worked today.
     */
    public int getShiftsWorkedToday() {
        return careerManager.getShiftsWorkedToday();
    }

    /**
     * Increments the shift counter for today by one.
     */
    public void incrementShiftsWorkedToday() {
        careerManager.incrementShiftsWorkedToday();
    }

    /**
     * Resets the consecutive-days-missed counter to zero.
     * Called when the Sim successfully reports to work.
     */
    public void resetConsecutiveDaysMissed() {
        careerManager.setConsecutiveDaysMissed(0);
    }

    /**
     * Returns whether the overwork warning has already been shown
     * for this shift cycle.
     *
     * @return {@code true} if the Sim has been warned about overworking.
     */
    public boolean hasWarnedAboutOverwork() {
        return careerManager.hasWarnedAboutOverwork();
    }

    /**
     * Sets the overwork warning flag.
     *
     * @param warned {@code true} to mark the warning as shown.
     */
    public void setWarnedAboutOverwork(boolean warned) {
        careerManager.setWarnedAboutOverwork(warned);
    }

    // -------------------------------------------------------------------------
    // Inventory Delegates
    // -------------------------------------------------------------------------

    /**
     * Returns the maximum number of items this Sim can carry.
     *
     * @return the inventory capacity.
     */
    public int getInventoryCapacity() {
        return inventoryManager.getCapacity();
    }

    /**
     * Sets the inventory capacity. Used when upgrading rooms or restoring saves.
     *
     * @param capacity the new inventory capacity.
     */
    public void setInventoryCapacity(int capacity) {
        inventoryManager.setCapacity(capacity);
    }

    /**
     * Returns the list of items currently held by this Sim.
     *
     * @return the Sim's inventory as a mutable list.
     */
    public List<Item> getInventory() {
        return inventoryManager.getItems();
    }

    /**
     * Adds an item to this Sim's inventory.
     *
     * @param item the item to add.
     */
    public void addItem(Item item) {
        inventoryManager.addItem(item);
    }

    // -------------------------------------------------------------------------
    // Room / Action / Manager Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the room this Sim is currently in, or {@code null} if not
     * inside a residential building.
     *
     * @return the current {@link Room}, or {@code null}.
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Places this Sim in the specified room.
     *
     * @param room the room to enter.
     */
    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    /**
     * Returns the Sim's current action state (e.g. IDLE, WORKING, SLEEPING).
     *
     * @return the current {@link ActionState}.
     */
    public ActionState getCurrentAction() {
        return currentAction;
    }

    /**
     * Sets the Sim's current action state.
     *
     * @param action the new action state.
     */
    public void setCurrentAction(ActionState action) {
        this.currentAction = action;
    }

    /**
     * Returns the relationship manager for this Sim, which handles
     * all social bonds, marriages, and children.
     *
     * @return the Sim's {@link RelationshipManager}.
     */
    public RelationshipManager getRelationshipManager() {
        return relationshipManager;
    }

    /**
     * Returns the career manager for this Sim, which handles
     * job state, promotions, and truancy tracking.
     *
     * @return the Sim's {@link CareerManager}.
     */
    public CareerManager getCareerManager() {
        return careerManager;
    }

    // -------------------------------------------------------------------------
    // Skill Delegates
    // -------------------------------------------------------------------------

    /**
     * Returns the skill manager for this Sim, which tracks experience
     * in every {@link SkillType}.
     *
     * @return the Sim's {@link SkillManager}.
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }

    /**
     * Returns this Sim's level in a skill.
     *
     * @param skill the skill to look up, or {@code null}.
     * @return the skill level, or 0 if {@code skill} is {@code null}.
     */
    public int getSkillLevel(SkillType skill) {
        return skill == null ? 0 : skillManager.getLevel(skill);
    }

    /**
     * Trains a skill. Active Sims learn Fitness 50% faster and Socialites
     * learn Charisma 50% faster, and any Sim in a great mood
     * ({@link #isInGreatMood()}) learns 50% faster still, and the
     * {@link Reward#QUICK_LEARNER} perk adds another 25%.
     *
     * @param skill  the skill to train, or {@code null} to do nothing.
     * @param points the base experience points earned.
     * @return {@code true} if the skill went up a level.
     */
    public boolean trainSkill(SkillType skill, int points) {
        if (skill == null) {
            return false;
        }
        if ((skill == SkillType.FITNESS && hasTrait(Trait.ACTIVE))
                || (skill == SkillType.CHARISMA && hasTrait(Trait.SOCIALITE))) {
            points = (int) (points * GameConstants.BONUS_TIMES);
        }
        if (isInGreatMood()) {
            points = (int) (points * GameConstants.GREAT_MOOD_SKILL_MULTIPLIER);
        }
        if (hasPerk(Reward.QUICK_LEARNER)) {
            points = (int) (points * GameConstants.PERK_BONUS);
        }
        return skillManager.addPractice(skill, points, name);
    }

    /**
     * Returns the skill level this Sim still needs before their next
     * promotion is possible.
     *
     * @return the required level of the career's skill, or 0 if the Sim
     *         already meets it, is unemployed or is at the top tier.
     */
    public int getSkillNeededForPromotion() {
        Job job = getCareer();
        if (job == Job.UNEMPLOYED || getJobTier() >= job.getMaxTier()) {
            return 0;
        }
        int required = job.getSkillRequiredForTier(getJobTier() + 1);
        return getSkillLevel(job.getPrimarySkill()) >= required ? 0 : required;
    }

    // -------------------------------------------------------------------------
    // Need Modification Shortcuts
    // -------------------------------------------------------------------------

    /**
     * Decreases this Sim's energy by the specified amount.
     *
     * @param amount the amount of energy to drain.
     */
    public void decreaseEnergy(int amount) {
        needsTracker.decreaseEnergy(amount);
    }

    /**
     * Increases this Sim's happiness by the specified amount.
     *
     * @param amount the amount of happiness to add.
     */
    public void increaseHappiness(int amount) {
        this.needsTracker.increaseHappiness(amount);
    }

    /**
     * Increases this Sim's social need by the specified amount.
     *
     * @param amount the amount of social to add.
     */
    public void increaseSocial(int amount) {
        this.needsTracker.increaseSocial(amount);
    }

    // -------------------------------------------------------------------------
    // Tick / Lifecycle Logic
    // -------------------------------------------------------------------------

    /**
     * Advances one game tick: decays all needs, applies cross-penalties,
     * drains health if starving, and updates the SimState.
     *
     * <p>Dead Sims are skipped. Non-playable child Sims only have hunger
     * decay applied.</p>
     */
    public void tick() {
        if (this.getState() == SimState.DEAD) {
            return;
        }
        moodletManager.tick();

        double ageMultiplier = getAgeDecayMultiplier();
        double stageEnergyModifier = (this.currentStage != null) ? this.currentStage.getEnergyDecayModifier() : 1.0;
        double traitEnergyMod = 1.0;
        for (Trait t : traits) {
            traitEnergyMod *= t.getEnergyDecayModifier();
        }

        if (this.isChildSim && !this.isPlayable()) {
            // Babies/Infants only have hunger decay — but they can still starve
            needsTracker.tickInfant(this, ageMultiplier, this.name);
        } else {
            needsTracker.tick(this, ageMultiplier, stageEnergyModifier * traitEnergyMod, this.name);
        }
    }

    /**
     * Returns how much faster this Sim's needs decay because of their age.
     * Needs decay at the normal rate until {@link GameConstants#ELDER_AGE},
     * then a little faster each year, up to
     * {@link GameConstants#MAX_AGE_DECAY_MULTIPLIER}.
     *
     * @return the age decay multiplier (1.0 for children, teens and adults).
     */
    public double getAgeDecayMultiplier() {
        double extra = Math.max(0, this.age - GameConstants.ELDER_AGE) * GameConstants.ELDER_DECAY_PER_YEAR;
        return Math.min(GameConstants.MAX_AGE_DECAY_MULTIPLIER, 1.0 + extra);
    }

    /**
     * @deprecated Use {@link simcli.engine.LifecycleManager#processDay(Sim)}
     *             instead. Retained only for backward compatibility.
     */
    @Deprecated
    public void growOlderDaily() {
        // Lifecycle logic is now centralized in LifecycleManager.
        // This method is kept for interface compliance (ISimBehaviour).
        // If called directly, it only increments the day counter and resets
        // career — the aging/death/retirement rules are in LifecycleManager.
        this.daysAlive++;
        careerManager.resetDaily(true);
    }

    /**
     * Returns the total number of in-game days this Sim has been alive.
     *
     * @return days-alive count.
     */
    public int getDaysAlive() {
        return this.daysAlive;
    }

    /**
     * Sets the total number of in-game days this Sim has been alive.
     * Used exclusively for saving/loading the exact lifecycle state.
     *
     * @param daysAlive days-alive count.
     */
    public void setDaysAlive(int daysAlive) {
        this.daysAlive = daysAlive;
    }

    /**
     * Increments the Sim's days-alive counter by one.
     * Called by {@link simcli.engine.LifecycleManager} on each day boundary.
     */
    public void incrementDaysAlive() {
        this.daysAlive++;
    }

    /**
     * Marks this Sim as dead (state = DEAD).
     * Called by {@link simcli.engine.LifecycleManager} when the Sim reaches
     * the death age threshold.
     */
    public void markAsDead() {
        this.needsTracker.setState(SimState.DEAD);
    }

    /**
     * Sets this Sim's current life stage (State Pattern).
     *
     * @param stage the new {@link LifeStage} to transition to.
     */
    protected void setLifeStage(LifeStage stage) {
        this.currentStage = stage;
    }

    /**
     * Returns this Sim's current life stage.
     *
     * @return the current {@link LifeStage}, or {@code null} if uninitialized.
     */
    public LifeStage getLifeStage() {
        return this.currentStage;
    }

    /**
     * Checks whether this Sim's life stage permits working.
     *
     * @return {@code true} if the Sim's current stage allows employment.
     */
    public boolean canWork() {
        return (this.currentStage != null) && this.currentStage.canWork();
    }

    /**
     * Returns the human-readable name of the Sim's current life stage.
     *
     * @return the stage name (e.g. "Child", "Adult"), or "Unknown" if unset.
     */
    public String getCurrentStageName() {
        return (this.currentStage != null) ? this.currentStage.getStageName() : "Unknown";
    }

    /**
     * Returns whether this Sim was born as a child within the simulation
     * (as opposed to being created at game start).
     *
     * @return {@code true} if this is a child Sim.
     */
    public boolean isChildSim() {
        return isChildSim;
    }

    /**
     * Marks this Sim as a child born in-game.
     *
     * @param childSim {@code true} to mark as a child Sim.
     */
    public void setChildSim(boolean childSim) {
        this.isChildSim = childSim;
    }

    /**
     * A child sim becomes playable once they reach TeenStage (age &gt;= 13).
     * Non-child sims are always playable.
     *
     * @return {@code true} if this Sim can be controlled by the player.
     */
    public boolean isPlayable() {
        if (!isChildSim)
            return true;
        return this.age >= GameConstants.TEEN_AGE;
    }

    /**
     * Increments this Sim's age by one year and triggers a life-stage
     * transition if the current stage's threshold is reached (State Pattern).
     */
    public void ageUp() {
        ageUp(true);
    }

    /**
     * Increments this Sim's age by one year and triggers a life-stage
     * transition if needed, optionally without announcing it (used for
     * townsfolk, whose birthdays the player doesn't need to hear about).
     *
     * @param announce {@code true} to log the birthday or stage change.
     */
    public void ageUp(boolean announce) {
        this.age++;
        if (!announce) {
            if (this.currentStage != null) {
                this.currentStage = this.currentStage.getNextStage(this.age);
            }
            return;
        }
        if (this.currentStage == null) {
            SimulationLogger.getInstance()
                    .log("[" + this.name + "] Birthday! Age: " + this.age + " (no life stage assigned yet).");
            return;
        }
        LifeStage nextStage = this.currentStage.getNextStage(this.age);
        if (nextStage != this.currentStage) {
            SimulationLogger.getInstance().log(
                    "\n*** LIFE STAGE TRANSITION ***"
                            + "\n    Sim   : " + this.name
                            + "\n    Age   : " + this.age
                            + "\n    From  : " + this.currentStage.getStageName()
                            + "\n    To    : " + nextStage.getStageName()
                            + "\n*****************************");
            this.currentStage = nextStage;
        } else {
            SimulationLogger.getInstance()
                    .log("\n*** BIRTHDAY! " + this.name + " has aged up to " + this.age + " years old! ***");
        }
    }

    // -------------------------------------------------------------------------
    // Statistics
    // -------------------------------------------------------------------------

    /**
     * Returns the total money earned by this Sim across all shifts.
     *
     * @return cumulative earnings.
     */
    public int getTotalMoneyEarned() {
        return totalMoneyEarned;
    }

    /**
     * Adds to the cumulative money-earned tracker.
     *
     * @param amount the amount of money earned.
     */
    public void addTotalMoneyEarned(int amount) {
        this.totalMoneyEarned += amount;
    }

    /**
     * Sets the cumulative money-earned tracker. Used for newborns and save/load restoration.
     *
     * @param amount the cumulative earnings.
     */
    public void setTotalMoneyEarned(int amount) {
        this.totalMoneyEarned = amount;
    }

    /**
     * Sets the cumulative items-bought tracker. Used for save/load restoration.
     *
     * @param amount the cumulative item purchase count.
     */
    public void setTotalItemsBought(int amount) {
        this.totalItemsBought = amount;
    }

    /**
     * Returns the total number of items bought by this Sim.
     *
     * @return cumulative item purchase count.
     */
    public int getTotalItemsBought() {
        return totalItemsBought;
    }

    /**
     * Adds to the cumulative items-bought tracker.
     *
     * @param amount the number of items bought.
     */
    public void addTotalItemsBought(int amount) {
        this.totalItemsBought += amount;
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    /**
     * Performs a work shift: drains energy/hunger/hygiene, earns salary,
     * trains the career's skill, and builds work performance.
     *
     * <p>Pay rises with tier and the career's skill
     * ({@link Job#getShiftPay(int, int)}), plus
     * {@link GameConstants#GREAT_MOOD_PAY_BONUS} for turning up in a great
     * mood. Promotions are earned, not rolled: each shift adds
     * {@link Job#getPerformanceGain(int, int)} performance, more for a
     * happy, skilled Sim, and a full meter
     * ({@link GameConstants#PROMOTION_PERFORMANCE}) earns a promotion once
     * the skill reaches {@link Job#getSkillRequiredForTier(int)}.</p>
     *
     * @return a {@link WorkResult} indicating earnings, promotion, and overwork status.
     */
    public WorkResult performWork() {
        if (!canWork() || getCareer() == Job.UNEMPLOYED) {
            return WorkResult.failure("Cannot work");
        }
        // mood counts as the Sim walks in, before the shift wears them out
        int moodAtStart = getMood();
        boolean greatMood = isInGreatMood();

        int multiplier = 1 + getShiftsWorkedToday();
        getEnergy().decrease(getCareer().getEnergyDrain() * multiplier);
        getHunger().decrease(20 * multiplier);
        getHygiene().decrease(30 * multiplier);

        SkillType skill = getCareer().getPrimarySkill();
        int skillLevel = getSkillLevel(skill);
        int earnings = getCareer().getShiftPay(getJobTier(), skillLevel);
        if (greatMood) {
            earnings = (int) Math.round(earnings * (1 + GameConstants.GREAT_MOOD_PAY_BONUS));
        }
        money += earnings;
        addTotalMoneyEarned(earnings);
        incrementShiftsWorkedToday();
        resetConsecutiveDaysMissed();

        boolean promoted = false;
        if (getJobTier() < getCareer().getMaxTier()) {
            CareerManager career = getCareerManager();
            int gain = getCareer().getPerformanceGain(moodAtStart, skillLevel);
            if (hasPerk(Reward.GO_GETTER)) {
                gain = (int) Math.round(gain * GameConstants.PERK_BONUS);
            }
            career.addPerformance(gain);
            if (career.getPerformance() >= GameConstants.PROMOTION_PERFORMANCE && getSkillNeededForPromotion() == 0) {
                career.promote(getName());
                promoted = true;
            }
        }
        trainSkill(skill, GameConstants.WORK_SKILL_XP);

        return WorkResult.success(earnings, promoted, multiplier > 1);
    }

    /**
     * Consumes a food item, restoring hunger, energy, and happiness,
     * then removes it from the inventory.
     *
     * @param item the consumable to eat.
     */
    public void eat(Consumable item) {
        this.getHunger().increase(item.getSatiationValue());
        this.getEnergy().increase(item.getEnergyValue());
        this.getHappiness().increase(item.getHappinessValue());
        this.getInventory().remove(item);
    }

    /**
     * Puts this Sim to sleep, restoring energy and reducing hunger
     * proportional to the number of ticks until morning.
     *
     * @param ticksToMorning the number of ticks until 08:00.
     */
    public void sleep(int ticksToMorning) {
        int energyGain = Math.min(100, 15 * ticksToMorning);
        int hungerLoss = 3 * ticksToMorning;
        this.getEnergy().increase(energyGain);
        this.getHunger().decrease(hungerLoss);
        this.setCurrentAction(ActionState.SLEEPING);
    }
}