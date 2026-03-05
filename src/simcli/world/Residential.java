package simcli.world;

import simcli.entities.Sim;
import simcli.world.interactables.Interactable;
import java.util.ArrayList;
import java.util.List;

public class Residential extends Building {
    private List<Room> rooms;

    public Residential(String name) {
        super(name);
        this.rooms = new ArrayList<>();
    }

    public void addRoom(Room r) {
        this.rooms.add(r);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    @Override
    public List<Interactable> getInteractables() {
        List<Interactable> all = new ArrayList<>(super.getInteractables());
        for (Room r : rooms) {
            all.addAll(r.getInteractables());
        }
        return all;
    }

    @Override
    public void enter(Sim sim) {
        System.out.println(sim.getName() + " has arrived at their home: " + this.name);
        if (!this.rooms.isEmpty()) {
            sim.setCurrentRoom(this.rooms.get(0));
            System.out.println("They entered the " + this.rooms.get(0).getName() + ".");
        }
    }
}