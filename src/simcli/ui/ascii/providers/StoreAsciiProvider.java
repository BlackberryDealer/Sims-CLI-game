package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * StoreAsciiProvider — 2D front-view of a market/supermarket building.
 *
 * Single authoritative source of store ASCII art.
 * Replaces the duplicate printStore() that existed in AsciiArt.java.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class StoreAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String locName = (location != null) ? location.getName() : "Market";
        return "^             ^               ^!^\n" +
            "   ^ _______________________________\n" +
            "    [=U=U=U=U=U=U=U=U=U=U=U=U=U=U=U=]\n" +
            "    |.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.|\n" +
            "    |        +-+-+-+-+-+-+-+        |\n" +
            "    |        | Supermarket |        |\n" +
            "    |        +-+-+-+-+-+-+-+        |\n" +
            "    |.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.|\n" +
            "    |  _________  __ __  _________  |\n" +
            "  _ | |___   _  ||[]|[]||  _      | | _\n" +
            " (!)||OPEN|_(!)_|| ,| ,||_(!)_____| |(!)\n" +
            ".T~T|:.....:T~T.:|__|__|:.T~T.:....:|T~T.\n" +
            "||_||||||||||_|||||||||||||_||||||||||_||\n" +
            "~\\=/~~~~~~~~\\=/~~~~~~~~~~~\\=/~~~~~~~~\\=/~\n" +
            "  | -------- | ----------- | -------- |\n" +
            "~ |~^ ^~~^ ~~| ~^  ~~ ^~^~ |~ ^~^ ~~^ |^~" +
                "   [ " + locName + " ]";
    }
}
