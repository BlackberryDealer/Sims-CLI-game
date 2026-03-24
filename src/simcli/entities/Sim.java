package simcli.entities;

import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.lifecycle.LifeStage;
import simcli.entities.lifecycle.AdultStage;
import simcli.entities.lifecycle.TeenStage;
import simcli.entities.lifecycle.ElderStage;
import simcli.entities.lifecycle.ChildStage;
import simcli.needs.Need;
import simcli.world.Room;
import simcli.utils.GameConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Sim implements ISimBehaviour {

    private String name;
    private int age;
    private Gender gender;
    private int money;
    private ActionState currentAction;
    private Room currentRoom;
    private int daysAlive;
    private LifeStage currentStage;

    // Component Managers
    private NeedsTracker needsTracker;
    private CareerProfile careerProfile;
    private InventoryManager inventoryManager;
    private SkillManager skillManager;
    private List<Trait> traits;

    // Social Mechanics
    private Map<Sim, Integer> relationships;
    private Sim spouse;

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

        this.needsTracker = new NeedsTracker();
        this.careerProfile = new CareerProfile();
        this.inventoryManager = new InventoryManager(10);
        this.skillManager = new SkillManager();
        this.traits = new ArrayList<>();
        Trait[] allTraits = Trait.values();
        this.traits.add(allTraits[simcli.utils.GameRandom.RANDOM.nextInt(allTraits.length)]);

        this.relationships = new HashMap<>();
        this.spouse = null;
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
        this.careerProfile.setCareer(career);
    }

    // --- Property Getters ---
    public String getName() { return name; }
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
    public Need getFun() { return needsTracker.getFun(); }
    public Need getSocial() { return needsTracker.getSocial(); }
    public SimState getState() { return needsTracker.getState(); }
    public void updateState() { needsTracker.updateState(); }
    public int getHealth() { return needsTracker.getHealth(); }
    public int getStarvingTicks() { return needsTracker.getStarvingTicks(); }
    public void setHealth(int health) { needsTracker.setHealth(health); }
    
    public Job getCareer() { return careerProfile.getCareer(); }
    public int getJobTier() { return careerProfile.getJobTier(); }
    public void setJobTier(int tier) { careerProfile.setJobTier(tier); }
    public int getShiftsWorkedToday() { return careerProfile.getShiftsWorkedToday(); }
    public void incrementShiftsWorkedToday() { careerProfile.incrementShiftsWorkedToday(); }
    public void resetConsecutiveDaysMissed() { careerProfile.setConsecutiveDaysMissed(0); }
    public boolean hasWarnedAboutOverwork() { return careerProfile.hasWarnedAboutOverwork(); }
    public void setWarnedAboutOverwork(boolean warned) { careerProfile.setWarnedAboutOverwork(warned); }

    public int getInventoryCapacity() { return inventoryManager.getCapacity(); }
    public void setInventoryCapacity(int capacity) { inventoryManager.setCapacity(capacity); }
    public List<Item> getInventory() { return inventoryManager.getItems(); }
    public void addItem(Item item) { inventoryManager.addItem(item); }

    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }
    public ActionState getCurrentAction() { return currentAction; }
    public void setCurrentAction(ActionState action) { this.currentAction = action; }

    public Map<Sim, Integer> getRelationships() { return relationships; }
    public Sim getSpouse() { return spouse; }

    public int getRelationship(Sim otherSim) {
        return relationships.getOrDefault(otherSim, 0);
    }

    public void increaseRelationship(Sim otherSim, int amount) {
        relationships.put(otherSim, getRelationship(otherSim) + amount);
    }

    // --- Social Logic ---
    public void interactSocially(Sim otherSim) {
        if (otherSim == this) return;
        relationships.putIfAbsent(otherSim, 0);
        int relBonus = GameConstants.relBonus;
        int socialBonus = GameConstants.socialBonus;
        int funBonus = GameConstants.funBonus;
        if (this.hasTrait(Trait.SOCIALITE)) {
            relBonus = (int)(relBonus * GameConstants.bonusTimes);   // 50% more relationship
            socialBonus = (int)(socialBonus * GameConstants.bonusTimes); // 50% more social recovery
            funBonus = (int)(funBonus * GameConstants.bonusTimes);
        }
        relationships.put(otherSim, relationships.get(otherSim) + relBonus);
        this.currentAction = ActionState.SOCIALIZING;
        
        SimulationLogger.log(this.name + " socializes with " + otherSim.getName() + ".");
        this.getSocial().increase(socialBonus);
        this.getEnergy().decrease(10);
        this.getFun().increase(funBonus);
    }

    public boolean marry(Sim otherSim) {
        if (otherSim == this) return false;
        relationships.putIfAbsent(otherSim, 0);
        if (relationships.get(otherSim) >= 50 && this.spouse == null && otherSim.getSpouse() == null) {
            this.spouse = otherSim;
            otherSim.spouse = this;
            SimulationLogger.log("\n*** WEDDING BELLS! " + this.name + " and " + otherSim.getName() + " are now married! ***");
            return true;
        }
        return false;
    }

    public Sim reproduce() throws SimulationException {
        if (this.spouse == null) {
            throw new SimulationException(this.name + " is not married and cannot reproduce.");
        }
        if (this.gender == this.spouse.getGender()) {
            throw new SimulationException(this.name + " and " + this.spouse.getName() + " are of the same gender and cannot reproduce biologically.");
        }
        SimulationLogger.log("\n*** NEW LIFE! " + this.name + " and " + this.spouse.getName() + " have had a baby! ***");
        return new Sim("Baby", 0, simcli.utils.GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE);
    }

    // --- Activity Logic ---
    public void checkTruancy() { careerProfile.checkTruancy(this.name); }
    public void promote() { careerProfile.promote(this.name); }
    public void changeJob(Job newJob) { careerProfile.changeJob(newJob, this.name); }

    public void tick() {
        double ageMultiplier = 1.0 + (Math.max(0, this.age - 18) * 0.05);
        double stageEnergyModifier = (this.currentStage != null) ? this.currentStage.getEnergyDecayModifier() : 1.0;
        double traitEnergyMod = 1.0;
        for(Trait t : traits) { traitEnergyMod *= t.getEnergyDecayModifier(); }
        needsTracker.tick(ageMultiplier, stageEnergyModifier * traitEnergyMod, this.name, this.money);
    }

    public void growOlderDaily() {
        this.daysAlive++;
        careerProfile.resetDaily(true);

        if (this.daysAlive % 3 == 0) {
            this.ageUp();
            if (this.age >= GameConstants.DEATH_AGE) {
                needsTracker.setState(SimState.DEAD);
                SimulationLogger.log("\n*** TRAGEDY! " + this.name + " has passed away of old age at " + this.age + ". ***");
            } else if (this.age >= GameConstants.ELDER_AGE && this.getCareer() != Job.UNEMPLOYED) {
                SimulationLogger.log("\n*** RETIREMENT! " + this.name + " has reached the retirement age of 65 and is officially retired. ***");
                this.changeJob(Job.UNEMPLOYED);
                this.setMoney(this.getMoney() + 1000);
            }
        }
    }

    protected void setLifeStage(LifeStage stage) { this.currentStage = stage; }
    public LifeStage getLifeStage() { return this.currentStage; }
    public boolean canWork() { return (this.currentStage != null) && this.currentStage.canWork(); }
    public String getCurrentStageName() { return (this.currentStage != null) ? this.currentStage.getStageName() : "Unknown"; }

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
}