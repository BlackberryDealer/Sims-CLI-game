package simcli.entities.lifecycle;


import simcli.utils.GameConstants;

public class AdultStage implements LifeStage {

    @Override
    public boolean canWork() {
        return true;
    }

 
    @Override
    public double getEnergyDecayModifier() {
        return 1.0;
    }


    @Override
    public String getStageName() {
        return "Adult";
    }


    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.ELDER_AGE) {
            return new ElderStage();
        }
        return this;
    }
}
