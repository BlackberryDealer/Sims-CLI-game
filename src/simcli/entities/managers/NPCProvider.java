package simcli.entities.managers;

import java.util.List;
import simcli.entities.actors.NPCSim;

/**
 * Abstraction for classes that provide a pool of NPC Sims.
 *
 * <p>Used to decouple the {@link simcli.world.Park} from the concrete
 * {@link NPCManager} implementation (Dependency Inversion Principle).
 * Any class that manages a collection of NPCs can implement this
 * interface to be used as an NPC source.</p>
 */
public interface NPCProvider {

    /**
     * Returns the list of currently active NPC Sims.
     *
     * @return the active NPC list.
     */
    List<NPCSim> getActiveNPCs();
}
