package simcli.entities.actors;

import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.GameRandom;

/**
 * A non-playable character Sim.
 */
public class NPCSim extends Sim {
    public NPCSim(String name, int age) {
        super(name, age, GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE);
    }

    public NPCSim(String name, int age, Gender gender, Job job) {
        super(name, age, gender, job);
    }
}
