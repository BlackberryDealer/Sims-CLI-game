package simcli.entities.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.CommandResult;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.engine.WorldManager;
import simcli.engine.commands.CommandContext;
import simcli.engine.commands.SpouseInteractionCommand;
import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.items.Item;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SkillType;
import simcli.entities.models.SocialAction;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for flirting, social success chances, Charisma, and gift-giving.
 */
@DisplayName("Social Actions — Flirt, Charisma and Gifts")
public class SocialInteractionTest {

    private Sim ann;
    private Sim bob;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        ann = adult("Ann", Gender.FEMALE);
        bob = adult("Bob", Gender.MALE);
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    /**
     * An adult with the Active trait, which doesn't change social odds or
     * charisma training, in an ordinary mood so the great-mood bonus to
     * skill experience doesn't apply.
     */
    private static Sim adult(String name, Gender gender) {
        Sim sim = new Sim(name, 30, gender, Job.UNEMPLOYED);
        sim.getTraits().clear();
        sim.getTraits().add(Trait.ACTIVE);
        sim.getHunger().setValue(60);
        sim.getHappiness().setValue(40);
        sim.getSocial().setValue(40);
        return sim;
    }

    @Test
    @DisplayName("Flirting needs two adults who already know each other")
    void testFlirtRequirements() {
        RelationshipManager rm = ann.getRelationshipManager();
        assertFalse(rm.canFlirtWith(bob), "Strangers can't flirt");

        rm.increaseRelationship(bob, GameConstants.FLIRT_MIN_RELATIONSHIP);
        assertTrue(rm.canFlirtWith(bob));
        assertFalse(rm.canFlirtWith(ann), "Can't flirt with yourself");

        Sim teen = new Sim("Tia", 15, Gender.MALE, Job.UNEMPLOYED);
        rm.increaseRelationship(teen, 50);
        assertFalse(rm.canFlirtWith(teen), "Adults only");
    }

    @Test
    @DisplayName("Married Sims can only flirt with their spouse")
    void testFlirtWhenMarried() {
        Sim cat = adult("Cat", Gender.FEMALE);
        RelationshipManager rm = ann.getRelationshipManager();
        rm.increaseRelationship(bob, 50);
        rm.increaseRelationship(cat, 50);

        rm.setSpouse(cat);
        cat.getRelationshipManager().setSpouse(ann);

        assertTrue(rm.canFlirtWith(cat));
        assertFalse(rm.canFlirtWith(bob));
    }

    @Test
    @DisplayName("Charisma and a closer relationship raise the odds")
    void testSuccessChance() {
        RelationshipManager rm = ann.getRelationshipManager();
        assertEquals(100, rm.getSuccessChance(bob, SocialAction.CHAT));
        assertEquals(100, rm.getSuccessChance(bob, SocialAction.ARGUE));
        assertEquals(GameConstants.JOKE_BASE_SUCCESS_CHANCE, rm.getSuccessChance(bob, SocialAction.JOKE));

        rm.increaseRelationship(bob, GameConstants.FLIRT_MIN_RELATIONSHIP);
        int flirtAtMinimum = rm.getSuccessChance(bob, SocialAction.FLIRT);
        assertEquals(GameConstants.FLIRT_BASE_SUCCESS_CHANCE, flirtAtMinimum);
        rm.increaseRelationship(bob, 40);
        assertEquals(flirtAtMinimum + 20, rm.getSuccessChance(bob, SocialAction.FLIRT));

        ann.getSkillManager().setExperience(SkillType.CHARISMA, SkillManager.experienceForLevel(5));
        assertEquals(GameConstants.JOKE_BASE_SUCCESS_CHANCE + 5 * GameConstants.CHARISMA_SUCCESS_BONUS_PER_LEVEL,
                rm.getSuccessChance(bob, SocialAction.JOKE));

        ann.getSkillManager().setExperience(SkillType.CHARISMA, 10_000);
        assertEquals(95, rm.getSuccessChance(bob, SocialAction.FLIRT), "Nothing is a sure thing");
    }

    @Test
    @DisplayName("A chat always lands, builds the relationship and trains Charisma")
    void testChatLands() {
        assertTrue(ann.getRelationshipManager().performSocialAction(bob, SocialAction.CHAT));
        assertEquals(SocialAction.CHAT.getRelationshipChange(), ann.getRelationshipManager().getRelationship(bob));
        assertEquals(GameConstants.SOCIAL_CHARISMA_XP, ann.getSkillManager().getExperience(SkillType.CHARISMA));
    }

    @Test
    @DisplayName("Charisma makes friendly actions build relationships faster")
    void testCharismaBoostsGains() {
        Sim charmer = adult("Cal", Gender.MALE);
        charmer.getSkillManager().setExperience(SkillType.CHARISMA, SkillManager.experienceForLevel(10));
        charmer.getRelationshipManager().performSocialAction(bob, SocialAction.CHAT);
        ann.getRelationshipManager().performSocialAction(bob, SocialAction.CHAT);

        assertTrue(charmer.getRelationshipManager().getRelationship(bob)
                > ann.getRelationshipManager().getRelationship(bob));
    }

    @Test
    @DisplayName("Jokes sometimes fall flat, costing relationship and happiness")
    void testJokesCanFail() {
        GameRandom.RANDOM.setSeed(42);
        int successes = 0;
        int failures = 0;
        for (int i = 0; i < 100; i++) {
            Sim listener = adult("L" + i, Gender.MALE);
            ann.getRelationshipManager().increaseRelationship(listener, 30);
            ann.getHappiness().setValue(50);
            boolean landed = ann.getRelationshipManager().performSocialAction(listener, SocialAction.JOKE);
            int rel = ann.getRelationshipManager().getRelationship(listener);
            if (landed) {
                successes++;
                assertTrue(rel > 30, "A joke that lands improves the relationship");
            } else {
                failures++;
                assertEquals(30 - GameConstants.SOCIAL_FAIL_RELATIONSHIP_PENALTY, rel);
                assertEquals(50 - GameConstants.SOCIAL_FAIL_HAPPINESS_PENALTY, ann.getHappiness().getValue());
            }
        }
        assertTrue(successes > 0 && failures > 0, "Over 100 jokes some should land and some fall flat");
    }

    @Test
    @DisplayName("Arguing never pushes happiness below 0")
    void testArgueClampsHappiness() {
        ann.getHappiness().setValue(3);
        ann.getRelationshipManager().performSocialAction(bob, SocialAction.ARGUE);
        assertEquals(0, ann.getHappiness().getValue());
        assertEquals(0, ann.getRelationshipManager().getRelationship(bob));
    }

    @Test
    @DisplayName("A gift moves to the receiver and raises the relationship both ways")
    void testGiveGift() throws SimulationException {
        Item novel = new Consumable("Novel", 15, 0, 0, 25);
        ann.addItem(novel);
        bob.getHappiness().setValue(50);

        int gained = ann.getRelationshipManager().giveGift(bob, novel);

        assertEquals(GameConstants.GIFT_BASE_RELATIONSHIP + 1, gained);
        assertFalse(ann.getInventory().contains(novel));
        assertTrue(bob.getInventory().contains(novel));
        assertEquals(gained, ann.getRelationshipManager().getRelationship(bob));
        assertEquals(gained, bob.getRelationshipManager().getRelationship(ann));
        assertEquals(50 + GameConstants.GIFT_HAPPINESS, bob.getHappiness().getValue());
    }

    @Test
    @DisplayName("Expensive gifts are capped, and you can't give what you don't have")
    void testGiftCapAndOwnership() throws SimulationException {
        Item computer = new Consumable("Gold Watch", 5000, 0, 0, 10);
        ann.addItem(computer);
        assertEquals(GameConstants.GIFT_MAX_RELATIONSHIP, ann.getRelationshipManager().giveGift(bob, computer));

        assertThrows(SimulationException.class,
                () -> ann.getRelationshipManager().giveGift(bob, new Consumable("Ghost", 1, 0, 0, 0)));
    }

    @Test
    @DisplayName("The spouse menu's gift option gives the chosen item")
    void testSpouseGiftMenu() throws Exception {
        ann.getRelationshipManager().setSpouse(bob);
        bob.getRelationshipManager().setSpouse(ann);
        Item comic = new Consumable("Comic Book", 8, 0, 0, 15);
        ann.addItem(comic);
        List<Sim> household = new ArrayList<>(List.of(ann, bob));
        WorldManager world = new WorldManager(new NPCManager(), household);
        world.setupWorld();

        CommandContext ctx = new CommandContext.Builder()
                .activePlayer(ann)
                .neighborhood(household)
                .scanner(new Scanner(new ByteArrayInputStream("4\n1\n".getBytes())))
                .timeManager(new TimeManager(10, 24))
                .worldManager(world)
                .currentLocation(world.getCurrentLocation())
                .logger(new SimulationLogger())
                .build();

        CommandResult result = new SpouseInteractionCommand(ctx).execute();

        assertEquals(CommandResult.TICK_FORWARD, result);
        assertTrue(bob.getInventory().contains(comic));
        assertTrue(ann.getRelationshipManager().getRelationship(bob) > 0);
    }
}
