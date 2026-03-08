package simcli.needs;

public class Happiness extends Need {
    public Happiness() {
        super("Happiness");
    }

    @Override
    public void decay() {
        this.decrease(2); // Happiness decays slightly
    }
}
