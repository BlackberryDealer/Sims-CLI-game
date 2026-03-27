package simcli.persistence;

import simcli.engine.GameEngine;
import simcli.entities.items.Item;
import simcli.entities.models.Job;
import simcli.entities.actors.Sim;
import simcli.entities.actors.NPCSim;
import simcli.entities.models.Gender;

import simcli.entities.models.SimState;
import simcli.ui.UIManager;
import simcli.world.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveManager {
    private static final String SAVE_DIR = "saves/";
    private static final String FILE_FORMAT = ".txt";

    public static void checkDirectory() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists())
            dir.mkdirs();
    }

    public static boolean saveExists(String worldName) {
        return new File(SAVE_DIR + worldName + FILE_FORMAT).exists();
    }

    public static boolean deleteSave(String worldName) {
        File file = new File(SAVE_DIR + worldName + FILE_FORMAT);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static List<String> getExistingSaves() {
        checkDirectory();
        List<String> saves = new ArrayList<>();
        File dir = new File(SAVE_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(FILE_FORMAT));

        if (files != null) {
            for (File f : files) {
                saves.add(f.getName().replace(FILE_FORMAT, ""));
            }
        }
        return saves;
    }

    public static void saveGame(GameEngine engine, String worldName) {
        checkDirectory();
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_DIR + worldName + FILE_FORMAT))) {
            writer.println("WORLD:" + engine.getWorldName());
            writer.println("TICK:" + engine.getCurrentTick());
            writer.println("ACTIVE_PLAYER_NAME:" + engine.getActivePlayer().getName());
            writer.println("GAME_OVER:" + engine.isGameOver());
            if (engine.isGameOver()) {
                writer.println("STATS_MONEY:" + engine.getSessionTotalMoney());
                writer.println("STATS_ITEMS:" + engine.getSessionTotalItems());
            }

            // Save Building States
            List<Building> map = engine.getWorldManager().getCityMap();
            for (int i = 0; i < map.size(); i++) {
                if (map.get(i) instanceof Residential) {
                    Residential res = (Residential) map.get(i);
                    StringBuilder sb = new StringBuilder("ResState:" + i + "," + res.isOwned());
                    for (Room r : res.getRooms()) {
                        sb.append(",").append(r.getMaxCapacity());
                    }
                    writer.println(sb.toString());
                }
            }

            // Save Location
            int locIndex = map.indexOf(engine.getWorldManager().getCurrentLocation());
            int roomIndex = -1;
            Sim activeSim = engine.getActivePlayer();
            if (engine.getWorldManager().getCurrentLocation() instanceof Residential && activeSim.getCurrentRoom() != null) {
                Residential res = (Residential) engine.getWorldManager().getCurrentLocation();
                roomIndex = res.getRooms().indexOf(activeSim.getCurrentRoom());
            }
            writer.println("LOCATION:" + locIndex + "," + roomIndex);

            // Save all household sims
            for (Sim sim : engine.getNeighborhood()) {
                writer.println("Sim:" + sim.getName() + "," + sim.getAge() + "," +
                        sim.getGender().name() + "," + sim.getCareer().name() + "," + 
                        sim.getMoney() + "," + sim.getInventoryCapacity() + "," +
                        sim.getHunger().getValue() + "," + sim.getEnergy().getValue() + ","
                        + sim.getHappiness().getValue() + "," + sim.getHygiene().getValue() + ","
                        + sim.getSocial().getValue() + "," + sim.getJobTier() + ","
                        + sim.isChildSim());
                
                // Save Inventory
                for (Item item : sim.getInventory()) {
                    writer.println("Inventory:" + sim.getName() + "," + item.toSaveString());
                }
            }

            // Save Global NPCs
            for (NPCSim npc : engine.getNpcManager().getActiveNPCs()) {
                writer.println("NPC:" + npc.getName() + "," + npc.getAge() + "," +
                        npc.getGender().name() + "," + npc.getCareer().name() + "," +
                        npc.getHunger().getValue() + "," + npc.getEnergy().getValue() + "," +
                        npc.getHappiness().getValue() + "," + npc.getHygiene().getValue() + "," +
                        npc.getSocial().getValue());
            }

            // Save Relationships
            List<Sim> allTrackedSims = new ArrayList<>(engine.getNeighborhood());
            allTrackedSims.addAll(engine.getNpcManager().getActiveNPCs());

            for (Sim sim : allTrackedSims) {
                Map<Sim, Integer> rels = sim.getRelationshipManager().getRelationships();
                for (Map.Entry<Sim, Integer> entry : rels.entrySet()) {
                    writer.println("Rel:" + sim.getName() + "," + entry.getKey().getName() + "," + entry.getValue());
                }
            }

            // Save Spouse data
            for (Sim sim : allTrackedSims) {
                if (sim.getRelationshipManager().getSpouse() != null) {
                    writer.println("Spouse:" + sim.getName() + "," + sim.getRelationshipManager().getSpouse().getName());
                }
            }

        } catch (IOException e) {
            UIManager.printWarning("Error saving game: " + e.getMessage());
        }
    }

    public static GameEngine loadGame(String worldName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_DIR + worldName + FILE_FORMAT))) {
            String line;
            String loadedWorldName = "Unknown";
            int loadedTick = 1;
            boolean isGameOver = false;
            int statsMoney = 0;
            int statsItems = 0;
            int loadedLocIndex = 0;
            int loadedRoomIndex = -1;
            List<Sim> loadedNeighborhood = new ArrayList<>();
            List<NPCSim> loadedNPCs = new ArrayList<>();
            Map<Integer, String[]> parsedResStates = new HashMap<>();
            List<String[]> relLines = new ArrayList<>();
            List<String[]> spouseLines = new ArrayList<>();
            List<String> inventoryLines = new ArrayList<>();

            String activePlayerName = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("WORLD:")) {
                    loadedWorldName = line.substring(6);
                } else if (line.startsWith("TICK:")) {
                    loadedTick = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("ACTIVE_PLAYER_NAME:")) {
                    activePlayerName = line.substring(19);
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
                } else if (line.startsWith("Rel:")) {
                    relLines.add(line.substring(4).split(","));
                } else if (line.startsWith("Spouse:")) {
                    spouseLines.add(line.substring(7).split(","));
                } else if (line.startsWith("Inventory:")) {
                    inventoryLines.add(line.substring(10));
                } else if (line.startsWith("NPC:")) {
                    String[] data = line.substring(4).split(",");
                    Gender gender = Gender.valueOf(data[2]);
                    Job job = Job.valueOf(data[3]);
                    NPCSim npc = new NPCSim(data[0], Integer.parseInt(data[1]), gender, job);
                    npc.getHunger().setValue(Integer.parseInt(data[4]));
                    npc.getEnergy().setValue(Integer.parseInt(data[5]));
                    npc.getHappiness().setValue(Integer.parseInt(data[6]));
                    npc.getHygiene().setValue(Integer.parseInt(data[7]));
                    npc.getSocial().setValue(Integer.parseInt(data[8]));
                    loadedNPCs.add(npc);
                } else if (line.startsWith("Sim:")) {
                    String[] data = line.substring(4).split(",");
                    Gender loadedGender = Gender.valueOf(data[2]);
                    Job loadedJob = Job.valueOf(data[3]);
                    Sim sim = new Sim(data[0], Integer.parseInt(data[1]), loadedGender, loadedJob);
                    sim.setMoney(Integer.parseInt(data[4]));
                    sim.setInventoryCapacity(Integer.parseInt(data[5]));
                    sim.getHunger().setValue(Integer.parseInt(data[6]));
                    sim.getEnergy().setValue(Integer.parseInt(data[7]));
                    sim.getHappiness().setValue(Integer.parseInt(data[8]));
                    sim.getHygiene().setValue(Integer.parseInt(data[9]));
                    sim.getSocial().setValue(Integer.parseInt(data[10]));
                    sim.setJobTier(Integer.parseInt(data[11]));
                    if (data.length > 12) {
                        sim.setChildSim(Boolean.parseBoolean(data[12]));
                    }
                    sim.updateState();
                    loadedNeighborhood.add(sim);
                }
            }

            if (isGameOver) {
                UIManager.printGameOverStats(loadedTick, statsMoney, statsItems);
                return null;
            }

            List<Sim> allSims = new ArrayList<>(loadedNeighborhood);
            allSims.addAll(loadedNPCs);

            // Load Inventory
            for (String invLine : inventoryLines) {
                int firstComma = invLine.indexOf(',');
                if (firstComma != -1) {
                    String ownerName = invLine.substring(0, firstComma);
                    Sim owner = findSimByName(allSims, ownerName);
                    if (owner != null) {
                        String itemData = invLine.substring(firstComma + 1);
                        Savable item = Savable.fromSaveString(itemData);
                        if (item instanceof Item) {
                            owner.addItem((Item) item);
                        }
                    }
                }
            }

            // Load Relationships
            for (String[] relData : relLines) {
                Sim s1 = findSimByName(allSims, relData[0]);
                Sim s2 = findSimByName(allSims, relData[1]);
                if (s1 != null && s2 != null) {
                    s1.getRelationshipManager().increaseRelationship(s2, Integer.parseInt(relData[2]));
                }
            }

            // Load Spouses
            for (String[] spouseData : spouseLines) {
                Sim s1 = findSimByName(allSims, spouseData[0]);
                Sim s2 = findSimByName(allSims, spouseData[1]);
                if (s1 != null && s2 != null) {
                    s1.getRelationshipManager().setSpouse(s2);
                }
            }

            // Load Children parentage
            for (Sim sim : allSims) {
                if (sim.isChildSim()) {
                    for (Sim parent : loadedNeighborhood) {
                        if (parent.getRelationshipManager().getSpouse() != null && !parent.isChildSim()) {
                            parent.getRelationshipManager().addChild(sim);
                        }
                    }
                }
            }

            GameEngine engine = new GameEngine(loadedWorldName, loadedTick, loadedNeighborhood, isGameOver);
            
            // Restore active player if saved
            if (activePlayerName != null) {
                Sim savedActive = findSimByName(loadedNeighborhood, activePlayerName);
                if (savedActive != null && savedActive.getState() != SimState.DEAD) {
                    engine.setActivePlayer(savedActive);
                }
            }
            
            for (NPCSim npc : loadedNPCs) {
                engine.getNpcManager().addNPC(npc);
            }

            // Post-Load injection
            List<Building> map = engine.getWorldManager().getCityMap();
            for (Map.Entry<Integer, String[]> entry : parsedResStates.entrySet()) {
                Residential res = (Residential) map.get(entry.getKey());
                res.setOwned(Boolean.parseBoolean(entry.getValue()[1]));
            }
            if (loadedLocIndex >= 0 && loadedLocIndex < map.size()) {
                engine.getWorldManager().setCurrentLocation(map.get(loadedLocIndex));
                if (engine.getWorldManager().getCurrentLocation() instanceof Residential && loadedRoomIndex >= 0) {
                    Residential res = (Residential) engine.getWorldManager().getCurrentLocation();
                    loadedNeighborhood.get(0).setCurrentRoom(res.getRooms().get(loadedRoomIndex));
                }
            }

            return engine;
        } catch (Exception e) {
            return null;
        }
    }

    private static Sim findSimByName(List<Sim> list, String name) {
        for (Sim s : list) {
            if (s.getName().equals(name)) return s;
        }
        return null;
    }
}