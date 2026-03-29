package simcli.world;

import java.util.ArrayList;
import java.util.List;
import simcli.entities.items.Furniture;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.world.interactables.Interactable;

/**
 * A subdivided area within a {@link Residential} building that holds
 * furniture interactables (e.g. Bed, Fridge, Shower). Each room has a
 * maximum capacity that limits how much furniture can be placed inside;
 * capacity can be upgraded at a cost.
 */
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

    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int capacity) {
        this.maxCapacity = capacity;
    }

    public int getUsedCapacity() {
        return currentCapacityUsed;
    }

    public List<Interactable> getInteractables() {
        return interactablesList;
    }

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
            UIManager.printMessage("Success! " + this.name + " upgraded. New capacity: " + this.maxCapacity);
        } else {
            UIManager.printMessage("Not enough money! Upgrade costs $" + cost);
        }
    }
}
