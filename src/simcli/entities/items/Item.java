package simcli.entities.items;

import simcli.world.interactables.Interactable;

public abstract class Item implements Interactable {
    private String name;
    private int price;

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public int getPrice() { return price; }
    
    @Override
    public String getObjectName() { return name; }

    public abstract Item copyItem();
}
