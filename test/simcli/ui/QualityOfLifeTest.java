package simcli.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.ActionCancelledException;
import simcli.engine.CommandResult;
import simcli.engine.SimulationLogger;
import simcli.engine.SleepEventException;
import simcli.engine.TimeManager;
import simcli.engine.WorldManager;
import simcli.engine.commands.CommandContext;
import simcli.engine.commands.HelpCommand;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.managers.RelationshipManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.RelationshipStatus;
import simcli.world.interactables.Bed;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the quality-of-life features (help, need summaries, danger
 * warnings, the hungry-bedtime check) and the single relationship store.
 */
@DisplayName("Quality of Life — Help, Summaries and Warnings")
public class QualityOfLifeTest {

    private Sim ann;
    private Sim bob;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        ann = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
    }

    private static Scanner input(String text) {
        return new Scanner(new ByteArrayInputStream(text.getBytes()));
    }

    @Test
    @DisplayName("The need summary lists only what changed, with before and after values")
    void testNeedSummary() {
        ann.getHunger().setValue(72);
        NeedSummary before = NeedSummary.capture(ann);
        ann.getHunger().setValue(49);
        ann.setMoney(ann.getMoney() + 150);

        List<String> changes = before.changesTo(NeedSummary.capture(ann));

        assertEquals(2, changes.size(), "Only hunger and money changed: " + changes);
        String hunger = Style.plain(changes.get(0));
        assertTrue(hunger.contains("Hunger") && hunger.contains("72") && hunger.contains("49"), hunger);
        assertTrue(Style.plain(changes.get(1)).contains("Money"));
    }

    @Test
    @DisplayName("Other household members in danger are flagged; the active Sim and the healthy aren't")
    void testHouseholdAlerts() {
        ann.getRelationshipManager().setSpouse(bob);
        bob.getRelationshipManager().setSpouse(ann);
        Sim baby = ann.getRelationshipManager().finalizeBaby("Bub", Gender.MALE);
        Sim sick = new Sim("Cal", 40, Gender.MALE, Job.UNEMPLOYED);
        List<Sim> household = new ArrayList<>(List.of(ann, bob, baby, sick));

        assertTrue(TerminalRenderer.householdAlerts(household, ann).isEmpty(), "Everyone's fine to start with");

        baby.getHunger().setValue(5);
        sick.setHealth(30);
        ann.setHealth(10); // the active Sim has their own HUD
        List<String> alerts = TerminalRenderer.householdAlerts(household, ann);

        assertEquals(2, alerts.size(), alerts.toString());
        assertTrue(Style.plain(alerts.get(0)).contains("Baby Bub is starving"));
        assertTrue(Style.plain(alerts.get(1)).contains("Cal's health is low"));
    }

    @Test
    @DisplayName("Going to bed very hungry asks first, and 'no' cancels without sleeping")
    void testHungryBedtimeWarning() throws Exception {
        ann.getHunger().setValue(10);
        Bed bed = new Bed();

        assertThrows(ActionCancelledException.class, () -> bed.interact(ann, input("N\n"), new TimeManager(22, 24)));
        assertEquals(10, ann.getHunger().getValue(), "Cancelling must not apply the night's hunger cost");

        assertThrows(SleepEventException.class, () -> bed.interact(ann, input("Y\n"), new TimeManager(22, 24)));
    }

    @Test
    @DisplayName("A well-fed Sim goes to bed without being asked")
    void testNoWarningWhenFed() {
        assertThrows(SleepEventException.class, () -> new Bed().interact(ann, input(""), new TimeManager(22, 24)));
    }

    @Test
    @DisplayName("The help screen opens without taking any time")
    void testHelpScreen() throws Exception {
        List<Sim> household = new ArrayList<>(List.of(ann));
        WorldManager world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(ann)
                .neighborhood(household)
                .scanner(input("\n"))
                .timeManager(new TimeManager(8, 24))
                .worldManager(world)
                .currentLocation(world.getCurrentLocation())
                .logger(new SimulationLogger())
                .build();

        assertEquals(CommandResult.NO_TICK, new HelpCommand(ctx).execute());
    }

    @Test
    @DisplayName("One relationship record holds the score and the status that goes with it")
    void testSingleRelationshipStore() {
        RelationshipManager rm = ann.getRelationshipManager();
        assertEquals(RelationshipStatus.STRANGER, rm.getStatus(bob));

        rm.increaseRelationship(bob, 55);
        assertEquals(55, rm.getRelationship(bob));
        assertEquals(RelationshipStatus.FRIEND, rm.getStatus(bob));
        assertSame(rm.getRelationshipWith(bob), rm.getRelationshipWith(bob));

        rm.interactSocially(bob);
        assertEquals(55 + (ann.hasTrait(simcli.entities.models.Trait.SOCIALITE) ? 15 : 10), rm.getRelationship(bob),
                "interactSocially changes the same score, not a second copy");
    }

    @Test
    @DisplayName("Marriage shows as MARRIED whatever the score, and the score map is read-only")
    void testMarriedStatus() {
        RelationshipManager rm = ann.getRelationshipManager();
        rm.increaseRelationship(bob, 100);
        assertTrue(rm.marry(bob));

        assertEquals(RelationshipStatus.MARRIED, rm.getStatus(bob));
        assertEquals(RelationshipStatus.MARRIED, bob.getRelationshipManager().getStatus(ann));
        rm.increaseRelationship(bob, -60);
        assertEquals(RelationshipStatus.MARRIED, rm.getStatus(bob), "A rough patch doesn't end a marriage");

        assertThrows(UnsupportedOperationException.class, () -> rm.getRelationships().put(bob, 0));
    }
}
