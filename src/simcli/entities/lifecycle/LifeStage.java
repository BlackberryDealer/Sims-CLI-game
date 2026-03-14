package simcli.entities.lifecycle;


public interface LifeStage {


    boolean canWork();


    double getEnergyDecayModifier();


    String getStageName();


    LifeStage getNextStage(int currentAge);
}
