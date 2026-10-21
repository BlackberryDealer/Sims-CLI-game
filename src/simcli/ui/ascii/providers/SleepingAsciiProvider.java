package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Sleeping scene: the Sim snoozes under a blanket while Z's drift upward.
 */
public class SleepingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, 3);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int cycle = 0; cycle < 2; cycle++) {
            for (int z = 0; z <= 3; z++) {
                frames.add(scene(player, z));
            }
        }
        return frames;
    }

    /** @param zs how many Z's are floating (0-3). */
    private String scene(Sim player, int zs) {
        String right = (zs >= 3 ? "             {C}z{/}" : "") + "\n"
                + (zs >= 2 ? "         {C}Z{/}" : "") + "\n"
                + (zs >= 1 ? "     {C}z{/}" : "") + "\n"
                + "\n"
                + "{b}.=========================.{/}\n"
                + "{b}|{B}~~~~~~~~~~~~~~~~~~~~~~~~~{b}|{/}\n"
                + "{b}'========================='{/}";
        return Sprites.indent(Sprites.beside(Sprites.head(player, '-', " o "), right, 1), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is fast asleep");
    }
}
