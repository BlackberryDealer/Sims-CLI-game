package simcli.entities.actors;

import simcli.entities.items.Consumable;
import simcli.entities.models.*;

import simcli.needs.SimsNeedsTracker;
import simcli.entities.managers.CareerManager;
import simcli.entities.managers.InventoryManager;
import simcli.entities.managers.RelationshipManager;
import simcli.entities.managers.SkillManager;
import simcli.entities.items.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import simcli.engine.SimulationException;
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

    // --- Property Getters ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public List<Trait> getTraits() {
        return traits;
    }

    public boolean hasTrait(Trait t) {
        return traits.contains(t);
    }

    // --- Delegate Getters for Exterior Compatibility ---
    public Need getHunger() {
        return needsTracker.getHunger();
    }

    public Need getEnergy() {
        return needsTracker.getEnergy();
    }

    public Need getHygiene() {
        return needsTracker.getHygiene();
    }

    public Need getHappiness() {
        return needsTracker.getHappiness();
    }

    public Need getSocial() {
        return needsTracker.getSocial();
    }

    public SimState getState() {
        return needsTracker.getState();
    }

    public void updateState() {
        needsTracker.updateState();
    }

    public int getHealth() {
        return needsTracker.getHealth();
    }

    public int getStarvingTicks() {
        return needsTracker.getStarvingTicks();
    }

    public void setHealth(int health) {
        needsTracker.setHealth(health);
    }

    public Job getCareer() {
        return careerManager.getCareer();
    }

    public int getJobTier() {
        return careerManager.getJobTier();
    }

    public void setJobTier(int tier) {
        careerManager.setJobTier(tier);
    }

    public int getShiftsWorkedToday() {
        return careerManager.getShiftsWorkedToday();
    }

    public void incrementShiftsWorkedToday() {
        careerManager.incrementShiftsWorkedToday();
    }

    public void resetConsecutiveDaysMissed() {
        careerManager.setConsecutiveDaysMissed(0);
    }

    public boolean hasWarnedAboutOverwork() {
        return careerManager.hasWarnedAboutOverwork();
    }

    public void setWarnedAboutOverwork(boolean warned) {
        careerManager.setWarnedAboutOverwork(warned);
    }

    public int getInventoryCapacity() {
        return inventoryManager.getCapacity();
    }

    public void setInventoryCapacity(int capacity) {
        inventoryManager.setCapacity(capacity);
    }

    public List<Item> getInventory() {
        return inventoryManager.getItems();
    }

    public void addItem(Item item) {
        inventoryManager.addItem(item);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public ActionState getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(ActionState action) {
        this.currentAction = action;
    }

    public RelationshipManager getRelationshipManager() {
        return relationshipManager;
    }

    public CareerManager getCareerManager() {
        return careerManager;
    }

    public void decreaseEnergy(int amount) {
        needsTracker.decreaseEnergy(amount);
    }

    public void increaseHappiness(int amount) {
        this.needsTracker.increaseHappiness(amount);
    }

    public void increaseSocial(int amount) {
        this.needsTracker.increaseSocial(amount);
    }

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

        double ageMultiplier = 1.0
                + (Math.max(0, this.age - GameConstants.ADULT_AGE) * GameConstants.AGE_ENERGY_PENALTY_MULTIPLIER);
        double stageEnergyModifier = (this.currentStage != null) ? this.currentStage.getEnergyDecayModifier() : 1.0;
        double traitEnergyMod = 1.0;
        for (Trait t : traits) {
            traitEnergyMod *= t.getEnergyDecayModifier();
        }

        if (this.isChildSim && !this.isPlayable()) {
            // Babys/Infants only have hunger decay
            this.getHunger().calculateDecay(this, ageMultiplier);
            this.updateState();
        } else {
            needsTracker.tick(this, ageMultiplier, stageEnergyModifier * traitEnergyMod, this.name);
        }
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

    protected void setLifeStage(LifeStage stage) {
        this.currentStage = stage;
    }

    public LifeStage getLifeStage() {
        return this.currentStage;
    }

    public boolean canWork() {
        return (this.currentStage != null) && this.currentStage.canWork();
    }

    public String getCurrentStageName() {
        return (this.currentStage != null) ? this.currentStage.getStageName() : "Unknown";
    }

    public boolean isChildSim() {
        return isChildSim;
    }

    public void setChildSim(boolean childSim) {
        this.isChildSim = childSim;
    }

    /**
     * A child sim becomes playable once they reach TeenStage (age >= 13).
     * Non-child sims are always playable.
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
        this.age++;
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

    public int getTotalMoneyEarned() {
        return totalMoneyEarned;
    }

    public void addTotalMoneyEarned(int amount) {
        this.totalMoneyEarned += amount;
    }

    public int getTotalItemsBought() {
        return totalItemsBought;
    }

    public void addTotalItemsBought(int amount) {
        this.totalItemsBought += amount;
    }

    /**
     * Performs a work shift: drains energy/hunger/hygiene, earns salary,
     * and has a 25% chance of promotion.
     *
     * @return a {@link WorkResult} indicating earnings, promotion, and overwork status.
     */
    public WorkResult performWork() {
        if (!canWork() || getCareer() == Job.UNEMPLOYED) {
            return WorkResult.failure("Cannot work");
        }
        int multiplier = 1 + getShiftsWorkedToday();
        getEnergy().decrease(getCareer().getEnergyDrain() * multiplier);
        getHunger().decrease(20 * multiplier);
        getHygiene().decrease(30 * multiplier);

        int earnings = getCareer().getSalaryAtTier(getJobTier());
        money += earnings;
        addTotalMoneyEarned(earnings);
        incrementShiftsWorkedToday();
        resetConsecutiveDaysMissed();

        boolean promoted = false;
        if (getJobTier() < getCareer().getMaxTier()) {
            if (GameRandom.RANDOM.nextDouble() < 0.25) {
                getCareerManager().promote(getName());
                promoted = true;
            }
        }

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