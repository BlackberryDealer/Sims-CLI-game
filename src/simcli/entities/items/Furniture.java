package simcli.entities.items;

import simcli.entities.actors.Sim;

import simcli.engine.SimulationException;

public class Furniture extends Item {
    private int spaceScore;

    public Furniture(String name, int price, int spaceScore) {
        super(name, price);
        this.spaceScore = spaceScore;
    }

    public int getSpaceScore() { return spaceScore; }

    public simcli.world.interactables.Interactable createInteractable() {
        return simcli.world.interactables.InteractableFactory.create(getObjectName());
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        // Will be handed by the Room system later. For now, it just sits in inventory.
        throw new SimulationException("You must place " + getObjectName() + " in a room to use it!");
    }

    @Override
    public Item copyItem() {
        return new Furniture(getObjectName(), getPrice(), this.spaceScore);
    }
}
