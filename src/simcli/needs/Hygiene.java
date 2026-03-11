package simcli.needs;

public class Hygiene extends Need {
    public Hygiene() {
        super("Hygiene");
    }

    @Override
    public void decay() {
        this.decrease(5);
    }

    @Override
    public void decay(double multiplier) {
        this.decrease((int) Math.round(5 * multiplier));
    }
}
