package simcli.entities.lifecycle;

/**
 * Represents the ElderStage entity or state in the simulation.
 */
public class ElderStage implements LifeStage {

    @Override
    public boolean canWork() {
        return true;
    }

    @Override
    public double getEnergyDecayModifier() {
        return 0.8;
    }

    @Override
    public String getStageName() {
        return "Elder";
    }

    @Override
    public LifeStage getNextStage(int currentAge) {
        return this;
    }
}
