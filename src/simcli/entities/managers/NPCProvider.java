package simcli.entities.managers;

import java.util.List;
import simcli.entities.actors.NPCSim;

/**
 * Represents the NPCProvider entity or state in the simulation.
 */
public interface NPCProvider {
    List<NPCSim> getActiveNPCs();
}
