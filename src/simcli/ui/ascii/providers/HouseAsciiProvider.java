package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * The Shared Dorm: a cosy brick block with a smoking chimney.
 */
public class HouseAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return ""
                + "            {k}(  )  (  ){/}\n"
                + "             {k}){/}{r}||{/}    {r}||{/}\n"
                + "        {r}______||____||___________________{/}\n"
                + "       {r}/                                /|{/}\n"
                + "      {r}/   {Y}~ T H E   D O R M ~{r}          / |{/}\n"
                + "     {r}/________________________________/  |{/}\n"
                + "     {r}|  {C}[##]  [##]{r}   {y}____{r}   {C}[##]  [##]{r}|  |{/}\n"
                + "     {r}|  {C}[##]  [##]{r}  {y}|    |{r}  {C}[##]  [##]{r}|  /{/}\n"
                + "     {r}|______________{y}|   o|{r}____________| /{/}\n"
                + "   {G},,'',,,'',,,,'',,,,,'',,,,'',,,,'',,,'',,{/}";
    }
}
