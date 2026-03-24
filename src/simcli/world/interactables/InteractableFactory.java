package simcli.world.interactables;

public class InteractableFactory {
    private static final java.util.Map<String, java.util.function.Supplier<Interactable>> FACTORY = new java.util.HashMap<>();
    static {
        FACTORY.put("Bed", Bed::new);
        FACTORY.put("Computer", Computer::new);
        FACTORY.put("Fridge", Fridge::new);
        FACTORY.put("Weight Bench", WeightBench::new);
        FACTORY.put("Shower", Shower::new);
        FACTORY.put("Storage Chest", StorageChest::new);
    }

    public static Interactable create(String name) {
        java.util.function.Supplier<Interactable> supplier = FACTORY.get(name);
        return supplier != null ? supplier.get() : null;
    }
}
