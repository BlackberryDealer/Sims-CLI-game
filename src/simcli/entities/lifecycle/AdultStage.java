package simcli.entities.lifecycle;


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
        return this;
    }
}
