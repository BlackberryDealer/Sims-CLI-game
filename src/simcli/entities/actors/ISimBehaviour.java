package simcli.entities.actors;

import simcli.entities.models.*;

/**
 * Contract that every controllable/playable Sim type must fulfil.
 *
 * <p>Decouples {@link simcli.engine.GameEngine} from concrete Sim subclasses
 * — the engine works through this interface rather than casting to
 * {@code AdultSim} or {@code ChildSim} directly.</p>
 */
public interface ISimBehaviour {

    /**
     * Advances one game tick: decays needs and updates state.
     */
    void tick();

    /**
     * Called once per in-game day; handles ageing logic.
     */
    void growOlderDaily();

    /**
     * Returns the Sim's current lifecycle state.
     *
     * @return the current {@link SimState}.
     */
    SimState getState();

    /**
     * Returns the Sim's display name.
     *
     * @return the Sim's name.
     */
    String getName();
}
