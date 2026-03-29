package simcli.world.interactables;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory that creates {@link Interactable} instances from a furniture name
 * string. Uses a static registry of {@link java.util.function.Supplier}s —
 * new furniture types are added by registering them in the static block.
 */
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
