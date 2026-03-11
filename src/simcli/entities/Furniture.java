package simcli.entities;

import simcli.engine.SimulationException;

public class Furniture extends Item {
    private int spaceScore;

    public Furniture(String name, int price, int spaceScore) {
        super(name, price);
        this.spaceScore = spaceScore;
    }

    public int getSpaceScore() { return spaceScore; }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        // Will be handed by the Room system later. For now, it just sits in inventory.
        throw new SimulationException("You must place " + this.name + " in a room to use it!");
    }

    @Override
    public Item copyItem() {
        return new Furniture(this.name, this.price, this.spaceScore);
    }
}
