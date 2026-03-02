package simcli.world;
import java.util.ArrayList;
import java.util.List;

import simcli.entities.Sim;

public abstract class Building {
    protected String name;
    protected List<Interactable> interactables;
    
    public Building(String name) {
        this.name = name;
        this.interactables = new ArrayList<>();
    }
    
    public abstract void enter(Sim sim);
    
    public void addInteractable(Interactable item) {
        this.interactables.add(item);
    }
    
    public List<Interactable> getInteractables() {
        return this.interactables;
    }
}