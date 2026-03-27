package simcli.world.interactables;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class InteractableFactory {
    private static final Map<String, Supplier<Interactable>> FACTORY = new HashMap<>();
    static {
        FACTORY.put("Bed", Bed::new);
        FACTORY.put("Computer", Computer::new);
        FACTORY.put("Fridge", Fridge::new);
        FACTORY.put("Weight Bench", WeightBench::new);
        FACTORY.put("Shower", Shower::new);
        FACTORY.put("Storage Chest", StorageChest::new);
    }

    public static Interactable create(String name) {
        Supplier<Interactable> supplier = FACTORY.get(name);
        return supplier != null ? supplier.get() : null;
    }
}
