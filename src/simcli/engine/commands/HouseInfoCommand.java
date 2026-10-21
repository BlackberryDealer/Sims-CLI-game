package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Interactable;

import java.util.ArrayList;
import java.util.List;

/**
 * Command that displays information about the current residential building.
 *
 * <p>Lists every room with its used/maximum capacity and the furniture
 * placed inside. Only works when the active Sim is in a residential
 * building.</p>
 */
public class HouseInfoCommand extends BaseCommand {

    /**
     * Constructs a {@code HouseInfoCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public HouseInfoCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Prints a room-by-room breakdown of the current residential building.
     *
     * @return {@link CommandResult#NO_TICK} — viewing house info does not advance time.
     */
    @Override
    protected CommandResult run() {
        Building currentLocation = ctx.getCurrentLocation();

        if (currentLocation.isResidential()) {
            Residential res = (Residential) currentLocation;
            List<String> lines = new ArrayList<>();
            for (Room r : res.getRooms()) {
                int used = r.getUsedCapacity();
                int max = r.getMaxCapacity();
                int pct = max == 0 ? 100 : used * 100 / max;
                // the bar shows free space, so a fuller room shows as "redder"
                String space = Panel.gauge(100 - pct, 10);
                lines.add(Style.fit("{W}" + r.getName() + "{/}", 18) + space
                        + Style.fit("  " + used + "/" + max + " space", 16) + contents(r));
            }
            int bills = 0;
            for (Building b : ctx.getWorldManager().getCityMap()) {
                if (b instanceof Residential && ((Residential) b).isOwned()) {
                    bills += ((Residential) b).getWeeklyBill();
                }
            }
            lines.add("");
            lines.add("{k}Bills{/}  {Y}$" + bills + "{/} {k}every Monday for all the homes you own"
                    + " (this one: $" + res.getWeeklyBill() + "){/}");
            System.out.println(Panel.box("{W}" + res.getName() + "{/}", 'm', lines));
            UIManager.printMessage("  {k}Tip: upgrade a room with [U] to fit more furniture.{/}");
        } else {
            UIManager.info("You can only inspect residential buildings.");
        }
        pause();
        return CommandResult.NO_TICK;
    }

    /** Comma-separated furniture in a room, or "empty". Shared with the room picker. */
    static String contents(Room room) {
        List<Interactable> items = room.getInteractables();
        if (items.isEmpty()) {
            return "{k}empty{/}";
        }
        StringBuilder sb = new StringBuilder("{k}");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(items.get(i).getObjectName());
        }
        return sb.append("{/}").toString();
    }
}
