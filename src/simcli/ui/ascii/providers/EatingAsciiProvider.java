package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Eating scene: the Sim chomps away at a burger.
 */
public class EatingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, false);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            frames.add(scene(player, i % 2 == 0));
        }
        frames.add(scene(player, false));
        return frames;
    }

    private String scene(Sim player, boolean mouthOpen) {
        String burger = "\n"
                + "   {y}.-'''''-.{/}\n"
                + "  {y}/ {W}.  .  .{y} \\{/}\n"
                + "  {G}~~~~~~~~~~~{/}\n"
                + "  {r}###########{/}\n"
                + "  {y}'---------'{/}\n"
                + (mouthOpen ? "    {Y}munch!{/}" : "     {Y}nom~{/}");
        String face = Sprites.head(player, '^', mouthOpen ? "(O)" : "~~~");
        return Sprites.indent(Sprites.beside(face, burger, 3), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is enjoying a meal");
    }
}
