package simcli.entities.lifecycle;

/**
 * Terminal life stage for Sims aged 65+. Elders can still work
 * (until forced retirement), have reduced energy decay ({@code 0.8x}),
 * and collect a pension when unemployed. No further stage transitions.
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
