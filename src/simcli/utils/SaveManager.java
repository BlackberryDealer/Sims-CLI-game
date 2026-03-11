package simcli.utils;

import simcli.engine.GameEngine;
import simcli.entities.AdultSim;
import simcli.entities.Job;
import simcli.entities.Sim;

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
            writer.println("GAME_OVER:" + engine.getIsGameOver());
            if (engine.getIsGameOver()) {
                writer.println("STATS_MONEY:" + engine.getSessionTotalMoney());
                writer.println("STATS_ITEMS:" + engine.getSessionTotalItems());
            }

            for (Sim sim : engine.getNeighborhood()) {
                if (sim instanceof AdultSim) {
                    AdultSim aSim = (AdultSim) sim;
                    // Format:
                    // AdultSim:Name,Age,JobName,Money,InventoryCapacity,Hunger,Energy,Happiness
                    writer.println("AdultSim:" + aSim.getName() + "," + aSim.getAge() + "," +
                            aSim.getCareer().name() + "," + aSim.getMoney() + "," + aSim.getInventoryCapacity() + "," +
                            aSim.getHunger().getValue() + "," + aSim.getEnergy().getValue() + ","
                            + aSim.getHappiness().getValue());
                } else if (sim instanceof simcli.entities.ChildSim) {
                    simcli.entities.ChildSim cSim = (simcli.entities.ChildSim) sim;
                    // Format: ChildSim:Name,Age,Money,InventoryCapacity,Hunger,Energy,Happiness
                    writer.println("ChildSim:" + cSim.getName() + "," + cSim.getAge() + "," +
                            cSim.getMoney() + "," + cSim.getInventoryCapacity() + "," +
                            cSim.getHunger().getValue() + "," + cSim.getEnergy().getValue() + ","
                            + cSim.getHappiness().getValue());
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
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
                } else if (line.startsWith("AdultSim:")) {
                    String[] data = line.substring(9).split(",");

                    // Parse Enum Job safely
                    Job loadedJob = Job.valueOf(data[2]);
                    AdultSim sim = new AdultSim(data[0], Integer.parseInt(data[1]), loadedJob);

                    // Load the new economy stats
                    sim.setMoney(Integer.parseInt(data[3]));
                    sim.setInventoryCapacity(Integer.parseInt(data[4]));

                    // Load the needs
                    sim.getHunger().setValue(Integer.parseInt(data[5]));
                    sim.getEnergy().setValue(Integer.parseInt(data[6]));
                    if (data.length > 7) {
                        sim.getHappiness().setValue(Integer.parseInt(data[7]));
                    }
                    sim.updateState();

                    loadedNeighborhood.add(sim);
                } else if (line.startsWith("ChildSim:")) {
                    String[] data = line.substring(9).split(",");

                    simcli.entities.ChildSim sim = new simcli.entities.ChildSim(data[0], Integer.parseInt(data[1]));

                    // Load the new economy stats
                    sim.setMoney(Integer.parseInt(data[2]));
                    sim.setInventoryCapacity(Integer.parseInt(data[3]));

                    // Load the needs
                    sim.getHunger().setValue(Integer.parseInt(data[4]));
                    sim.getEnergy().setValue(Integer.parseInt(data[5]));
                    if (data.length > 6) {
                        sim.getHappiness().setValue(Integer.parseInt(data[6]));
                    }
                    sim.updateState();

                    loadedNeighborhood.add(sim);
                }
            }

            if (isGameOver) {
                System.out.println("\n--- FINAL WORLD STATS ---");
                System.out.println("Total Ticks Survived: " + loadedTick);
                System.out.println("Total Money Earned: $" + statsMoney);
                System.out.println("Total Items Bought: " + statsItems);
                System.out.println("-------------------------");
                System.out.println("This save state is complete. Returning to Main Menu.");
                return null;
            }

            return new GameEngine(loadedWorldName, loadedTick, loadedNeighborhood, isGameOver);

        } catch (Exception e) {
            System.err.println("Error loading game: " + e.getMessage());
            return null;
        }
    }
}