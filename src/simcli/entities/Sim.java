package simcli.entities;

import simcli.engine.SimulationException;
import simcli.entities.lifecycle.LifeStage;
import simcli.entities.lifecycle.AdultStage;
import simcli.entities.lifecycle.ChildStage;

import simcli.needs.Need;
import simcli.needs.Hunger;
import simcli.needs.Energy;
import simcli.needs.Hygiene;
import simcli.needs.Happiness;
import simcli.world.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sim implements ISimBehaviour {

    protected String name;
    protected int age;
    protected Gender gender;
    protected int money;
    protected Need hunger;
    protected Need energy;
    protected Need hygiene;
    protected Need happiness;
    protected int inventoryCapacity;
    protected SimState state;
    protected List<Item> inventory;
    protected int starvingTicks;
    protected Room currentRoom;
    protected int daysAlive;
    protected ActionState currentAction;

    private LifeStage currentStage;

    // Career tracking
    private Job career;
    private int jobTier;
    private int consecutiveDaysMissed;
    private int shiftsWorkedToday;
    private boolean hasWarnedAboutOverwork;

    // Social Mechanics
    private Map<Sim, Integer> relationships;
    private Sim spouse;

    // World Stats trackers
    protected int totalMoneyEarned;
    protected int totalItemsBought;

    public Sim(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.money = 500;
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.hygiene = new Hygiene();
        this.happiness = new Happiness();
        this.inventoryCapacity = 10;
        this.state = SimState.HEALTHY;
        this.inventory = new ArrayList<>();
        this.starvingTicks = 0;
        this.daysAlive = 0;
        this.currentAction = ActionState.IDLE;

        this.career = Job.UNEMPLOYED;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        this.hasWarnedAboutOverwork = false;

        this.relationships = new HashMap<>();
        this.spouse = null;

        this.totalMoneyEarned = money;
        this.totalItemsBought = 0;

        if (this.age < 18) {
            this.setLifeStage(new ChildStage());
        } else {
            this.setLifeStage(new AdultStage());
        }
    }

    public Sim(String name, int age, Gender gender, Job career) {
        this(name, age, gender);
        this.career = career;
    }

    public String getName() {
        return name;
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

    public Need getHunger() {
        return hunger;
    }

    public Need getEnergy() {
        return energy;
    }

    public SimState getState() {
        return state;
    }

    public Need getHygiene() {
        return hygiene;
    }

    public Need getHappiness() {
        return happiness;
    }

    public int getInventoryCapacity() {
        return inventoryCapacity;
    }

    public void setInventoryCapacity(int capacity) {
        this.inventoryCapacity = capacity;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public Job getCareer() {
        return career;
    }

    public int getJobTier() {
        return jobTier;
    }

    public ActionState getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(ActionState action) {
        this.currentAction = action;
    }

    public int getShiftsWorkedToday() {
        return shiftsWorkedToday;
    }

    public boolean hasWarnedAboutOverwork() {
        return hasWarnedAboutOverwork;
    }

    public void setWarnedAboutOverwork(boolean warned) {
        this.hasWarnedAboutOverwork = warned;
    }

    public Map<Sim, Integer> getRelationships() {
        return relationships;
    }

    public Sim getSpouse() {
        return spouse;
    }

    public void interactSocially(Sim otherSim) {
        if (otherSim == this)
            return;
        relationships.putIfAbsent(otherSim, 0);
        int currentRelationship = relationships.get(otherSim);
        relationships.put(otherSim, currentRelationship + 10);
        this.currentAction = ActionState.SOCIALIZING;
        simcli.ui.UIManager.printMessage(this.name + " socializes with " + otherSim.getName() + ".");
        this.energy.decrease(10);
        this.happiness.increase(10);
    }

    public boolean marry(Sim otherSim) {
        if (otherSim == this)
            return false;
        relationships.putIfAbsent(otherSim, 0);
        if (relationships.get(otherSim) >= 50 && this.spouse == null && otherSim.getSpouse() == null) {
            this.spouse = otherSim;
            otherSim.spouse = this;
            simcli.ui.UIManager.printMessage(
                    "\n*** WEDDING BELLS! " + this.name + " and " + otherSim.getName() + " are now married! ***");
            return true;
        }
        return false;
    }

    public Sim reproduce() throws SimulationException {
        if (this.spouse == null) {
            throw new SimulationException(this.name + " is not married and cannot reproduce.");
        }
        if (this.gender == this.spouse.getGender()) {
            throw new SimulationException(this.name + " and " + this.spouse.getName()
                    + " are of the same gender and cannot reproduce biologically.");
        }
        // Success
        simcli.ui.UIManager.printMessage(
                "\n*** NEW LIFE! " + this.name + " and " + this.spouse.getName() + " have had a baby! ***");
        Sim childObj = new Sim("Baby", 0, Math.random() < 0.5 ? Gender.MALE : Gender.FEMALE);
        // The GameEngine / WorldManager would normally add this child to the household.
        // For now we return it.
        return childObj;
    }

    @Override
    public void performActivity(String activityType) throws SimulationException {
        if (this.state == SimState.DEAD || this.state == SimState.CRITICAL) {
            throw new SimulationException(this.name + " is in critical condition and refuses to act.");
        }

        if (activityType.equalsIgnoreCase("Work")) {
            if (!canWork()) {
                simcli.ui.UIManager.printMessage(
                        this.name + " is in the " + this.getCurrentStageName() + " stage and cannot work!");
                return;
            }
            if (this.career == Job.UNEMPLOYED) {
                simcli.ui.UIManager.printMessage(this.name + " is unemployed and cannot work!");
                return;
            }
            this.currentAction = ActionState.WORKING;
            simcli.ui.UIManager.displayActionAnimation(this);

            int dailyPay = this.career.getSalaryAtTier(this.jobTier);
            simcli.ui.UIManager.printMessage(
                    this.name + " works a shift as a " + this.career.getTitle() + " and earns $" + dailyPay + "!");

            int multiplier = 1 + this.shiftsWorkedToday;
            if (multiplier > 1) {
                simcli.ui.UIManager.printMessage(this.name + " feels the heavy strain of overworking!");
            }
            this.energy.decrease(this.career.getEnergyDrain() * multiplier);
            this.hunger.decrease(20 * multiplier);
            this.hygiene.decrease(30 * multiplier);

            this.setMoney(this.getMoney() + dailyPay);
            this.addTotalMoneyEarned(dailyPay);
            this.consecutiveDaysMissed = 0;
            this.shiftsWorkedToday++;
        } else if (activityType.equalsIgnoreCase("Study")) {
            this.currentAction = ActionState.STUDYING;
            simcli.ui.UIManager.displayActionAnimation(this);
            simcli.ui.UIManager.printMessage(this.name + " sits down to study.");
            this.energy.decrease(15);
            this.hunger.decrease(10);
            this.happiness.increase(5);
        } else if (activityType.equalsIgnoreCase("Play")) {
            this.currentAction = ActionState.PLAYING;
            simcli.ui.UIManager.displayActionAnimation(this);
            simcli.ui.UIManager.printMessage(this.name + " goes out to play!");
            this.energy.decrease(25);
            this.hunger.decrease(20);
            this.hygiene.decrease(30);
            this.happiness.increase(20);
        } else {
            this.currentAction = ActionState.IDLE;
            simcli.ui.UIManager.printMessage(this.name + " is idling.");
        }
    }

    public void checkTruancy() {
        if (this.career == Job.UNEMPLOYED)
            return;
        this.consecutiveDaysMissed++;
        if (this.consecutiveDaysMissed > 3) {
            simcli.ui.UIManager.printWarning("Oh no! " + this.name + " missed too many days of work and was fired from "
                    + this.career.getTitle() + ".");
            this.career = Job.UNEMPLOYED;
            this.jobTier = 1;
            this.consecutiveDaysMissed = 0;
            this.shiftsWorkedToday = 0;
        } else if (this.consecutiveDaysMissed > 0) {
            simcli.ui.UIManager
                    .printWarning(this.name + " missed work! Consecutive days missed: " + this.consecutiveDaysMissed);
        }
    }

    public void promote() {
        if (this.career == Job.UNEMPLOYED)
            return;
        if (this.jobTier < this.career.getMaxTier()) {
            this.jobTier++;
            simcli.ui.UIManager.printMessage("\n*** PROMOTION! " + this.name + " has been promoted to tier "
                    + this.jobTier + " in " + this.career.getTitle() + "! ***");
        }
    }

    public void changeJob(Job newJob) {
        this.career = newJob;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        simcli.ui.UIManager.printMessage(this.name + " has started a new job as a " + newJob.getTitle() + ".");
    }

    public void tick() {
        if (this.state == SimState.DEAD)
            return;

        // Reset action state to IDLE at start of each tick, or maybe the logic depends
        // on other components
        // For right now, ActionState is transient, but wait, if it's rendered during
        // the turn, IDLE happens after.

        double ageMultiplier = 1.0 + (Math.max(0, this.age - 18) * 0.05);
        double stageEnergyModifier = (this.currentStage != null) ? this.currentStage.getEnergyDecayModifier() : 1.0;

        this.hunger.decay(ageMultiplier);
        this.energy.decay(ageMultiplier * stageEnergyModifier);
        this.hygiene.decay(ageMultiplier);
        this.happiness.decay(ageMultiplier);
        this.updateState();

        simcli.ui.UIManager.printMessage("[" + this.name + "] Hunger: " + this.hunger.getValue() +
                " | Energy: " + this.energy.getValue() +
                " | Hygiene: " + this.hygiene.getValue() +
                " | Happiness: " + this.happiness.getValue() +
                " | Cash: $" + this.money + " | Status: " + this.state);
    }

    public void updateState() {
        if (this.hunger.getValue() <= 0) {
            this.state = SimState.STARVING;
            this.starvingTicks++;
            if (this.starvingTicks > 3)
                this.state = SimState.DEAD;
        } else {
            this.starvingTicks = 0;
            if (this.hunger.getValue() <= 20)
                this.state = SimState.HUNGRY;
            else if (this.energy.getValue() <= 20)
                this.state = SimState.TIRED;
            else
                this.state = SimState.HEALTHY;
        }
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void addItem(Item item) {
        this.inventory.add(item);
    }

    public int getStarvingTicks() {
        return starvingTicks;
    }

    public void setStarvingTicks(int ticks) {
        this.starvingTicks = ticks;
    }

    public void growOlderDaily() {
        this.daysAlive++;
        this.shiftsWorkedToday = 0;
        this.hasWarnedAboutOverwork = false;

        if (this.daysAlive % 3 == 0) {
            this.ageUp(); // Delegate the actual birthday and brain swap logic to ageUp()
            if (this.age >= 81) {
                this.state = SimState.DEAD;
                simcli.ui.UIManager.printMessage(
                        "\n*** TRAGEDY! " + this.name + " has passed away of old age at " + this.age + ". ***");
            } else if (this.age >= 65 && this.career != Job.UNEMPLOYED) {
                simcli.ui.UIManager.printMessage("\n*** RETIREMENT! " + this.name
                        + " has reached the retirement age of 65 and is officially retired. ***");
                this.changeJob(Job.UNEMPLOYED);
                this.setMoney(this.getMoney() + 1000);
            }
        }
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

    public void ageUp() {
        this.age++;
        if (this.currentStage == null) {
            simcli.ui.UIManager
                    .printMessage("[" + this.name + "] Birthday! Age: " + this.age + " (no life stage assigned yet).");
            return;
        }

        LifeStage nextStage = this.currentStage.getNextStage(this.age);
        if (nextStage != this.currentStage) {
            simcli.ui.UIManager.printMessage(
                    "\n*** LIFE STAGE TRANSITION ***"
                            + "\n    Sim   : " + this.name
                            + "\n    Age   : " + this.age
                            + "\n    From  : " + this.currentStage.getStageName()
                            + "\n    To    : " + nextStage.getStageName()
                            + "\n*****************************");
            this.currentStage = nextStage;
        } else {
            simcli.ui.UIManager
                    .printMessage("\n*** BIRTHDAY! " + this.name + " has aged up to " + this.age + " years old! ***");
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
}