package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Working scene: the Sim types away at a laptop while a progress bar fills.
 */
public class WorkingAsciiProvider implements IAsciiProvider {

    private static final int STEPS = 6;

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, STEPS);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int step = 0; step <= STEPS; step++) {
            frames.add(scene(player, step));
        }
        return frames;
    }

    private String scene(Sim player, int step) {
        String bar = "[" + "#".repeat(step) + " ".repeat(STEPS - step) + "]";
        String status = step >= STEPS ? "{G} Shift done! {/}" : "{G} working...  {/}";
        String laptop = "\n"
                + "  {k}.-------------.{/}\n"
                + "  {k}|{/}{G} $ ./work.sh {/}{k}|{/}\n"
                + "  {k}|{/}{G}  " + bar + "   {/}{k}|{/}\n"
                + "  {k}|{/}" + status + "{k}|{/}\n"
                + "  {k}|_____________|{/}\n"
                + " {k}/_______________\\{/}";
        String face = Sprites.head(player, 'o', step % 2 == 0 ? "---" : "-o-");
        return Sprites.indent(Sprites.beside(face, laptop, 2), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is on shift as a "
                        + player.getCareer().getTitle());
    }
}
