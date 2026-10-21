package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * Socializing scene: the Sim and a friend trade a few lines of chit-chat.
 */
public class SocializingAsciiProvider implements IAsciiProvider {

    private static final String[] LINES = {
            "{C}\"Hey there!\"{/}",
            "      {M}\"Oh, hi!\"{/}",
            "{C}\"Nice weather!\"{/}",
            "      {M}\"Haha, yes!\"{/}",
    };

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return scene(player, LINES.length);
    }

    @Override
    public List<String> getFrames(Sim player, Building location) {
        List<String> frames = new ArrayList<>();
        for (int shown = 1; shown <= LINES.length; shown++) {
            frames.add(scene(player, shown));
            frames.add(scene(player, shown));
        }
        return frames;
    }

    private String scene(Sim player, int shown) {
        StringBuilder chat = new StringBuilder("\n\n");
        for (int i = 0; i < LINES.length; i++) {
            chat.append(i < shown ? LINES[i] : "").append('\n');
        }
        // fixed-width chat column so the friend doesn't jump between frames
        String chatColumn = chat.toString() + " ".repeat(18);
        String friend = "\n\n"
                + "  {w}.-\"\"\"-.{/}\n"
                + " {w}/ o   o \\{/}\n"
                + "{w}|    ^    |{/}\n"
                + " {w}\\  \\_/  /{/}\n"
                + "  {w}'-...-'{/}";
        String twoOfThem = Sprites.beside(Sprites.beside(Sprites.head(player, 'o', "\\_/"), chatColumn, 2),
                friend, 2);
        return Sprites.indent(twoOfThem, 4)
                + Sprites.caption("{W}" + player.getName() + "{/} is catching up with a friend");
    }
}
