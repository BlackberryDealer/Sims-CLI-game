package simcli.needs;

public class Hygiene extends Need {
    public Hygiene() {
        super("Hygiene");
    }

    @Override
    public void decay() {
        this.decrease(5);
    }
}
