package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Shower scene: water pours down while the Sim sings.
 */
public class BathingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, 0);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            frames.add(scene(player, i % 2));
        }
        return frames;
    }

    private String scene(Sim player, int phase) {
        String a = "{C}'  '  '  '{/} ";
        String b = "{C} '  '  '  '{/}";
        String right = "{k}  ______{/}\n"
                + "{k} [______]=={/}\n"
                + (phase == 0 ? a : b) + "\n"
                + (phase == 0 ? b : a) + "     {M}la la la~{/}\n"
                + (phase == 0 ? a : b) + "\n"
                + (phase == 0 ? b : a) + "\n"
                + "{c}~~~~~~~~~~~~{/}";
        return Sprites.indent(Sprites.beside(Sprites.head(player, '^', " o "), right, 2), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is taking a refreshing shower");
    }
}
