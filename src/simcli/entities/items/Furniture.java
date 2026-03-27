package simcli.entities.items;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;

import simcli.engine.SimulationException;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.InteractableFactory;

import java.util.Scanner;

public class Furniture extends Item {
    private int spaceScore;

    public Furniture(String name, int price, int spaceScore) {
        super(name, price);
        this.spaceScore = spaceScore;
    }

    public int getSpaceScore() { return spaceScore; }

    public Interactable createInteractable() {
        return InteractableFactory.create(getObjectName());
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        // Will be handed by the Room system later. For now, it just sits in inventory.
        throw new SimulationException("You must place " + getObjectName() + " in a room to use it!");
    }

    @Override
    public Item copyItem() {
        return new Furniture(getObjectName(), getPrice(), this.spaceScore);
    }

    @Override
    public String toSaveString() {
        return String.format("Furniture,%s,%d,%d", getObjectName(), getPrice(), getSpaceScore());
    }
}
