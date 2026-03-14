package simcli.entities.lifecycle;

/**
 * AdultStage — the concrete State for the adult life stage.
 *
 * <p>Encapsulates all rules that apply once a Sim reaches adulthood:</p>
 * <ul>
 *   <li>Adults <strong>can</strong> work and earn income.</li>
 *   <li>Adults decay energy at the <strong>normal rate</strong> (1.0×).</li>
 *   <li>{@link #getNextStage(int)} currently returns {@code this}, making this a
 *       terminal state. A future {@code ElderStage} can be added by changing only
 *       this one method — no other class needs to change (Open/Closed Principle).</li>
 * </ul>
 *
 * <p><strong>State Pattern note:</strong> When {@link simcli.entities.Sim#ageUp()}
 * receives an {@code AdultStage} from {@link ChildStage#getNextStage(int)}, it
 * replaces the internal reference. The Sim's identity, inventory, money, and
 * relationships all remain perfectly intact — the Sim object never changed.</p>
 */
public class AdultStage implements LifeStage {

    /**
     * Adults are permitted to work and earn income.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean canWork() {
        return true;
    }

    /**
     * Adults decay energy at the baseline rate — no modifier applied.
     *
     * @return {@code 1.0} — the standard decay multiplier.
     */
    @Override
    public double getEnergyDecayModifier() {
        return 1.0;
    }

    /**
     * Human-readable stage label for CLI output.
     *
     * @return {@code "Adult"}.
     */
    @Override
    public String getStageName() {
        return "Adult";
    }

    /**
     * Terminal state — adults do not automatically transition further in the
     * current version of the game.
     *
     * <p><strong>Extension point:</strong> To add an {@code ElderStage} when the
     * Sim turns 65, simply change this method to return {@code new ElderStage()}
     * when {@code currentAge >= 65}. Zero other classes need modification.</p>
     *
     * @param currentAge the Sim's current age (currently unused).
     * @return {@code this} — the Sim stays an adult.
     */
    @Override
    public LifeStage getNextStage(int currentAge) {
        // Adults do not transition further for now.
        // Future extension: return new ElderStage() when currentAge >= 65.
        return this;
    }
}
