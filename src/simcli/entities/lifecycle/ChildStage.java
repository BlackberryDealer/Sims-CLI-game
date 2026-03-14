package simcli.entities.lifecycle;


public class ChildStage implements LifeStage {

    private static final int ADULTHOOD_AGE = 18;

    @Override
    public boolean canWork() {
        return false;
    }

 
    @Override
    public double getEnergyDecayModifier() {
        return 1.5;
    }

    @Override
    public String getStageName() {
        return "Child";
    }

    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= ADULTHOOD_AGE) {
            // Transition triggered — return a new AdultStage instance.
            // Sim.ageUp() will store this reference, and the JVM garbage-collects
            // this ChildStage object automatically once it becomes unreachable.
            return new AdultStage();
        }
        // No transition yet — stay in ChildStage.
        return this;
    }
}
