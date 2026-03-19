package simcli.entities.lifecycle;

public class TeenStage implements LifeStage {
    private static final int ADULTHOOD_AGE = 18;

    @Override
    public boolean canWork() {
        return false;
    }

    @Override
    public double getEnergyDecayModifier() {
        return 1.2;
    }

    @Override
    public String getStageName() {
        return "Teen";
    }

    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= ADULTHOOD_AGE) {
            return new AdultStage();
        }
        return this;
    }
}
