package simcli.entities.actors;

import simcli.entities.models.*;

public class NPCSim extends Sim {


    public NPCSim(String name, int age) {
        super(name, age, simcli.utils.GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE);

    }

}
