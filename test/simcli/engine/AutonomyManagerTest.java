package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AutonomyManager}: household Sims the player isn't
 * controlling eat, sleep, shower and feed babies on their own.
 */
@DisplayName("AutonomyManager — Idle Sims Look After Themselves")
public class AutonomyManagerTest {

    private Sim active;
    private Sim idle;
    private List<Sim> household;
    private WorldManager world;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        active = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        idle = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        household = new ArrayList<>(List.of(active, idle));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
    }

    /**
     * Plays a turn hour by hour exactly as {@link GameLoop#processTick} does,
     * but without its random events: a household event (a neighbour's pie in
     * the fridge, a flu draining energy) would otherwise make exact results
     * flaky.
     */
    private void playTurn(int hours) {
        AutonomyManager autonomy = new AutonomyManager(world, new SimulationLogger());
        autonomy.startTurn(active, household);
        boolean bedtime = active.getCurrentAction() == ActionState.SLEEPING;
        for (int i = 0; i < hours; i++) {
            autonomy.act(active, household, bedtime);
            for (Sim sim : household) {
                sim.tick();
            }
        }
    }

    private Fridge dormFridge() {
        for (Building b : world.getCityMap()) {
            if (b instanceof Residential) {
                for (Room room : ((Residential) b).getRooms()) {
                    for (Interactable it : room.getInteractables()) {
                        if (it instanceof Fridge) {
                            return (Fridge) it;
                        }
                    }
                }
            }
        }
        throw new AssertionError("The dorm should have a fridge");
    }

    @Test
    @DisplayName("A hungry idle Sim eats the most filling food in the fridge")
    void testHungrySimEats() {
        Fridge fridge = dormFridge();
        Food snack = new Food("Apple", 5, 15, 0);
        Food meal = new Food("Lasagna", 30, 60, 0);
        fridge.storeItem(snack);
        fridge.storeItem(meal);
        idle.getHunger().setValue(20);

        playTurn(1);

        assertFalse(fridge.getStoredItems().contains(meal), "The lasagna should be eaten");
        assertTrue(fridge.getStoredItems().contains(snack), "Only one item is eaten");
        assertTrue(idle.getHunger().getValue() > 60, "Hunger should be restored");
    }

    @Test
    @DisplayName("Idle Sims don't eat when they aren't hungry")
    void testFullSimDoesNotEat() {
        Fridge fridge = dormFridge();
        fridge.storeItem(new Food("Apple", 5, 15, 0));

        playTurn(1);

        assertEquals(1, fridge.getStoredItems().size());
    }

    @Test
    @DisplayName("The active Sim is never moved by autonomy")
    void testActiveSimUntouched() {
        Fridge fridge = dormFridge();
        fridge.storeItem(new Food("Apple", 5, 15, 0));
        active.getHunger().setValue(10);
        active.getEnergy().setValue(10);

        playTurn(1);

        assertEquals(1, fridge.getStoredItems().size());
        assertEquals(ActionState.IDLE, active.getCurrentAction());
    }

    @Test
    @DisplayName("Everyone goes to bed when the active Sim does")
    void testHouseholdSleepsWithActiveSim() {
        idle.getEnergy().setValue(40);
        active.setCurrentAction(ActionState.SLEEPING);

        playTurn(10);

        assertEquals(ActionState.SLEEPING, idle.getCurrentAction());
        assertTrue(idle.getEnergy().getValue() > 90, "A night in bed should restore energy");
    }

    @Test
    @DisplayName("An exhausted idle Sim naps, then gets up once rested")
    void testTiredSimSleepsAndWakes() {
        idle.getEnergy().setValue(10);

        playTurn(1);
        assertEquals(ActionState.SLEEPING, idle.getCurrentAction());

        playTurn(10);
        assertEquals(ActionState.IDLE, idle.getCurrentAction(), "Should be awake again after resting");
        // woke at the threshold, then a couple of awake hours took some off
        assertTrue(idle.getEnergy().getValue() >= GameConstants.AUTONOMY_WAKE_THRESHOLD - 20);
    }

    @Test
    @DisplayName("A Sim with very low hygiene takes a shower")
    void testDirtySimShowers() {
        idle.getHygiene().setValue(10);

        playTurn(1);

        assertTrue(idle.getHygiene().getValue() >= 50);
    }

    @Test
    @DisplayName("An idle Sim feeds a hungry baby")
    void testBabyGetsFed() {
        active.getRelationshipManager().setSpouse(idle);
        idle.getRelationshipManager().setSpouse(active);
        Sim baby = active.getRelationshipManager().finalizeBaby("Bub", Gender.MALE);
        household.add(baby);
        baby.getHunger().setValue(10);
        dormFridge().storeItem(new Food("Apple", 5, 15, 0)); // babies need real food

        playTurn(1);

        assertTrue(baby.getHunger().getValue() >= 50, "The baby should have been fed");
    }

    @Test
    @DisplayName("With no world (older callers), idle Sims still sleep but nothing breaks")
    void testNoWorld() {
        idle.getHunger().setValue(10);
        idle.getEnergy().setValue(10);
        GameLoop loop = new GameLoop(new TimeManager(12, 24), household, new SimulationLogger());

        assertDoesNotThrow(() -> loop.processTick(active, 2));
        assertEquals(ActionState.SLEEPING, idle.getCurrentAction());
    }
}
