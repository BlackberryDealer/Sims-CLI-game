package simcli.entities.lifecycle;


/**
 * Represents the LifeStage entity or state in the simulation.
 */
public interface LifeStage {


    boolean canWork();


    double getEnergyDecayModifier();


    String getStageName();


    LifeStage getNextStage(int currentAge);
}
