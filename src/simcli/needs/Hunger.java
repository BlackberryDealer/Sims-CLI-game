package simcli.needs;

public class Hunger extends Need {
    public Hunger() {
        super("Hunger");
    }

    @Override
    public void decay() {
        this.decrease(5); // Hunger decays faster than other needs
    }

    @Override
    public void decay(double multiplier) {
        this.decrease((int) Math.round(5 * multiplier));
    }
}