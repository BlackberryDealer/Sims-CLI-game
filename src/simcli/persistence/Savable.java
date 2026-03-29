package simcli.persistence;

import simcli.entities.items.*;

/**
 * Interface for objects that can be saved to and loaded from a string format.
 * This is used for persisting item data and other game objects.
 */
public interface Savable {
    /**
     * Converts the object state into a string format suitable for saving.
     * @return a comma-separated string representation of the object.
     */
    String toSaveString();

    /**
     * Creates a Savable object from a saved string representation.
     * @param data the saved string representation of the object.
     * @return a new Savable object (Furniture, Food, or Consumable), or null if the type is unknown.
     */
    static Savable fromSaveString(String data) {
        String[] parts = data.split(",");
        String type = parts[0];
        if (type.equals("Furniture")) {
            return new Furniture(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } else if (type.equals("Food")) {
            return new Food(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
        } else if (type.equals("Consumable")) {
            return new Consumable(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
        }
        return null;
    }
}
