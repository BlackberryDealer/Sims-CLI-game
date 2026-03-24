package simcli.persistence;

import simcli.engine.GameEngine;
import simcli.entities.actors.Job;
import simcli.entities.actors.Sim;
import simcli.entities.actors.Gender;

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

            // Save Building States
            List<simcli.world.Building> map = engine.getWorldManager().getCityMap();
            for (int i = 0; i < map.size(); i++) {
                if (map.get(i) instanceof simcli.world.Residential) {
                    simcli.world.Residential res = (simcli.world.Residential) map.get(i);
                    StringBuilder sb = new StringBuilder("ResState:" + i + "," + res.isOwned());
                    for (simcli.world.Room r : res.getRooms()) {
                        sb.append(",").append(r.getMaxCapacity());
                    }
                    writer.println(sb.toString());
                }
            }

            // Save Location
            int locIndex = map.indexOf(engine.getWorldManager().getCurrentLocation());
            int roomIndex = -1;
            Sim activeSim = engine.getNeighborhood().get(0);
            if (engine.getWorldManager().getCurrentLocation() instanceof simcli.world.Residential && activeSim.getCurrentRoom() != null) {
                simcli.world.Residential res = (simcli.world.Residential) engine.getWorldManager().getCurrentLocation();
                roomIndex = res.getRooms().indexOf(activeSim.getCurrentRoom());
            }
            writer.println("LOCATION:" + locIndex + "," + roomIndex);

            for (Sim sim : engine.getNeighborhood()) {
                // Format: Sim:Name,Age,Gender,JobName,Money,InventoryCapacity,Hunger,Energy,Fun,Hygiene,Social,JobTier
                writer.println("Sim:" + sim.getName() + "," + sim.getAge() + "," +
                        sim.getGender().name() + "," + sim.getCareer().name() + "," + 
                        sim.getMoney() + "," + sim.getInventoryCapacity() + "," +
                        sim.getHunger().getValue() + "," + sim.getEnergy().getValue() + ","
                        + sim.getFun().getValue() + "," + sim.getHygiene().getValue() + ","
                        + sim.getSocial().getValue() + "," + sim.getJobTier());
                
                // Save Inventory
                for (simcli.entities.items.Item item : sim.getInventory()) {
                    if (item instanceof simcli.entities.items.Furniture) {
                        simcli.entities.items.Furniture f = (simcli.entities.items.Furniture) item;
                        writer.println("Inventory:" + sim.getName() + ",Furniture," + f.getObjectName() + "," + f.getPrice() + "," + f.getSpaceScore());
                    } else if (item instanceof simcli.entities.items.Food) {
                        simcli.entities.items.Food f = (simcli.entities.items.Food) item;
                        writer.println("Inventory:" + sim.getName() + ",Food," + f.getObjectName() + "," + f.getPrice() + "," + f.getSatiationValue() + "," + f.getEnergyValue());
                    } else if (item instanceof simcli.entities.items.Consumable) {
                        simcli.entities.items.Consumable c = (simcli.entities.items.Consumable) item;
                        writer.println("Inventory:" + sim.getName() + ",Consumable," + c.getObjectName() + "," + c.getPrice() + "," + c.getSatiationValue() + "," + c.getEnergyValue() + "," + c.getFunValue());
                    }
                }
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
            int loadedLocIndex = 0;
            int loadedRoomIndex = -1;
            List<Sim> loadedNeighborhood = new ArrayList<>();
            java.util.Map<Integer, String[]> parsedResStates = new java.util.HashMap<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("WORLD:")) {
                    loadedWorldName = line.substring(6);
                } else if (line.startsWith("TICK:")) {
                    loadedTick = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("GAME_OVER:")) {
                    isGameOver = Boolean.parseBoolean(line.substring(10));
                } else if (line.startsWith("LOCATION:")) {
                    String[] locData = line.substring(9).split(",");
                    loadedLocIndex = Integer.parseInt(locData[0]);
                    loadedRoomIndex = Integer.parseInt(locData[1]);
                } else if (line.startsWith("ResState:")) {
                    String[] resData = line.substring(9).split(",");
                    parsedResStates.put(Integer.parseInt(resData[0]), resData);
                } else if (line.startsWith("STATS_MONEY:")) {
                    statsMoney = Integer.parseInt(line.substring(12));
                } else if (line.startsWith("STATS_ITEMS:")) {
                    statsItems = Integer.parseInt(line.substring(12));
                } else if (line.startsWith("Inventory:")) {
                    String[] invData = line.substring(10).split(",");
                    String ownerName = invData[0];
                    String type = invData[1];
                    String itemName = invData[2];
                    int price = Integer.parseInt(invData[3]);
                    
                    for (Sim s : loadedNeighborhood) {
                        if (s.getName().equals(ownerName)) {
                            if (type.equals("Furniture")) {
                                int space = Integer.parseInt(invData[4]);
                                s.addItem(new simcli.entities.items.Furniture(itemName, price, space));
                            } else if (type.equals("Food")) {
                                int sat = Integer.parseInt(invData[4]);
                                int eng = Integer.parseInt(invData[5]);
                                s.addItem(new simcli.entities.items.Food(itemName, price, sat, eng));
                            } else if (type.equals("Consumable")) {
                                int sat = Integer.parseInt(invData[4]);
                                int eng = Integer.parseInt(invData[5]);
                                int fun = Integer.parseInt(invData[6]);
                                s.addItem(new simcli.entities.items.Consumable(itemName, price, sat, eng, fun));
                            }
                            break;
                        }
                    }
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
                    if (data.length > 11) {
                        sim.setJobTier(Integer.parseInt(data[11]));
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

            GameEngine engine = new GameEngine(loadedWorldName, loadedTick, loadedNeighborhood, isGameOver);
            
            // Post-Load: Inject World States
            List<simcli.world.Building> map = engine.getWorldManager().getCityMap();
            for (java.util.Map.Entry<Integer, String[]> entry : parsedResStates.entrySet()) {
                int bIndex = entry.getKey();
                String[] bData = entry.getValue();
                if (bIndex >= 0 && bIndex < map.size() && map.get(bIndex) instanceof simcli.world.Residential) {
                    simcli.world.Residential res = (simcli.world.Residential) map.get(bIndex);
                    res.setOwned(Boolean.parseBoolean(bData[1]));
                    for (int i = 0; i < res.getRooms().size() && (i + 2) < bData.length; i++) {
                        res.getRooms().get(i).setMaxCapacity(Integer.parseInt(bData[i + 2]));
                    }
                }
            }
            
            // Post-Load: Inject Location
            if (loadedLocIndex >= 0 && loadedLocIndex < map.size()) {
                simcli.world.Building curLoc = map.get(loadedLocIndex);
                engine.getWorldManager().setCurrentLocation(curLoc);
                
                Sim active = loadedNeighborhood.get(0);
                if (curLoc instanceof simcli.world.Residential && loadedRoomIndex >= 0) {
                    simcli.world.Residential res = (simcli.world.Residential) curLoc;
                    if (loadedRoomIndex < res.getRooms().size()) {
                        active.setCurrentRoom(res.getRooms().get(loadedRoomIndex));
                    }
                }
            }

            return engine;

        } catch (Exception e) {
            simcli.ui.UIManager.printWarning("Error loading game: " + e.getMessage());
            return null;
        }
    }
}