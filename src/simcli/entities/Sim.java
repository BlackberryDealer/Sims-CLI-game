package simcli.entities;
import simcli.engine.SimulationException;

import simcli.needs.Need;
import simcli.needs.Hunger;
import simcli.needs.Energy;

public abstract class Sim {

    protected String name;
    protected int age;
    protected int money; 
    protected Need hunger;
    protected Need energy;
    protected SimState state;
    protected int groceries;

    public Sim(String name, int age) {
        this.name = name;
        this.age = age;
        this.money = 500; // Starting Simoleons
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.state = SimState.HEALTHY;
        this.groceries = 0;
    }

    // getters for .txt saving
    public String getName() { return name; }
    public int getAge() { return age; }
    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }
    public Need getHunger() { return hunger; }
    public Need getEnergy() { return energy; }
    public SimState getState() { return state; }

    public abstract void performActivity(String activityType) throws SimulationException;

    public void tick() {
        if (this.state == SimState.DEAD) return;
        this.hunger.decay();
        this.energy.decay();
        this.updateState();
        
        System.out.println("[" + this.name + "] Hunger: " + this.hunger.getValue() + 
                           " | Energy: " + this.energy.getValue() + 
                           " | Cash: $" + this.money + " | Status: " + this.state);
    }

    public void updateState() {
        if (this.hunger.getValue() <= 0) this.state = SimState.DEAD;
        else if (this.hunger.getValue() <= 20) this.state = SimState.HUNGRY;
        else if (this.energy.getValue() <= 20) this.state = SimState.TIRED;
        else this.state = SimState.HEALTHY;
    }

    public int getGroceries() { return groceries; }
    public void setGroceries(int amount) { this.groceries = amount; }
}