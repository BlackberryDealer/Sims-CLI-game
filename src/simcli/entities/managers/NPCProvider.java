package simcli.entities.managers;

import java.util.List;
import simcli.entities.actors.NPCSim;

/**
 * Abstraction for classes that provide a pool of NPC Sims.
 * Used to decouple the {@link simcli.world.Park} from the concrete
 * {@link NPCManager} implementation (Dependency Inversion).
 */
public interface NPCProvider {
    List<NPCSim> getActiveNPCs();
}
