package simcli.world;

import simcli.entities.actors.Sim;
import simcli.world.interactables.Interactable;
import java.util.ArrayList;
import java.util.List;

public class Residential extends Building {
    private List<Room> rooms;
    private int purchasePrice;
    private boolean isOwned;

    public Residential(String name) {
        super(name);
        this.rooms = new ArrayList<>();
        this.purchasePrice = 0;
        this.isOwned = true; // Free residential — owned by default
    }

    public Residential(String name, int purchasePrice) {
        super(name);
        this.rooms = new ArrayList<>();
        this.purchasePrice = purchasePrice;
        this.isOwned = false; // Must be purchased
    }

    public void addRoom(Room r) {
        this.rooms.add(r);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public int getPurchasePrice() { return purchasePrice; }
    public boolean isOwned() { return isOwned; }
    public void setOwned(boolean owned) { this.isOwned = owned; }

    public boolean purchase(Sim sim) {
        if (sim.getMoney() >= purchasePrice) {
            sim.setMoney(sim.getMoney() - purchasePrice);
            this.isOwned = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean isResidential() {
        return true;
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
        simcli.ui.UIManager.printMessage(sim.getName() + " has arrived at their home: " + getName());
        if (!this.rooms.isEmpty()) {
            sim.setCurrentRoom(this.rooms.get(0));
            simcli.ui.UIManager.printMessage("They entered the " + this.rooms.get(0).getName() + ".");
        }
    }
}