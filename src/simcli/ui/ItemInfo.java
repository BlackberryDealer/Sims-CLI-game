package simcli.ui;

import simcli.entities.items.Consumable;
import simcli.entities.items.Furniture;
import simcli.entities.items.Item;

/**
 * Short, coloured descriptions of what an item does, for shop and
 * inventory lists, e.g. {@code +50 hunger +20 energy}.
 */
public final class ItemInfo {

    private ItemInfo() {
    }

    /** Describes the item's effect (or footprint, for furniture). */
    public static String describe(Item item) {
        if (item instanceof Furniture) {
            return "{b}furniture{/} {k}" + Panel.DOT + " needs " + ((Furniture) item).getSpaceScore() + " space{/}";
        }
        if (item instanceof Consumable) {
            Consumable c = (Consumable) item;
            StringBuilder sb = new StringBuilder();
            effect(sb, c.getSatiationValue(), "hunger");
            effect(sb, c.getEnergyValue(), "energy");
            effect(sb, c.getHappinessValue(), "fun");
            return sb.length() == 0 ? "" : sb.toString().trim();
        }
        return "";
    }

    private static void effect(StringBuilder sb, int value, String need) {
        if (value != 0) {
            sb.append(value > 0 ? "{G}+" : "{R}").append(value).append(' ').append(need).append("{/} ");
        }
    }
}
