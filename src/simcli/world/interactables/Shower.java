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
    public void interact(Sim activePlayer, Scanner scanner) throws SimulationException {
        System.out.println(activePlayer.getName() + " takes a long, refreshing shower.");
        activePlayer.getHygiene().increase(50);
        System.out.println(
                "Hygiene is now " + activePlayer.getHygiene().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
    }
}
