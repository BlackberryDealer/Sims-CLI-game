package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Workout scene: the Sim pumps a barbell up and down.
 */
public class ExercisingAsciiProvider implements IAsciiProvider {

    private static final String BARBELL = "{k}[#]{W}=============={k}[#]{/}";

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, true, 3);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int rep = 1; rep <= 3; rep++) {
            frames.add(scene(player, false, rep - 1));
            frames.add(scene(player, true, rep));
        }
        return frames;
    }

    private String scene(Sim player, boolean up, int reps) {
        StringBuilder right = new StringBuilder();
        for (int row = 0; row < 7; row++) {
            if ((up && row == 1) || (!up && row == 5)) {
                right.append(BARBELL);
            } else if (row == 3) {
                right.append("      {Y}reps: ").append(reps).append("{/}");
            }
            if (row < 6) right.append('\n');
        }
        String face = Sprites.head(player, '>', up ? "===" : "\\_/");
        return Sprites.indent(Sprites.beside(face, right.toString(), 2), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is pumping iron");
    }
}
