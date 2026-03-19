package simcli.entities;

import simcli.engine.SimulationException;

public class Furniture extends Item {
    private int spaceScore;

    private static final java.util.Map<String, java.util.function.Supplier<simcli.world.interactables.Interactable>> FACTORY = new java.util.HashMap<>();
    static {
        FACTORY.put("Bed", simcli.world.interactables.Bed::new);
        FACTORY.put("Computer", simcli.world.interactables.Computer::new);
        FACTORY.put("Fridge", simcli.world.interactables.Fridge::new);
        FACTORY.put("Weight Bench", simcli.world.interactables.WeightBench::new);
        FACTORY.put("Shower", simcli.world.interactables.Shower::new);
        FACTORY.put("Storage Chest", simcli.world.interactables.StorageChest::new);
    }

    public Furniture(String name, int price, int spaceScore) {
        super(name, price);
        this.spaceScore = spaceScore;
    }

    public int getSpaceScore() { return spaceScore; }

    public simcli.world.interactables.Interactable createInteractable() {
        java.util.function.Supplier<simcli.world.interactables.Interactable> supplier = FACTORY.get(getObjectName());
        return supplier != null ? supplier.get() : null;
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
