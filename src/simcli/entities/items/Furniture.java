package simcli.entities.items;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;

import simcli.engine.SimulationException;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.InteractableFactory;

import java.util.Scanner;

/**
 * A placeable item that occupies space in a {@link simcli.world.Room}.
 *
 * <p>Furniture cannot be used directly from the inventory — it must be
 * placed into a room first, at which point it creates a corresponding
 * {@link Interactable} via {@link InteractableFactory}.</p>
 */
public class Furniture extends Item {

    /** The amount of room space this furniture occupies. */
    private int spaceScore;

    /**
     * Constructs a new furniture item.
     *
     * @param name       the display name of the furniture.
     * @param price      the purchase price in Simoleons.
     * @param spaceScore the amount of room capacity this item requires.
     */
    public Furniture(String name, int price, int spaceScore) {
        super(name, price);
        this.spaceScore = spaceScore;
    }

    /**
     * Returns the room space required by this furniture piece.
     *
     * @return the space score.
     */
    public int getSpaceScore() {
        return spaceScore;
    }

    /**
     * Creates the in-world {@link Interactable} version of this furniture
     * piece using the {@link InteractableFactory}.
     *
     * @return a new {@code Interactable} corresponding to this furniture type.
     */
    public Interactable createInteractable() {
        return InteractableFactory.create(getObjectName());
    }

    /**
     * Attempting to interact with furniture from the inventory throws an
     * exception — furniture must be placed in a room before use.
     *
     * {@inheritDoc}
     *
     * @throws SimulationException always, indicating the furniture must be placed first.
     */
    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        // Will be handled by the Room system later. For now, it just sits in inventory.
        throw new SimulationException("You must place " + getObjectName() + " in a room to use it!");
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@code Furniture} with identical properties.
     */
    @Override
    public Item copyItem() {
        return new Furniture(getObjectName(), getPrice(), this.spaceScore);
    }

    /**
     * {@inheritDoc}
     *
     * @return a CSV-formatted string: {@code Furniture,name,price,spaceScore}.
     */
    @Override
    public String toSaveString() {
        return String.format("Furniture,%s,%d,%d", getObjectName(), getPrice(), getSpaceScore());
    }
}
