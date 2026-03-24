package simcli.entities.lifecycle;


import simcli.utils.GameConstants;

public class ChildStage implements LifeStage {

    @Override
    public boolean canWork() {
        return false;
    }

 
    @Override
    public double getEnergyDecayModifier() {
        return simcli.utils.GameConstants.BONUS_TIMES;
    }

    @Override
    public String getStageName() {
        return "Child";
    }

    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.TEEN_AGE) {
            // Transition triggered — return a new TeenStage instance.
            // Sim.ageUp() will store this reference, and the JVM garbage-collects
            // this ChildStage object automatically once it becomes unreachable.
            return new TeenStage();
        }
        // No transition yet — stay in ChildStage.
        return this;
    }
}
