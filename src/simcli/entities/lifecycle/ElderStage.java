package simcli.entities.lifecycle;

/**
 * Terminal life stage for Sims aged 65+. Elders can still work
 * (until forced retirement), have reduced energy decay ({@code 0.8x}),
 * and collect a pension when unemployed. No further stage transitions.
 */
public class ElderStage implements LifeStage {

    /**
     * {@inheritDoc}
     *
     * @return {@code true} — elders can work until forced retirement.
     */
    @Override
    public boolean canWork() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 0.8} — reduced energy decay for elders.
     */
    @Override
    public double getEnergyDecayModifier() {
        return 0.8;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "Elder"}.
     */
    @Override
    public String getStageName() {
        return "Elder";
    }

    /**
     * {@inheritDoc}
     * Elder is the terminal stage — always returns {@code this}.
     */
    @Override
    public LifeStage getNextStage(int currentAge) {
        return this;
    }
}
