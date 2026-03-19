package simcli.entities;

public class NPCSim extends Sim {


    public NPCSim(String name, int age) {
        super(name, age, simcli.utils.GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE);

    }

}
