package simcli.entities;

import simcli.world.interactables.Interactable;

public abstract class Item implements Interactable {
    protected String name;
    protected int price;

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public int getPrice() { return price; }
    
    @Override
    public String getObjectName() { return name; }
}
