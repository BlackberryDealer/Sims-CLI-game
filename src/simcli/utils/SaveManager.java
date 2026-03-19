package simcli.utils;

import simcli.engine.GameEngine;
import simcli.entities.Job;
import simcli.entities.Sim;
import simcli.entities.Gender;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static final String SAVE_DIR = "saves/";

    // Ensures the saves directory exists
    public static void checkDirectory() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists())
            dir.mkdirs();
    }

    // Checks if a world name is already taken (MainMenu needs this!)
    public static boolean saveExists(String worldName) {
        return new File(SAVE_DIR + worldName + ".txt").exists();
    }

    // Deletes a specific save
    public static boolean deleteSave(String worldName) {
        File file = new File(SAVE_DIR + worldName + ".txt");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    // Gets a list of all current save files (MainMenu needs this!)
    public static List<String> getExistingSaves() {
        checkDirectory();
        List<String> saves = new ArrayList<>();
        File dir = new File(SAVE_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));

        if (files != null) {
            for (File f : files) {
                saves.add(f.getName().replace(".txt", ""));
            }
        }
        return saves;
    }

    // Saves the game engine state to a text file
    public static void saveGame(GameEngine engine, String worldName) {
        checkDirectory();
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_DIR + worldName + ".txt"))) {
            writer.println("WORLD:" + engine.getWorldName());
            writer.println("TICK:" + engine.getCurrentTick());
            writer.println("GAME_OVER:" + engine.isGameOver());
            if (engine.isGameOver()) {
                writer.println("STATS_MONEY:" + engine.getSessionTotalMoney());
                writer.println("STATS_ITEMS:" + engine.getSessionTotalItems());
            }

            for (Sim sim : engine.getNeighborhood()) {
                // Format: Sim:Name,Age,Gender,JobName,Money,InventoryCapacity,Hunger,Energy,Fun,Hygiene,Social
                writer.println("Sim:" + sim.getName() + "," + sim.getAge() + "," +
                        sim.getGender().name() + "," + sim.getCareer().name() + "," + 
                        sim.getMoney() + "," + sim.getInventoryCapacity() + "," +
                        sim.getHunger().getValue() + "," + sim.getEnergy().getValue() + ","
                        + sim.getFun().getValue() + "," + sim.getHygiene().getValue() + ","
                        + sim.getSocial().getValue());
            }
        } catch (IOException e) {
            simcli.ui.UIManager.printWarning("Error saving game: " + e.getMessage());
        }
    }

    // Loads a game engine state from a text file
    public static GameEngine loadGame(String worldName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_DIR + worldName + ".txt"))) {
            String line;
            String loadedWorldName = "Unknown";
            int loadedTick = 1;
            boolean isGameOver = false;
            int statsMoney = 0;
            int statsItems = 0;
            List<Sim> loadedNeighborhood = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("WORLD:")) {
                    loadedWorldName = line.substring(6);
                } else if (line.startsWith("TICK:")) {
                    loadedTick = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("GAME_OVER:")) {
                    isGameOver = Boolean.parseBoolean(line.substring(10));
                } else if (line.startsWith("STATS_MONEY:")) {
                    statsMoney = Integer.parseInt(line.substring(12));
                } else if (line.startsWith("STATS_ITEMS:")) {
                    statsItems = Integer.parseInt(line.substring(12));
                } else if (line.startsWith("Sim:")) {
                    String[] data = line.substring(4).split(",");

                    Gender loadedGender = Gender.valueOf(data[2]);
                    Job loadedJob = Job.valueOf(data[3]);
                    Sim sim = new Sim(data[0], Integer.parseInt(data[1]), loadedGender, loadedJob);

                    // Load the new economy stats
                    sim.setMoney(Integer.parseInt(data[4]));
                    sim.setInventoryCapacity(Integer.parseInt(data[5]));

                    // Load the needs
                    sim.getHunger().setValue(Integer.parseInt(data[6]));
                    sim.getEnergy().setValue(Integer.parseInt(data[7]));
                    if (data.length > 8) {
                        sim.getFun().setValue(Integer.parseInt(data[8]));
                    }
                    if (data.length > 9) {
                        sim.getHygiene().setValue(Integer.parseInt(data[9]));
                    }
                    if (data.length > 10) {
                        sim.getSocial().setValue(Integer.parseInt(data[10]));
                    }
                    sim.updateState();

                    loadedNeighborhood.add(sim);
                }
            }

            if (isGameOver) {
                simcli.ui.UIManager.printGameOverStats(loadedTick, statsMoney, statsItems);
                simcli.ui.UIManager.printMessage("This save state is complete. Returning to Main Menu.");
                return null;
            }

            return new GameEngine(loadedWorldName, loadedTick, loadedNeighborhood, isGameOver);

        } catch (Exception e) {
            simcli.ui.UIManager.printWarning("Error loading game: " + e.getMessage());
            return null;
        }
    }
}