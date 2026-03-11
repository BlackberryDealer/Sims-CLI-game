package simcli.entities;

import simcli.engine.SimulationException;

/**
 * Contract that every controllable/playable Sim type must fulfil.
 * Decouples GameEngine from concrete Sim subclasses — the engine works
 * through this interface rather than casting to AdultSim or ChildSim directly.
 */
public interface ISimBehaviour {

    /** Execute an activity by type name (e.g. "Work", "Study", "Play"). */
    void performActivity(String activityType) throws SimulationException;

    /** Advance one game tick: decay needs, update state. */
    void tick();

    /** Called once per in-game day; handles ageing logic. */
    void growOlderDaily();

    /** Returns the Sim's current lifecycle state. */
    SimState getState();

    /** Returns the Sim's display name. */
    String getName();
}
