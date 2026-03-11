package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.entities.Sim;
import java.util.Scanner;

public class Shower implements Interactable {
    public Shower() {
    }

    @Override
    public String getObjectName() {
        return "Shower";
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        System.out.println(sim.getName() + " takes a long, refreshing shower.");
        sim.getHygiene().increase(50);
        System.out.println(
                "Hygiene is now " + sim.getHygiene().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
    }
}
