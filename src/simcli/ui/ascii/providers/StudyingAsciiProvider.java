package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Studying scene: the Sim pores over an open book until an idea lights up.
 */
public class StudyingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, true);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            frames.add(scene(player, false));
        }
        frames.add(scene(player, true));
        frames.add(scene(player, false));
        frames.add(scene(player, true));
        return frames;
    }

    private String scene(Sim player, boolean idea) {
        String right = (idea ? "         {Y}\\  |  /{/}" : "") + "\n"
                + (idea ? "        {Y}-- AHA! --{/}" : "") + "\n"
                + "\n"
                + "    {b}_______{/} {r}_______{/}\n"
                + "   {b}/~~~~~~{/} {k}V{/} {r}~~~~~~\\{/}\n"
                + "  {b}/~~~~~~  {/}{k}|{/}{r}  ~~~~~~\\{/}\n"
                + " {b}/_________{/}{k}|{/}{r}_________\\{/}";
        String face = Sprites.head(player, idea ? 'O' : 'o', idea ? "\\_/" : " - ");
        return Sprites.indent(Sprites.beside(face, right, 1), 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is studying hard");
    }
}
