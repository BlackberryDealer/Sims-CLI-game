package simcli.world;
import java.util.ArrayList;
import java.util.List;

import simcli.entities.actors.Sim;
import simcli.world.interactables.Interactable;

public abstract class Building {
    private String name;
    private List<Interactable> interactables;
    
    public Building(String name) {
        this.name = name;
        this.interactables = new ArrayList<>();
    }

    public String getName () {return this.name;}

    public boolean isResidential() { return false; }

    
    public abstract void enter(Sim sim);
    
    public void addInteractable(Interactable item) {
        this.interactables.add(item);
    }
    
    public List<Interactable> getInteractables() {
        return this.interactables;
    }
}