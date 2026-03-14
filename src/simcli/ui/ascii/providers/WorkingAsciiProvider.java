package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * WorkingAsciiProvider — 2D side-view of a stickman Sim typing at a desk.
 *
 * Visual: monitor on desk, stickman seated and typing, career title shown on
 * screen.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class WorkingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String job = player.getCareer().getTitle();
        return "   .---------.         \n" +
                "   |  [" + padCenter(job, 7) + "] |  \n" +
                "   |  _______  |       \n" +
                "   | |  :D   | |   o   \n" +
                "   | |_______| |  /|\\  \n" +
                "   '-----------'  / \\  \n" +
                "   [___keyboard__]      \n" +
                "   \" " + player.getName() + " is working! \"";
    }

    /** Pads or trims a string to a fixed width, centering the text. */
    private String padCenter(String text, int width) {
        if (text.length() >= width)
            return text.substring(0, width);
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text + " ".repeat(width - text.length() - pad);
    }
}
