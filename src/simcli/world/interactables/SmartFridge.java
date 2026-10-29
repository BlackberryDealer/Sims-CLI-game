package simcli.world.interactables;

/**
 * A better fridge: food stored in it goes off half as fast (it only loses
 * freshness every other day; see {@link simcli.engine.UpkeepManager}).
 */
public class SmartFridge extends Fridge {

    @Override
    public String getObjectName() {
        return "Smart Fridge";
    }
}
