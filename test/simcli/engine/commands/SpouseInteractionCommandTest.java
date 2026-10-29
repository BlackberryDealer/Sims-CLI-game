package simcli.engine.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.CommandResult;
import simcli.engine.HomeSupplies;
import simcli.engine.IWorldManager;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.engine.WorldManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.GameRandom;
import simcli.world.Residential;
import simcli.world.interactables.Fridge;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Command Pattern - SpouseInteractionCommand")
public class SpouseInteractionCommandTest {

    private Sim husband;
    private Sim wife;
    private List<Sim> household;
    private IWorldManager world;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        husband = new Sim("Hal", 30, Gender.MALE, Job.UNEMPLOYED);
        wife = new Sim("Wren", 30, Gender.FEMALE, Job.UNEMPLOYED);
        husband.getRelationshipManager().setSpouse(wife);
        wife.getRelationshipManager().setSpouse(husband);
        household = new ArrayList<>(List.of(husband, wife));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    private SpouseInteractionCommand command(String input) {
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(husband)
                .neighborhood(household)
                .scanner(new Scanner(new ByteArrayInputStream(input.getBytes())))
                .timeManager(new TimeManager(1, 24))
                .worldManager(world)
                .currentLocation(world.getCurrentLocation())
                .logger(new SimulationLogger())
                .build();
        return new SpouseInteractionCommand(ctx);
    }

    @Test
    @DisplayName("A dead spouse can't be dated")
    void testDeadSpouseCannotDate() throws Exception {
        wife.markAsDead();
        husband.getHappiness().setValue(50);

        CommandResult result = command("1\n\n").execute();

        assertEquals(CommandResult.NO_TICK, result);
        assertEquals(50, husband.getHappiness().getValue());
    }

    @Test
    @DisplayName("Babies can still be fed after the spouse has died")
    void testFeedBabiesWithDeadSpouse() throws Exception {
        Sim baby = husband.getRelationshipManager().finalizeBaby("Bub", Gender.MALE);
        household.add(baby);
        baby.getHunger().setValue(10);
        HomeSupplies.find(world, Fridge.class).get(0).storeItem(new Food("Apple", 10, 15, 5)); // babies need real food
        wife.markAsDead();

        CommandResult result = command("3\n").execute();

        assertEquals(CommandResult.TICK_FORWARD, result);
        assertEquals(60, baby.getHunger().getValue());
    }

    @Test
    @DisplayName("A failed baby attempt still costs time")
    void testFailedBabyAttemptTicks() throws Exception {
        // Find a seed whose first roll fails the 50% pregnancy check.
        long seed = 0;
        while (new Random(seed).nextInt(100) < 50) seed++;
        GameRandom.RANDOM.setSeed(seed);

        CommandResult result = command("2\n").execute();

        assertEquals(CommandResult.TICK_FORWARD, result, "Retrying must not be free");
        assertEquals(2, household.size(), "No baby on a failed attempt");
    }

    @Test
    @DisplayName("Trying for a baby uses the best owned home, not always the Dorm")
    void testBabyAttemptGoesToBestOwnedHome() throws Exception {
        Residential bungalow = (Residential) world.getCityMap().get(1);
        bungalow.setOwned(true);
        world.setCurrentLocation(world.getCityMap().get(2)); // the supermarket

        long seed = 0;
        while (new Random(seed).nextInt(100) < 50) seed++;
        GameRandom.RANDOM.setSeed(seed);

        command("2\n").execute();

        assertSame(bungalow, world.getCurrentLocation());
        assertTrue(bungalow.getRooms().contains(husband.getCurrentRoom()));
    }
}
