package simcli.needs;

public class Energy extends Need {
    public Energy() {
        super("Energy");
    }

    @Override
    public void decay() {
        this.decrease(2);
    }

    @Override
    public void decay(double multiplier) {
        this.decrease((int) Math.round(2 * multiplier));
    }
}