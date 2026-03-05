package simcli.entities;

import simcli.engine.SimulationException;

import simcli.needs.Need;
import simcli.needs.Hunger;
import simcli.needs.Energy;
import simcli.needs.Hygiene;
import simcli.world.Room;

import java.util.ArrayList;
import java.util.List;

public abstract class Sim {

    protected String name;
    protected int age;
    protected int money;
    protected Need hunger;
    protected Need energy;
    protected Need hygiene;
    protected SimState state;
    protected List<Item> inventory;
    protected int starvingTicks;
    protected Room currentRoom;
    protected int daysAlive;

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
        double ageMultiplier = 1.0 + (Math.max(0, this.age - 18) * 0.05); // 5% faster stat decay per year over 18
        int decayAmount = (int) Math.round(5 * ageMultiplier);

        this.hunger.decrease(decayAmount);
        this.energy.decrease(decayAmount);
        this.hygiene.decrease(decayAmount);
        this.updateState();

        System.out.println("[" + this.name + "] Hunger: " + this.hunger.getValue() +
                " | Energy: " + this.energy.getValue() +
                " | Hygiene: " + this.hygiene.getValue() +
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
        if (this.daysAlive % 7 == 0) {
            this.age++;
            System.out.println("\n*** BIRTHDAY! " + this.name + " has aged up to " + this.age + " years old! ***");
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