package simcli.entities;

import simcli.engine.SimulationException;
import simcli.entities.lifecycle.LifeStage; // State Pattern: the stage "brain"

import simcli.needs.Need;
import simcli.needs.Hunger;
import simcli.needs.Energy;
import simcli.needs.Hygiene;
import simcli.needs.Happiness;
import simcli.world.Room;

import java.util.ArrayList;
import java.util.List;

public abstract class Sim implements ISimBehaviour {

    protected String name;
    protected int age;
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

    /**
     * State Pattern — the Sim's current lifecycle "brain".
     * Private to enforce encapsulation; subclasses set it via
     * {@link #setLifeStage}.
     * When a birthday triggers a transition, only this reference is swapped;
     * the Sim object itself remains unchanged in memory.
     */
    private LifeStage currentStage;

    // World Stats trackers
    protected int totalMoneyEarned;
    protected int totalItemsBought;

    public Sim(String name, int age) {
        this.name = name;
        this.age = age;
        this.money = 500; // Starting Simoleons
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.hygiene = new Hygiene();
        this.happiness = new Happiness();
        this.inventoryCapacity = 10;
        this.state = SimState.HEALTHY;
        this.inventory = new ArrayList<>();
        this.starvingTicks = 0;
        this.daysAlive = 0;

        this.totalMoneyEarned = money; // initial seed counts
        this.totalItemsBought = 0;
    }

    // getters for .txt saving
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
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

    public abstract void performActivity(String activityType) throws SimulationException;

    public void tick() {
        if (this.state == SimState.DEAD)
            return;
        // Base age multiplier: 5 % faster stat decay per year over 18.
        double ageMultiplier = 1.0 + (Math.max(0, this.age - 18) * 0.05);

        // State Pattern: energy decay is additionally scaled by the current life stage.
        // A child's energyDecayModifier = 1.5x; an adult's = 1.0x (no extra penalty).
        double stageEnergyModifier = (this.currentStage != null)
                ? this.currentStage.getEnergyDecayModifier()
                : 1.0;

        this.hunger.decay(ageMultiplier);
        this.energy.decay(ageMultiplier * stageEnergyModifier); // stage modifier applied only to energy
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
        if (this.daysAlive % 3 == 0) {
            this.age++;
            simcli.ui.UIManager
                    .printMessage("\n*** BIRTHDAY! " + this.name + " has aged up to " + this.age + " years old! ***");
            if (this.age >= 81) {
                this.state = SimState.DEAD;
                simcli.ui.UIManager
                        .printMessage("\n*** TRAGEDY! " + this.name + " has passed away of old age at 81. ***");
            }
        }
    }

    // =========================================================================
    // State Pattern — lifecycle methods added to existing Sim base class
    // =========================================================================

    /**
     * Protected setter used by subclasses ({@code ChildSim}, {@code AdultSim})
     * to initialise — and by {@link #ageUp()} to swap — the lifecycle stage.
     * Keeping this {@code protected} prevents external code from arbitrarily
     * swapping stages while still allowing the inheritance hierarchy to set
     * the initial stage.
     *
     * @param stage the new {@link LifeStage}; must not be {@code null}.
     */
    protected void setLifeStage(LifeStage stage) {
        this.currentStage = stage;
    }

    /**
     * Returns the current lifecycle stage object (read-only access for callers
     * that need to query stage details without being able to replace it).
     *
     * @return the active {@link LifeStage}, or {@code null} if not yet set.
     */
    public LifeStage getLifeStage() {
        return this.currentStage;
    }

    /**
     * Convenience delegate: asks the current stage whether this Sim may work.
     * Returns {@code false} safely if no stage has been set yet.
     *
     * @return {@code true} if the stage permits working, {@code false} otherwise.
     */
    public boolean canWork() {
        return (this.currentStage != null) && this.currentStage.canWork();
    }

    /**
     * Returns the display name of the active life stage (e.g. "Child", "Adult").
     * Returns {@code "Unknown"} if no stage has been assigned yet.
     *
     * @return the stage name string.
     */
    public String getCurrentStageName() {
        return (this.currentStage != null) ? this.currentStage.getStageName() : "Unknown";
    }

    /**
     * <strong>State Pattern core method.</strong>
     *
     * <p>
     * Increments the Sim's age by one year and performs a polymorphic stage
     * transition check:
     * </p>
     * <ol>
     * <li>Age is incremented.</li>
     * <li>{@code currentStage.getNextStage(age)} is called — the current stage
     * decides its own successor (Open/Closed Principle).</li>
     * <li>If the returned object is <em>different</em> from the current stage
     * (identity check with {@code !=}), the reference is replaced — the
     * "brain swap". The old stage object becomes unreachable and the JVM
     * garbage-collects it. <strong>The Sim object itself never
     * changes.</strong></li>
     * </ol>
     *
     * <p>
     * Called directly by {@link simcli.engine.LifecycleManager#processTick}.
     * Distinct from {@link #growOlderDaily()}, which handles day-based aging
     * used by the existing game loop.
     * </p>
     */
    public void ageUp() {
        // Step 1: increment age.
        this.age++;

        if (this.currentStage == null) {
            simcli.ui.UIManager.printMessage("[" + this.name + "] Birthday! Age: " + this.age
                    + " (no life stage assigned yet).");
            return;
        }

        // Step 2: ask the stage if a transition is needed.
        LifeStage nextStage = this.currentStage.getNextStage(this.age);

        // Step 3: if a NEW stage object was returned, execute the "brain swap".
        if (nextStage != this.currentStage) {
            simcli.ui.UIManager.printMessage(
                    "\n*** LIFE STAGE TRANSITION ***"
                            + "\n    Sim   : " + this.name
                            + "\n    Age   : " + this.age
                            + "\n    From  : " + this.currentStage.getStageName()
                            + "\n    To    : " + nextStage.getStageName()
                            + "\n    [The old " + this.currentStage.getStageName()
                            + "Stage is now eligible for garbage collection]"
                            + "\n*****************************");
            // The "brain swap" — one line replaces the stage inside the Sim.
            // The Sim object in memory is identical before and after this line.
            this.currentStage = nextStage;
        } else {
            simcli.ui.UIManager.printMessage("[" + this.name + "] Birthday! Age: " + this.age
                    + ". Stage unchanged: " + this.currentStage.getStageName() + ".");
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