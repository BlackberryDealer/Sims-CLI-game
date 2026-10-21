package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Leisure scene (reading a novel, a comic, a spritz of perfume...): the
 * Sim beams while sparkles pop around them.
 */
public class PlayingAsciiProvider implements IAsciiProvider {

    private static final String[][] SPARKLES = {
            {"  {M}*{/}       {Y}+{/}", "        {C}o{/}", "   {Y}+{/}        {M}*{/}", "       {G}*{/}"},
            {"       {C}+{/}    {M}o{/}", "  {Y}*{/}", "         {C}*{/}", "  {M}+{/}     {Y}o{/}"},
            {"   {Y}o{/}     {G}*{/}", "      {M}+{/}     {C}*{/}", " {C}*{/}", "     {Y}*{/}   {M}+{/}"},
    };

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, 0);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            frames.add(scene(player, i % SPARKLES.length));
        }
        return frames;
    }

    private String scene(Sim player, int pattern) {
        String right = "\n" + String.join("\n", SPARKLES[pattern]) + "\n\n    {M}Yay!{/}";
        return Sprites.indent(Sprites.beside(Sprites.head(player, '^', "\\_/"), right, 2), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is having a great time");
    }
}
