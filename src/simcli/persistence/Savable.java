package simcli.persistence;

import simcli.entities.items.*;

public interface Savable {
    String toSaveString();

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
