package simcli.world;

import java.util.ArrayList;
import java.util.List;
import simcli.entities.Furniture;
import simcli.entities.Sim;
import simcli.world.interactables.Interactable;

public class Room {
    private String name;
    private int maxCapacity;
    private int currentCapacityUsed;
    private List<Interactable> interactablesList;

    public Room(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentCapacityUsed = 0;
        this.interactablesList = new ArrayList<>();
    }

    public String getName() { return name; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getUsedCapacity() { return currentCapacityUsed; }
    public List<Interactable> getInteractables() { return interactablesList; }

    public boolean canFit(Furniture f) {
        return currentCapacityUsed + f.getSpaceScore() <= maxCapacity;
    }

    public void placeFurniture(Interactable interactableInstance, int spaceScore) {
        interactablesList.add(interactableInstance);
        currentCapacityUsed += spaceScore;
    }
    
    public void upgradeCapacity(Sim sim, int extraSpace, int cost) {
        if (sim.getMoney() >= cost) {
            sim.setMoney(sim.getMoney() - cost);
            this.maxCapacity += extraSpace;
            System.out.println("Success! " + this.name + " upgraded. New capacity: " + this.maxCapacity);
        } else {
            System.out.println("Not enough money! Upgrade costs $" + cost);
        }
    }
}
