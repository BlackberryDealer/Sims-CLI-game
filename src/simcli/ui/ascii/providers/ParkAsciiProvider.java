package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * City Park: leafy trees, a sunny sky and a bench to sit and chat on.
 */
public class ParkAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return ""
                + "      {G}.-~~~-.{/}          {G}.-~~-.{/}                {Y}\\  |  /{/}\n"
                + "    {G}.' @ @ @ '.{/}      {G}.' @@ '.{/}               {Y}-- ( ) --{/}\n"
                + "   {G}( @ @ @ @ @ ){/}    {G}( @ @@ @ ){/}    {w}~~{/}         {Y}/  |  \\{/}\n"
                + "    {G}'. @ @ @ .'{/}      {G}'. @@ .'{/}   {w}~~~~{/}\n"
                + "      {G}'-._.-'{/}          {G}'-..-'{/}\n"
                + "        {y}| |{/}               {y}||{/}       {y}._______________.{/}\n"
                + "        {y}| |{/}               {y}||{/}       {y}|_______________|{/}\n"
                + "        {y}| |{/}               {y}||{/}        {k}||{/}           {k}||{/}\n"
                + "  {G},,,,,,{y}| |{G},,,,,,,,,,,,,,,{y}||{G},,,,,,,,,{k}||{G},,,,,,,,,,,{k}||{G},,,,,{/}";
    }
}
