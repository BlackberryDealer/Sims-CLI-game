package simcli.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.GameEngine;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.StorageChest;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Save → load round trips: everything the player built must come back
 * exactly, including Sims with duplicate names or commas in their names.
 */
@DisplayName("SaveManager — Round-trip Persistence")
public class SaveRoundTripTest {

    private static final String WORLD = "JUnitRoundTrip";

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        SaveManager.deleteSave(WORLD);
    }

    @AfterEach
    void tearDown() {
        SaveManager.deleteSave(WORLD);
    }

    @Test
    @DisplayName("Full household state survives a save and reload")
    void testFullRoundTrip() {
        Sim smith = new Sim("Smith, Jr. 100%+", 30, Gender.MALE, Job.SOFTWARE_ENGINEER);
        Sim alexF = new Sim("Alex", 28, Gender.FEMALE, Job.UNEMPLOYED);
        Sim alexM = new Sim("Alex", 40, Gender.MALE, Job.PERSONAL_TRAINER);

        smith.getTraits().clear();
        smith.getTraits().add(Trait.LAZY);
        smith.setTotalMoneyEarned(1234);
        smith.setTotalItemsBought(7);
        smith.getCareerManager().setConsecutiveDaysMissed(2);
        smith.getCareerManager().setShiftsWorkedToday(1);

        smith.getRelationshipManager().setSpouse(alexF);
        alexF.getRelationshipManager().setSpouse(smith);
        Sim kid = smith.getRelationshipManager().finalizeBaby("Kid, The", Gender.FEMALE);

        smith.getRelationshipManager().increaseRelationship(alexM, 40);
        alexF.getRelationshipManager().increaseRelationship(alexM, 70);
        alexM.addItem(new Food("Apple", 10, 15, 5));

        List<Sim> household = new ArrayList<>(List.of(smith, alexF, alexM, kid));
        GameEngine engine = new GameEngine(WORLD, 50, household, false);
        engine.setActivePlayer(alexM);

        List<simcli.world.Building> map = engine.getWorldManager().getCityMap();
        Residential dorm = (Residential) map.get(0);
        Residential bungalow = (Residential) map.get(1);

        Room dormBedroom = dorm.getRooms().get(0);
        dormBedroom.setMaxCapacity(140);
        StorageChest chest = new StorageChest();
        chest.storeItem(new Food("Steak", 40, 50, 20));
        dormBedroom.placeFurniture(chest, 10);
        ((Fridge) dorm.getRooms().get(1).getInteractables().get(0)).storeItem(new Food("Apple", 10, 15, 5));

        bungalow.setOwned(true);
        engine.getWorldManager().setCurrentLocation(bungalow);
        smith.setCurrentRoom(bungalow.getRooms().get(1));
        alexM.setCurrentRoom(bungalow.getRooms().get(0));

        SaveManager.saveGame(engine, WORLD);
        GameEngine loaded = SaveManager.loadGame(WORLD);
        assertNotNull(loaded, "Round-trip save must load");

        List<Sim> l = loaded.getNeighborhood();
        assertEquals(4, l.size());
        Sim lSmith = l.get(0), lAlexF = l.get(1), lAlexM = l.get(2), lKid = l.get(3);

        // Names with commas and URL-special characters
        assertEquals("Smith, Jr. 100%+", lSmith.getName());
        assertEquals("Kid, The", lKid.getName());

        // Traits and counters
        assertEquals(List.of(Trait.LAZY), lSmith.getTraits());
        assertEquals(1234, lSmith.getTotalMoneyEarned());
        assertEquals(7, lSmith.getTotalItemsBought());
        assertEquals(2, lSmith.getCareerManager().getConsecutiveDaysMissed());
        assertEquals(1, lSmith.getShiftsWorkedToday());
        assertEquals(alexM.getTraits(), lAlexM.getTraits());

        // Duplicate names stay distinct
        assertSame(lAlexM, loaded.getActivePlayer(), "The male Alex was active");
        assertEquals(70, lAlexF.getRelationshipManager().getRelationship(lAlexM));
        assertEquals(40, lSmith.getRelationshipManager().getRelationship(lAlexM));
        assertEquals(0, lSmith.getRelationshipManager().getRelationship(lAlexF));
        assertEquals(1, lAlexM.getInventory().size());
        assertEquals(0, lAlexF.getInventory().size());

        // Family
        assertSame(lAlexF, lSmith.getRelationshipManager().getSpouse());
        assertSame(lSmith, lAlexF.getRelationshipManager().getSpouse());
        assertNull(lAlexM.getRelationshipManager().getSpouse());
        assertEquals(List.of(lKid), lSmith.getRelationshipManager().getChildren());
        assertEquals(List.of(lKid), lAlexF.getRelationshipManager().getChildren());
        assertTrue(lAlexM.getRelationshipManager().getChildren().isEmpty());
        assertTrue(lKid.isChildSim());
        assertEquals(0, lKid.getMoney(), "Newborns start without money");

        // Buildings, furniture, containers
        List<simcli.world.Building> lMap = loaded.getWorldManager().getCityMap();
        Residential lDorm = (Residential) lMap.get(0);
        Residential lBungalow = (Residential) lMap.get(1);
        Room lBedroom = lDorm.getRooms().get(0);
        assertEquals(140, lBedroom.getMaxCapacity(), "Room upgrade must persist");
        assertEquals(40, lBedroom.getUsedCapacity(), "Bed (30) + placed chest (10)");

        StorageChest lChest = null;
        for (Interactable it : lBedroom.getInteractables()) {
            if (it instanceof StorageChest) lChest = (StorageChest) it;
        }
        assertNotNull(lChest, "Placed furniture must persist");
        assertEquals(1, lChest.getStoredItems().size());
        assertEquals("Steak", lChest.getStoredItems().get(0).getObjectName());

        Fridge lFridge = (Fridge) lDorm.getRooms().get(1).getInteractables().get(0);
        assertEquals(1, lFridge.getStoredItems().size(), "Fridge contents must persist");

        assertTrue(lBungalow.isOwned());
        assertSame(lBungalow, loaded.getWorldManager().getCurrentLocation());
        assertEquals("Study", lSmith.getCurrentRoom().getName());
        assertEquals("Living Room", lAlexM.getCurrentRoom().getName());
    }

    @Test
    @DisplayName("A Sim who died of old age stays dead after reloading")
    void testOldAgeDeathPersists() {
        Sim elder = new Sim("Elder", GameConstants.DEATH_AGE, Gender.FEMALE, Job.UNEMPLOYED);
        elder.markAsDead();
        Sim heir = new Sim("Heir", 30, Gender.MALE, Job.UNEMPLOYED);
        GameEngine engine = new GameEngine(WORLD, 10, new ArrayList<>(List.of(elder, heir)), false);

        SaveManager.saveGame(engine, WORLD);
        GameEngine loaded = SaveManager.loadGame(WORLD);

        assertNotNull(loaded);
        assertEquals(SimState.DEAD, loaded.getNeighborhood().get(0).getState());
        assertEquals("Heir", loaded.getActivePlayer().getName());
    }

    @Test
    @DisplayName("Park NPCs keep distinct identities after reloading")
    void testNpcsReloaded() {
        Sim sim = new Sim("Solo", 30, Gender.MALE, Job.UNEMPLOYED);
        GameEngine engine = new GameEngine(WORLD, new ArrayList<>(List.of(sim)));
        var npcs = engine.getNpcManager().getActiveNPCs();
        sim.getRelationshipManager().increaseRelationship(npcs.get(3), 55);

        SaveManager.saveGame(engine, WORLD);
        GameEngine loaded = SaveManager.loadGame(WORLD);

        assertNotNull(loaded);
        var lNpcs = loaded.getNpcManager().getActiveNPCs();
        assertEquals(npcs.size(), lNpcs.size());
        assertEquals(55, loaded.getNeighborhood().get(0).getRelationshipManager().getRelationship(lNpcs.get(3)));
        assertEquals(npcs.get(3).getName(), lNpcs.get(3).getName());
    }

    @Test
    @DisplayName("Version 1 saves (name-based, unencoded) still load")
    void testLegacySaveLoads() throws Exception {
        SaveManager.checkDirectory();
        try (PrintWriter w = new PrintWriter(new FileWriter("saves/" + WORLD + ".txt"))) {
            w.println("WORLD:" + WORLD);
            w.println("TICK:30");
            w.println("ACTIVE_PLAYER_NAME:Bea");
            w.println("GAME_OVER:false");
            w.println("ResState:0,true,120,50,60,30");
            w.println("ResState:1,false,120,80,40");
            w.println("LOCATION:0,2");
            w.println("Sim:Ann,30,FEMALE,UNEMPLOYED,500,10,90,90,90,90,90,1,false,100,0,3");
            w.println("Sim:Bea,32,FEMALE,SOFTWARE_ENGINEER,800,10,80,80,80,80,80,2,false,100,0,3");
            w.println("Inventory:Bea,Food,Apple,10,15,5");
            w.println("NPC:Bob,40,MALE,UNEMPLOYED,100,100,100,100,100,100,0,0");
            w.println("Rel:Ann,Bob,25");
        }

        GameEngine loaded = SaveManager.loadGame(WORLD);

        assertNotNull(loaded);
        assertEquals("Bea", loaded.getActivePlayer().getName());
        assertEquals("Garage", loaded.getActivePlayer().getCurrentRoom().getName(),
                "v1 room index belongs to the active player");
        assertEquals(1, loaded.getActivePlayer().getInventory().size());
        assertEquals(120, ((Residential) loaded.getWorldManager().getCityMap().get(0)).getRooms().get(0).getMaxCapacity());
        assertEquals(GameConstants.MAX_NPCS, loaded.getNpcManager().getActiveNPCs().size(),
                "The NPC pool is topped up after loading");
        assertEquals(25, loaded.getNeighborhood().get(0).getRelationshipManager()
                .getRelationship(loaded.getNpcManager().getActiveNPCs().get(0)));
    }

    @Test
    @DisplayName("Generated park NPCs have unique names")
    void testNpcNamesUnique() {
        Sim sim = new Sim("Solo", 30, Gender.MALE, Job.UNEMPLOYED);
        GameEngine engine = new GameEngine(WORLD, new ArrayList<>(List.of(sim)));
        Set<String> names = new HashSet<>();
        engine.getNpcManager().getActiveNPCs().forEach(n -> names.add(n.getName()));
        assertEquals(engine.getNpcManager().getActiveNPCs().size(), names.size());
    }
}
