package simcli.needs;
public class Hunger extends Need {
    public Hunger() {
        super("Hunger");
    }
    
    @Override
    public void decay() {
        this.decrease(5); // Hunger decays faster than other needs
    }
}