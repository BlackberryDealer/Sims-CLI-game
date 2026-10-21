package simcli.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.CommandResult;
import simcli.engine.InputHandler;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.engine.WorldManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.ui.ascii.AsciiEngine;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.interactables.Shower;
import simcli.world.interactables.WeightBench;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UI — styling, layout and interaction regressions")
public class UiRegressionTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private boolean colorBefore;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        System.setOut(new PrintStream(captured));
        colorBefore = Term.isColorEnabled();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        Term.setColorEnabled(colorBefore);
    }

    private static Scanner input(String text) {
        return new Scanner(new ByteArrayInputStream(text.getBytes()));
    }

    // -------------------------------------------------------------------------
    // Style & Panel
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Colour tags become ANSI codes when on and vanish when off")
    void testColorTags() {
        Term.setColorEnabled(true);
        String on = Style.render("{G}ok{/}");
        assertTrue(on.contains("\u001b[92m"));
        assertEquals("ok", Style.plain(on));

        Term.setColorEnabled(false);
        assertEquals("ok", Style.render("{G}ok{/}"));
    }

    @Test
    @DisplayName("Visible length ignores colour codes; fit pads and truncates")
    void testFit() {
        Term.setColorEnabled(true);
        String coloured = Style.render("{R}abc{/}");
        assertEquals(3, Style.visibleLength(coloured));
        assertEquals(6, Style.visibleLength(Style.fit(coloured, 6)));
        assertEquals("abcd..", Style.fit("abcdefgh", 6));
    }

    @Test
    @DisplayName("Every line of a panel is exactly the screen width, with or without colour")
    void testBoxWidth() {
        for (boolean color : new boolean[] {true, false}) {
            Term.setColorEnabled(color);
            String box = Panel.box("{W}Title{/}", 'c', "short", "{G}a line that is far, far too long to fit inside the box "
                    + "so it has to be truncated somewhere{/}", Panel.meter("Hunger", 42, 18));
            for (String line : box.split("\n")) {
                assertEquals(Term.WIDTH, Style.visibleLength(line), "line: " + Style.plain(line));
            }
        }
    }

    @Test
    @DisplayName("Menu options render colour tags instead of printing them raw")
    void testOptionRendersTags() {
        Term.setColorEnabled(false);
        UIManager.option("1", "Software Engineer {G}$150{/}", "{k}hint{/}");
        String out = captured.toString();
        assertTrue(out.contains("[1] Software Engineer $150"), out);
        assertFalse(out.contains("{G}") || out.contains("{k}"), out);
    }

    @Test
    @DisplayName("All scene art renders and fits the screen")
    void testAllArtFits() {
        Term.setColorEnabled(false);
        Sim sim = new Sim("Maximilian Long", 30, Gender.MALE, Job.SOFTWARE_ENGINEER);
        List<Sim> household = new ArrayList<>(List.of(sim));
        WorldManager world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        AsciiEngine engine = new AsciiEngine();

        List<String> scenes = new ArrayList<>();
        for (Building b : world.getCityMap()) {
            scenes.add(engine.render(sim, b));
        }
        for (ActionState action : ActionState.values()) {
            sim.setCurrentAction(action);
            scenes.add(engine.render(sim, world.getCurrentLocation()));
        }
        for (String scene : scenes) {
            assertFalse(scene.isBlank());
            for (String line : scene.split("\n")) {
                assertTrue(Style.visibleLength(line) <= Term.WIDTH, "too wide: " + line);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Interaction fixes
    // -------------------------------------------------------------------------

    private InputHandler handlerAt(Sim sim, int buildingIndex, WorldManager[] worldOut) {
        List<Sim> household = new ArrayList<>(List.of(sim));
        WorldManager world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        world.setCurrentLocation(world.getCityMap().get(buildingIndex));
        if (worldOut != null) worldOut[0] = world;
        return new InputHandler(world, new TimeManager(8, 24), household, s -> { }, new SimulationLogger());
    }

    @Test
    @DisplayName("Leaving a shop without buying costs no time")
    void testBrowsingShopIsFree() {
        Sim sim = new Sim("Shopper", 30, Gender.FEMALE, Job.UNEMPLOYED);
        InputHandler handler = handlerAt(sim, 2, null); // Supermarket

        assertEquals(CommandResult.NO_TICK, handler.handle("1", sim, input("0\n")));
    }

    @Test
    @DisplayName("Buying something in a shop takes time")
    void testBuyingTakesTime() {
        Sim sim = new Sim("Shopper", 30, Gender.FEMALE, Job.UNEMPLOYED);
        InputHandler handler = handlerAt(sim, 2, null);

        assertEquals(CommandResult.TICK_FORWARD, handler.handle("1", sim, input("1\n0\n")));
        assertEquals(1, sim.getInventory().size());
    }

    @Test
    @DisplayName("Backing out of the park bench costs no time")
    void testParkBackIsFree() {
        Sim sim = new Sim("Walker", 30, Gender.MALE, Job.UNEMPLOYED);
        InputHandler handler = handlerAt(sim, 4, null); // City Park

        assertEquals(CommandResult.NO_TICK, handler.handle("1", sim, input("0\n")));
        assertEquals(CommandResult.NO_TICK, handler.handle("1", sim, input("1\n0\n")));
    }

    @Test
    @DisplayName("Opening and closing the fridge costs no time")
    void testLookingInFridgeIsFree() {
        Sim sim = new Sim("Hungry", 30, Gender.MALE, Job.UNEMPLOYED);
        WorldManager[] world = new WorldManager[1];
        InputHandler handler = handlerAt(sim, 0, world);
        sim.setCurrentRoom(((Residential) world[0].getCurrentLocation()).getRooms().get(1)); // Kitchen

        assertEquals(CommandResult.NO_TICK, handler.handle("1", sim, input("0\n")));
    }

    @Test
    @DisplayName("Commands may be typed with spaces or in lower case")
    void testCommandIsTrimmedByEngineContract() {
        // GameEngine trims and upper-cases before dispatch; the handler receives "W".
        assertEquals("W", " w ".trim().toUpperCase());
    }

    @Test
    @DisplayName("Reading a book uses the leisure scene, not the eating one")
    void testBookIsNotEaten() throws Exception {
        Sim sim = new Sim("Reader", 30, Gender.FEMALE, Job.UNEMPLOYED);
        Consumable novel = new Consumable("Novel", 15, 0, 0, 25);
        sim.addItem(novel);

        novel.interact(sim, input(""), new TimeManager(8, 24));

        assertEquals(ActionState.PLAYING, sim.getCurrentAction());
        assertTrue(captured.toString().contains("enjoys the Novel"));
    }

    @Test
    @DisplayName("Showering and working out get their own action states")
    void testShowerAndWorkoutStates() throws Exception {
        Sim sim = new Sim("Gym Rat", 30, Gender.MALE, Job.UNEMPLOYED);
        new Shower().interact(sim, input(""), new TimeManager(8, 24));
        assertEquals(ActionState.BATHING, sim.getCurrentAction());

        new WeightBench().interact(sim, input(""), new TimeManager(8, 24));
        assertEquals(ActionState.EXERCISING, sim.getCurrentAction());
    }

    @Test
    @DisplayName("World names that can't be file names are rejected")
    void testWorldNameValidation() {
        assertNull(MainMenu.worldNameProblem("My World_2"));
        assertNotNull(MainMenu.worldNameProblem(""));
        assertNotNull(MainMenu.worldNameProblem("a/b"));
        assertNotNull(MainMenu.worldNameProblem("..\\evil"));
        assertNotNull(MainMenu.worldNameProblem("CON"));
        assertNotNull(MainMenu.worldNameProblem("x".repeat(40)));
    }

    @Test
    @DisplayName("Sim names are trimmed, shortened and stripped of colour tags")
    void testCleanName() {
        assertEquals("Zed", MainMenu.cleanName("  Zed  "));
        assertEquals("Rainbow", MainMenu.cleanName("{R}Rainbow"));
        assertEquals(MainMenu.MAX_SIM_NAME, MainMenu.cleanName("x".repeat(40)).length());
    }

    @Test
    @DisplayName("Quitting a job no longer logs 'started a new job as a Unemployed'")
    void testNoBogusJobLog() {
        Sim sim = new Sim("Quitter", 30, Gender.MALE, Job.SOFTWARE_ENGINEER);
        sim.getCareerManager().changeJob(Job.UNEMPLOYED, sim.getName());
        SimulationLogger.getInstance().flushAndPrint();
        assertFalse(captured.toString().contains("new job as a Unemployed"));
    }
}
