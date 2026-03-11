package simcli.entities;

import simcli.engine.SimulationException;

public class ChildSim extends Sim {

    public ChildSim(String name, int age) {
        super(name, age);
    }

    public ChildSim(String name) {
        this(name, 8); // default starting age for a child
    }

    @Override
    public void performActivity(String activityType) throws SimulationException {
        if (this.state == SimState.DEAD || this.state == SimState.CRITICAL) {
            throw new SimulationException(this.name + " is in critical condition and refuses to act.");
        }

        if (activityType.equalsIgnoreCase("Work")) {
            System.out.println(this.name + " is a child and cannot work! Children should study and play.");
        } else if (activityType.equalsIgnoreCase("Study")) {
            System.out.println(this.name + " sits down to study.");
            this.energy.decrease(15);
            this.hunger.decrease(10);
            this.happiness.increase(5); // A little happy they did their homework
        } else if (activityType.equalsIgnoreCase("Play")) {
            System.out.println(this.name + " goes out to play!");
            this.energy.decrease(25);
            this.hunger.decrease(20);
            this.hygiene.decrease(30);
            this.happiness.increase(20);
        } else {
            System.out.println(this.name + " is idling.");
        }
    }

    @Override
    public void growOlderDaily() {
        super.growOlderDaily();

        // When they hit 18, we let the game know they aged up.
        // Returning them as an AdultSim requires replacing the Sim object in the
        // neighborhood,
        // which implies logic in GameEngine. For now, we print a message.
        if (this.age == 18) {
            System.out.println("\n*** AGED UP! " + this.name + " has turned 18 and is now a young adult! ***");
            // NOTE: In a more complex entity-component system, we would just swap the
            // behaviour.
            // For now, ChildSim becomes a generic adult conceptually, but remains a
            // ChildSim object.
        }
    }
}
