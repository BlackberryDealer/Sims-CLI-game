package simcli.world;
import simcli.entities.Sim;

public class Residential extends Building {
    public Residential(String name) {
        super(name);
    }
    
    @Override
    public void enter(Sim sim) {
        System.out.println(sim.getName() + " has arrived at their home: " + this.name);
    }
}