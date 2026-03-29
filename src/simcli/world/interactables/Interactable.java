package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;

import java.util.Scanner;

/**
 * Contract for in-world objects that a Sim can interact with (e.g. Bed,
 * Fridge, Shower, shop shelves). Each interactable defines its own
 * behaviour when a Sim uses it.
 */
public interface Interactable {

    /**
     * Performs the interaction between the Sim and this object.
     *
     * @param sim         the Sim performing the interaction.
     * @param scanner     for reading follow-up user input if needed.
     * @param timeManager the simulation clock (for time-dependent actions).
     * @throws SimulationException if game rules prevent the interaction.
     */
    void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException;

    /**
     * Returns the display name of this interactable object.
     *
     * @return the object's name (e.g. "Bed", "Fridge").
     */
    String getObjectName();
}