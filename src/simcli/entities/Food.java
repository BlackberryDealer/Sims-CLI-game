package simcli.entities;

public class Food extends Consumable {
    public Food(String name, int price, int satiationValue, int energyValue) {
        super(name, price, satiationValue, energyValue, 0); // Food does not provide happiness
    }
    
    @Override
    public Item copyItem() {
        return new Food(getObjectName(), getPrice(), getSatiationValue(), getEnergyValue());
    }
}
