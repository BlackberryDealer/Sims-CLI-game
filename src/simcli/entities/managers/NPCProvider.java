package simcli.entities.managers;

import java.util.List;
import simcli.entities.actors.NPCSim;

public interface NPCProvider {
    List<NPCSim> getActiveNPCs();
}
