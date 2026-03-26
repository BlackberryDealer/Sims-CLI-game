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
        this.traits.add(allTraits[simcli.utils.GameRandom.RANDOM.nextInt(allTraits.length)]);

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

    public Sim(String name, int age, Gender gender, Job career) {
        this(name, age, gender);
        this.careerManager.setCareer(career);
    }

    // --- Property Getters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public Gender getGender() { return gender; }
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public SkillManager getSkillManager() { return skillManager; }
    public List<Trait> getTraits() { return traits; }
    public boolean hasTrait(Trait t) { return traits.contains(t); }
    
    // --- Delegate Getters for Exterior Compatibility ---
    public Need getHunger() { return needsTracker.getHunger(); }
    public Need getEnergy() { return needsTracker.getEnergy(); }
    public Need getHygiene() { return needsTracker.getHygiene(); }
    public Need getHappiness() { return needsTracker.getHappiness(); }
    public Need getSocial() { return needsTracker.getSocial(); }
    public SimState getState() { return needsTracker.getState(); }
    public void updateState() { needsTracker.updateState(); }
    public int getHealth() { return needsTracker.getHealth(); }
    public int getStarvingTicks() { return needsTracker.getStarvingTicks(); }
    public void setHealth(int health) { needsTracker.setHealth(health); }
    
    public Job getCareer() { return careerManager.getCareer(); }
    public int getJobTier() { return careerManager.getJobTier(); }
    public void setJobTier(int tier) { careerManager.setJobTier(tier); }
    public int getShiftsWorkedToday() { return careerManager.getShiftsWorkedToday(); }
    public void incrementShiftsWorkedToday() { careerManager.incrementShiftsWorkedToday(); }
    public void resetConsecutiveDaysMissed() { careerManager.setConsecutiveDaysMissed(0); }
    public boolean hasWarnedAboutOverwork() { return careerManager.hasWarnedAboutOverwork(); }
    public void setWarnedAboutOverwork(boolean warned) { careerManager.setWarnedAboutOverwork(warned); }

    public int getInventoryCapacity() { return inventoryManager.getCapacity(); }
    public void setInventoryCapacity(int capacity) { inventoryManager.setCapacity(capacity); }
    public List<Item> getInventory() { return inventoryManager.getItems(); }
    public void addItem(Item item) { inventoryManager.addItem(item); }

    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }
    public ActionState getCurrentAction() { return currentAction; }
    public void setCurrentAction(ActionState action) { this.currentAction = action; }

    public RelationshipManager getRelationshipManager() { return relationshipManager; }
    public CareerManager getCareerManager() { return careerManager; }

    public void decreaseEnergy(int amount) { needsTracker.decreaseEnergy(amount); }
    public void increaseHappiness(int amount) { this.needsTracker.increaseHappiness(amount); }
    public void increaseSocial(int amount) { this.needsTracker.increaseSocial(amount); }

    public void tick() {
        if (this.getState() == SimState.DEAD) {
            return; 
        }

        double ageMultiplier = 1.0 + (Math.max(0, this.age - GameConstants.ADULT_AGE) * GameConstants.AGE_ENERGY_PENALTY_MULTIPLIER);
        double stageEnergyModifier = (this.currentStage != null) ? this.currentStage.getEnergyDecayModifier() : 1.0;
        double traitEnergyMod = 1.0;
        for(Trait t : traits) { 
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

    public void growOlderDaily() {
        this.daysAlive++;
        careerManager.resetDaily(true);

        if (this.daysAlive % GameConstants.DAYS_PER_AGE_TICK == 0) {
            this.ageUp();
            
            if (this.age >= GameConstants.DEATH_AGE) {
                this.needsTracker.setState(SimState.DEAD); 
                SimulationLogger.log("\n*** " + this.name + " has passed away of old age. ***");
            } 
            
            if (this.age >= GameConstants.ELDER_AGE && this.getCareer() == Job.UNEMPLOYED) {
                int pensionIncome = GameConstants.RETIREMENT_PENSION_INCOME_AMOUNT;
                this.money += pensionIncome;
                SimulationLogger.log(this.name + " collected a retirement pension of $" + pensionIncome);
            }

            Job currentJob = this.getCareer();
            if (currentJob != Job.UNEMPLOYED && this.age > currentJob.getMaxAge()) {
                SimulationLogger.log("\n[RETIREMENT] " + this.name + " is too old for " + currentJob.getTitle() + " and must retire.");
                this.getCareerManager().changeJob(Job.UNEMPLOYED, this.name);
            }
        }
    }

    protected void setLifeStage(LifeStage stage) { this.currentStage = stage; }
    public LifeStage getLifeStage() { return this.currentStage; }
    public boolean canWork() { return (this.currentStage != null) && this.currentStage.canWork(); }
    public String getCurrentStageName() { return (this.currentStage != null) ? this.currentStage.getStageName() : "Unknown"; }

    public boolean isChildSim() { return isChildSim; }
    public void setChildSim(boolean childSim) { this.isChildSim = childSim; }
    
    /**
     * A child sim becomes playable once they reach TeenStage (age >= 13).
     * Non-child sims are always playable.
     */
    public boolean isPlayable() {
        if (!isChildSim) return true;
        return this.age >= GameConstants.TEEN_AGE;
    }

    public void ageUp() {
        this.age++;
        if (this.currentStage == null) {
            SimulationLogger.log("[" + this.name + "] Birthday! Age: " + this.age + " (no life stage assigned yet).");
            return;
        }
        LifeStage nextStage = this.currentStage.getNextStage(this.age);
        if (nextStage != this.currentStage) {
            SimulationLogger.log(
                    "\n*** LIFE STAGE TRANSITION ***"
                            + "\n    Sim   : " + this.name
                            + "\n    Age   : " + this.age
                            + "\n    From  : " + this.currentStage.getStageName()
                            + "\n    To    : " + nextStage.getStageName()
                            + "\n*****************************");
            this.currentStage = nextStage;
        } else {
            SimulationLogger.log("\n*** BIRTHDAY! " + this.name + " has aged up to " + this.age + " years old! ***");
        }
    }

    public int getTotalMoneyEarned() { return totalMoneyEarned; }
    public void addTotalMoneyEarned(int amount) { this.totalMoneyEarned += amount; }
    public int getTotalItemsBought() { return totalItemsBought; }
    public void addTotalItemsBought(int amount) { this.totalItemsBought += amount; }

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

    public void eat(Consumable item) {
        this.getHunger().increase(item.getSatiationValue());
        this.getEnergy().increase(item.getEnergyValue());
        this.getHappiness().increase(item.getHappinessValue());
        this.getInventory().remove(item);
    }

    public void sleep(int ticksToMorning) {
        int energyGain = Math.min(100, 15 * ticksToMorning);
        int hungerLoss = 3 * ticksToMorning;
        this.getEnergy().increase(energyGain);
        this.getHunger().decrease(hungerLoss);
        this.setCurrentAction(ActionState.SLEEPING);
    }
}