package simcli.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.GameEngine;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.actors.Sim;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Save Manager Persistence Tests")
public class SaveManagerTest {

    private final String TEST_WORLD_NAME = "JUnitTestWorld777";

    @BeforeEach
    void setUp() {
        SaveManager.checkDirectory();
        SaveManager.deleteSave(TEST_WORLD_NAME);
    }

    @AfterEach
    void tearDown() {
        SaveManager.deleteSave(TEST_WORLD_NAME);
    }

    @Test
    @DisplayName("saveExists() and deleteSave() operate safely")
    void testSaveExists() {
        assertFalse(SaveManager.saveExists(TEST_WORLD_NAME));
        
        try (PrintWriter writer = new PrintWriter(new FileWriter("saves/" + TEST_WORLD_NAME + ".txt"))) {
            writer.println("WORLD:MOCK");
        } catch (Exception e) {
            fail("Failed to write mock text save.");
        }
        
        assertTrue(SaveManager.saveExists(TEST_WORLD_NAME));
    }

    @Test
    @DisplayName("loadGame() catches Exception internally and returns gracefully on corrupted data")
    void testLoadGameCorruptedData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("saves/" + TEST_WORLD_NAME + ".txt"))) {
            writer.println("WORLD:" + TEST_WORLD_NAME);
            writer.println("TICK:INVALID_INT"); // Triggers NumberFormatException
        } catch (Exception e) {
            fail("Failed to write mock text save.");
        }

        GameEngine loaded = SaveManager.loadGame(TEST_WORLD_NAME);
        assertNull(loaded, "Corrupted save file must return null cleanly, not crash the JVM.");
    }
    
    @Test
    @DisplayName("saveGame() serializes without error")
    void testSaveGameSerialization() {
        Sim testSim = new Sim("TestSaveSim", 25, Gender.FEMALE, Job.SOFTWARE_ENGINEER);
        GameEngine engine = new GameEngine(TEST_WORLD_NAME, 1, Collections.singletonList(testSim), false);
        
        // We ensure no exception bubbles up
        assertDoesNotThrow(() -> {
            SaveManager.saveGame(engine, TEST_WORLD_NAME);
        }, "saveGame should execute thoroughly without throwing an IO Exception");
        
        assertTrue(SaveManager.saveExists(TEST_WORLD_NAME));
    }
}
