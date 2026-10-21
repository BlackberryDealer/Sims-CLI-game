package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * The Bungalow: a detached home with a garden tree, a picket fence and
 * a path up to the front door.
 */
public class BungalowAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return ""
                + "                                  {r}/\\{/}\n"
                + "     {G}.oOOo.{/}                      {r}/  \\{/}               {Y}\\ | /{/}\n"
                + "   {G}.oOOOOOOo.{/}                   {r}/    \\{/}             {Y}- ( ) -{/}\n"
                + "   {G}'oOOOOOOo'{/}         {r}_________/      \\_________{/}    {Y}/ | \\{/}\n"
                + "     {G}'oOOo'{/}           {m}|                        |{/}\n"
                + "       {y}||{/}             {m}|  {C}[]{m}     {y}____{m}    {C}[]{m}     |{/}\n"
                + "       {y}||{/}             {m}|        {y}|    |{m}          |{/}\n"
                + "       {y}||{/}             {m}|________{y}|   o|{m}__________|{/}\n"
                + "  {w}|-|-|-|-|-|{G},,,,,,,,,,,,,,,,,,,{y}===={G},,,,,,,,,,,,,,,{w}|-|-|-|{/}";
    }
}
