package simcli.world.interactables;

import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.persistence.GameContent;

import java.util.List;

/**
 * A shop whose stock comes from {@code shops.json}: the same
 * {@link AbstractShop} buying flow, with the catalog defined as data.
 *
 * <p>Adding a new shop only needs a new entry in {@code shops.json} and a
 * building that uses it in {@code buildings.json}.</p>
 */
public class CatalogShop extends AbstractShop {

    private final String shopId;

    /**
     * Creates a shop from its entry in {@code shops.json}.
     *
     * @param shopId the shop's id.
     */
    public CatalogShop(String shopId) {
        super(GameContent.registerName(shopId));
        this.shopId = shopId;
    }

    /**
     * Returns the shop's id in {@code shops.json}.
     *
     * @return the shop id.
     */
    public String getShopId() {
        return shopId;
    }

    @Override
    protected List<Item> getCatalog(Sim sim) {
        return GameContent.shopCatalog(shopId, sim);
    }
}
