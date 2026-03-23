package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

public class TeenStage implements LifeStage {

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
        if (currentAge >= GameConstants.ADULT_AGE) {
            return new AdultStage();
        }
        return this;
    }
}
