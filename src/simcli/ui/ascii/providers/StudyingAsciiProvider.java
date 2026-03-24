package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * StudyingAsciiProvider — 2D side-view of a stickman Sim reading at a desk.
 *
 * Visual: stickman leaning over an open book on a desk, stack of books beside
 * them.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class StudyingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "          __    __\n" +
            "         /  \\ /| |'-.\n" +
            "        .\\__/ || |   |\n" +
            "     _ /  `._ \\|_|_.-'\n" +
            "    | /  \\__.`=._) (_\n" +
            "    |/ ._/  |\"\"\"\"\"\"\"\"\"|\n" +
            "    |'.  `\\ |         |\n" +
            "    ;\"\"\"/ / |         |\n" +
            "     ) /_/| |.-------.|\n" +
            "    '  `-`' \"         \"" +
                "   \" " + player.getName() + " is studying! \"";
    }
}
