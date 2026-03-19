package simcli.needs;

public class Hunger extends Need {
    // Hunger decays faster than other needs
    public Hunger() {
        super("Hunger", 5);
    }
}