package simcli.persistence;

import simcli.engine.GameEngine;
import simcli.entities.items.Item;
import simcli.entities.models.Job;
import simcli.entities.models.Trait;
import simcli.entities.actors.Sim;
import simcli.entities.actors.NPCSim;
import simcli.entities.models.Gender;

import simcli.ui.UIManager;
import simcli.utils.GameConstants;
import simcli.world.*;
import simcli.world.interactables.Container;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.InteractableFactory;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the persistence of game state, including saving worlds, loading worlds,
 * and deleting existing game saves.
 *
 * <h2>Save format (version 2)</h2>
 * <p>A line-based text file. Free-text fields (world and Sim names) are
 * URL-encoded so commas in names can't break the comma-separated fields.
 * Sims are referenced by position rather than by name — {@code H<i>} is the
 * i-th household Sim and {@code N<j>} the j-th park NPC — so Sims that share
 * a name are never mixed up.</p>
 *
 * <p>Version 1 saves (no {@code VERSION:} line, name-based references) can
 * still be loaded.</p>
 */
public class SaveManager {
    /** Directory where save files are stored. */
    private static final String SAVE_DIR = "saves/";
    /** File extension for save files. */
    private static final String FILE_FORMAT = ".txt";
    /** Current save format version. */
    private static final int SAVE_VERSION = 2;

    /**
     * Checks if the save directory exists and creates it if it does not.
     */
    public static void checkDirectory() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists())
            dir.mkdirs();
    }

    /**
     * Checks if a save file exists for a given world name.
     * @param worldName the name of the world to check.
     * @return true if the save file exists, false otherwise.
     */
    public static boolean saveExists(String worldName) {
        return new File(SAVE_DIR + worldName + FILE_FORMAT).exists();
    }

    /**
     * Deletes the save file associated with a given world name.
     * @param worldName the name of the world whose save should be deleted.
     * @return true if the file was deleted successfully, false otherwise.
     */
    public static boolean deleteSave(String worldName) {
        File file = new File(SAVE_DIR + worldName + FILE_FORMAT);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * Retrieves a list of all existing save names (without file extensions).
     * @return a list of strings representing the names of the existing worlds.
     */
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

    // -------------------------------------------------------------------------
    // Saving
    // -------------------------------------------------------------------------

    /**
     * Saves the current game state to a file.
     * This includes world stats, actor states, inventory items, furniture,
     * container contents, relationships, and family links.
     *
     * <p>The file is written to a temporary file first and then moved over
     * the old save, so a crash mid-write can't corrupt an existing save.</p>
     *
     * @param engine the current GameEngine instance containing the world and sims.
     * @param worldName the name of the world/file to save under.
     */
    public static void saveGame(GameEngine engine, String worldName) {
        checkDirectory();
        File target = new File(SAVE_DIR + worldName + FILE_FORMAT);
        File temp = new File(SAVE_DIR + worldName + FILE_FORMAT + ".tmp");

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)))) {
            writeSave(engine, writer);
            if (writer.checkError()) {
                throw new IOException("write failed");
            }
        } catch (IOException e) {
            UIManager.printWarning("Error saving game: " + e.getMessage());
            temp.delete();
            return;
        }

        try {
            try {
                Files.move(temp.toPath(), target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            UIManager.printWarning("Error saving game: " + e.getMessage());
        }
    }

    private static void writeSave(GameEngine engine, PrintWriter writer) {
        List<Sim> household = engine.getNeighborhood();
        List<NPCSim> npcs = engine.getNpcManager().getActiveNPCs();

        Map<Sim, String> ids = new IdentityHashMap<>();
        for (int i = 0; i < household.size(); i++) {
            ids.put(household.get(i), "H" + i);
        }
        for (int j = 0; j < npcs.size(); j++) {
            ids.putIfAbsent(npcs.get(j), "N" + j);
        }

        writer.println("VERSION:" + SAVE_VERSION);
        writer.println("WORLD:" + encode(engine.getWorldName()));
        writer.println("TICK:" + engine.getCurrentTick());
        writer.println("ACTIVE_PLAYER:" + ids.get(engine.getActivePlayer()));
        writer.println("GAME_OVER:" + engine.isGameOver());
        if (engine.isGameOver()) {
            writer.println("STATS_MONEY:" + engine.getSessionTotalMoney());
            writer.println("STATS_ITEMS:" + engine.getSessionTotalItems());
        }

        // Building states: ownership, room capacities, furniture, container contents
        List<Building> map = engine.getWorldManager().getCityMap();
        for (int i = 0; i < map.size(); i++) {
            if (!(map.get(i) instanceof Residential)) {
                continue;
            }
            Residential res = (Residential) map.get(i);
            StringBuilder sb = new StringBuilder("ResState:" + i + "," + res.isOwned());
            for (Room r : res.getRooms()) {
                sb.append(",").append(r.getMaxCapacity());
            }
            writer.println(sb.toString());

            List<Room> rooms = res.getRooms();
            for (int ri = 0; ri < rooms.size(); ri++) {
                Room room = rooms.get(ri);
                List<Interactable> furniture = room.getInteractables();
                for (int k = 0; k < furniture.size(); k++) {
                    Interactable it = furniture.get(k);
                    writer.println("Furn:" + i + "," + ri + "," + encode(it.getObjectName())
                            + "," + room.getSpaceScore(k));
                    if (it instanceof Container) {
                        for (Item stored : ((Container) it).getStoredItems()) {
                            writer.println("Stored:" + i + "," + ri + "," + k + "," + stored.toSaveString());
                        }
                    }
                }
            }
        }

        // Location (room indices are stored per Sim below)
        Building location = engine.getWorldManager().getCurrentLocation();
        writer.println("LOCATION:" + map.indexOf(location) + ",-1");

        // Household Sims
        for (Sim sim : household) {
            int roomIndex = -1;
            if (location instanceof Residential && sim.getCurrentRoom() != null) {
                roomIndex = ((Residential) location).getRooms().indexOf(sim.getCurrentRoom());
            }
            writer.println("Sim:" + encode(sim.getName()) + "," + sim.getAge() + ","
                    + sim.getGender().name() + "," + sim.getCareer().name() + ","
                    + sim.getMoney() + "," + sim.getInventoryCapacity() + ","
                    + sim.getHunger().getValue() + "," + sim.getEnergy().getValue() + ","
                    + sim.getHappiness().getValue() + "," + sim.getHygiene().getValue() + ","
                    + sim.getSocial().getValue() + "," + sim.getJobTier() + ","
                    + sim.isChildSim() + "," + sim.getHealth() + "," + sim.getStarvingTicks() + ","
                    + sim.getDaysAlive() + "," + encodeTraits(sim) + ","
                    + sim.getTotalMoneyEarned() + "," + sim.getTotalItemsBought() + ","
                    + sim.getCareerManager().getConsecutiveDaysMissed() + ","
                    + sim.getShiftsWorkedToday() + "," + sim.hasWarnedAboutOverwork() + ","
                    + roomIndex);

            for (Item item : sim.getInventory()) {
                writer.println("Inventory:" + ids.get(sim) + "," + item.toSaveString());
            }
        }

        // Park NPCs
        for (NPCSim npc : npcs) {
            writer.println("NPC:" + encode(npc.getName()) + "," + npc.getAge() + ","
                    + npc.getGender().name() + "," + npc.getCareer().name() + ","
                    + npc.getHunger().getValue() + "," + npc.getEnergy().getValue() + ","
                    + npc.getHappiness().getValue() + "," + npc.getHygiene().getValue() + ","
                    + npc.getSocial().getValue() + "," + npc.getHealth() + "," + npc.getStarvingTicks() + ","
                    + npc.getDaysAlive() + "," + encodeTraits(npc));
        }

        // Relationships, spouses and children — by id, never by name
        List<Sim> allTrackedSims = new ArrayList<>(household);
        allTrackedSims.addAll(npcs);

        for (Sim sim : allTrackedSims) {
            for (Map.Entry<Sim, Integer> entry : sim.getRelationshipManager().getRelationships().entrySet()) {
                if (ids.containsKey(entry.getKey())) {
                    writer.println("Rel:" + ids.get(sim) + "," + ids.get(entry.getKey()) + "," + entry.getValue());
                }
            }
        }
        for (Sim sim : allTrackedSims) {
            Sim spouse = sim.getRelationshipManager().getSpouse();
            if (spouse != null && ids.containsKey(spouse)) {
                writer.println("Spouse:" + ids.get(sim) + "," + ids.get(spouse));
            }
        }
        for (Sim sim : allTrackedSims) {
            for (Sim child : sim.getRelationshipManager().getChildren()) {
                if (ids.containsKey(child)) {
                    writer.println("Child:" + ids.get(sim) + "," + ids.get(child));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------------------

    /**
     * Loads a game from a save file.
     *
     * <p>If the save records a finished game, its final stats are printed and
     * {@code null} is returned. If the file can't be read or parsed, a warning
     * explaining why is printed and {@code null} is returned.</p>
     *
     * @param worldName the name of the world to load.
     * @return the restored engine, or {@code null} if the game can't be resumed.
     */
    public static GameEngine loadGame(String worldName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(SAVE_DIR + worldName + FILE_FORMAT), StandardCharsets.UTF_8))) {
            String line;
            int version = 1;
            String loadedWorldName = "Unknown";
            int loadedTick = 1;
            boolean isGameOver = false;
            int statsMoney = 0;
            int statsItems = 0;
            int loadedLocIndex = 0;
            int loadedRoomIndex = -1;
            String activePlayerRef = null;

            List<String[]> simLines = new ArrayList<>();
            List<String[]> npcLines = new ArrayList<>();
            Map<Integer, String[]> parsedResStates = new HashMap<>();
            List<String[]> furnLines = new ArrayList<>();
            List<String> storedLines = new ArrayList<>();
            List<String[]> relLines = new ArrayList<>();
            List<String[]> spouseLines = new ArrayList<>();
            List<String[]> childLines = new ArrayList<>();
            List<String> inventoryLines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("VERSION:")) {
                    version = Integer.parseInt(line.substring(8));
                } else if (line.startsWith("WORLD:")) {
                    loadedWorldName = line.substring(6);
                } else if (line.startsWith("TICK:")) {
                    loadedTick = Integer.parseInt(line.substring(5));
                } else if (line.startsWith("ACTIVE_PLAYER_NAME:")) {
                    activePlayerRef = line.substring(19);
                } else if (line.startsWith("ACTIVE_PLAYER:")) {
                    activePlayerRef = line.substring(14);
                } else if (line.startsWith("GAME_OVER:")) {
                    isGameOver = Boolean.parseBoolean(line.substring(10));
                } else if (line.startsWith("LOCATION:")) {
                    String[] locData = line.substring(9).split(",");
                    loadedLocIndex = Integer.parseInt(locData[0]);
                    loadedRoomIndex = Integer.parseInt(locData[1]);
                } else if (line.startsWith("ResState:")) {
                    String[] resData = line.substring(9).split(",");
                    parsedResStates.put(Integer.parseInt(resData[0]), resData);
                } else if (line.startsWith("Furn:")) {
                    furnLines.add(line.substring(5).split(","));
                } else if (line.startsWith("Stored:")) {
                    storedLines.add(line.substring(7));
                } else if (line.startsWith("STATS_MONEY:")) {
                    statsMoney = Integer.parseInt(line.substring(12));
                } else if (line.startsWith("STATS_ITEMS:")) {
                    statsItems = Integer.parseInt(line.substring(12));
                } else if (line.startsWith("Rel:")) {
                    relLines.add(line.substring(4).split(","));
                } else if (line.startsWith("Spouse:")) {
                    spouseLines.add(line.substring(7).split(","));
                } else if (line.startsWith("Child:")) {
                    childLines.add(line.substring(6).split(","));
                } else if (line.startsWith("Inventory:")) {
                    inventoryLines.add(line.substring(10));
                } else if (line.startsWith("NPC:")) {
                    npcLines.add(line.substring(4).split(",", -1));
                } else if (line.startsWith("Sim:")) {
                    simLines.add(line.substring(4).split(",", -1));
                }
            }

            if (version > SAVE_VERSION) {
                throw new IOException("save was made by a newer version of the game (format " + version + ")");
            }

            if (isGameOver) {
                UIManager.printGameOverStats(loadedTick, statsMoney, statsItems);
                return null;
            }

            final boolean encoded = version >= 2;
            if (encoded) {
                loadedWorldName = decode(loadedWorldName);
            }

            List<Sim> loadedNeighborhood = new ArrayList<>();
            List<Integer> savedRoomIndices = new ArrayList<>();
            for (String[] data : simLines) {
                Sim sim = parseSim(data, encoded);
                loadedNeighborhood.add(sim);
                savedRoomIndices.add(data.length > 22 ? Integer.parseInt(data[22]) : -1);
            }

            List<NPCSim> loadedNPCs = new ArrayList<>();
            for (String[] data : npcLines) {
                loadedNPCs.add(parseNPC(data, encoded));
            }

            List<Sim> allSims = new ArrayList<>(loadedNeighborhood);
            allSims.addAll(loadedNPCs);
            SimResolver resolver = new SimResolver(encoded, loadedNeighborhood, loadedNPCs, allSims);

            // Load Inventory
            for (String invLine : inventoryLines) {
                int firstComma = invLine.indexOf(',');
                if (firstComma != -1) {
                    Sim owner = resolver.resolve(invLine.substring(0, firstComma));
                    if (owner != null) {
                        Savable item = Savable.fromSaveString(invLine.substring(firstComma + 1));
                        if (item instanceof Item) {
                            owner.addItem((Item) item);
                        }
                    }
                }
            }

            // Load Relationships
            for (String[] relData : relLines) {
                Sim s1 = resolver.resolve(relData[0]);
                Sim s2 = resolver.resolve(relData[1]);
                if (s1 != null && s2 != null) {
                    s1.getRelationshipManager().increaseRelationship(s2, Integer.parseInt(relData[2]));
                }
            }

            // Load Spouses
            for (String[] spouseData : spouseLines) {
                Sim s1 = resolver.resolve(spouseData[0]);
                Sim s2 = resolver.resolve(spouseData[1]);
                if (s1 != null && s2 != null) {
                    s1.getRelationshipManager().setSpouse(s2);
                }
            }

            // Load Children parentage
            if (encoded) {
                for (String[] childData : childLines) {
                    Sim parent = resolver.resolve(childData[0]);
                    Sim child = resolver.resolve(childData[1]);
                    if (parent != null && child != null) {
                        parent.getRelationshipManager().addChild(child);
                    }
                }
            } else {
                // Version 1 saves didn't record parentage; best-effort guess.
                for (Sim sim : allSims) {
                    if (sim.isChildSim()) {
                        for (Sim parent : loadedNeighborhood) {
                            if (parent.getRelationshipManager().getSpouse() != null && !parent.isChildSim()) {
                                parent.getRelationshipManager().addChild(sim);
                            }
                        }
                    }
                }
            }

            GameEngine engine = new GameEngine(loadedWorldName, loadedTick, loadedNeighborhood, false);

            // Restore active player if saved
            if (activePlayerRef != null) {
                Sim savedActive = resolver.resolve(activePlayerRef);
                if (savedActive != null && loadedNeighborhood.contains(savedActive)
                        && savedActive.getState() != simcli.entities.models.SimState.DEAD) {
                    engine.setActivePlayer(savedActive);
                }
            }

            for (NPCSim npc : loadedNPCs) {
                engine.getNpcManager().addNPC(npc);
            }
            engine.getNpcManager().replenishNPCs(GameConstants.MAX_NPCS);

            // Post-Load injection: ownership and upgraded room capacities
            List<Building> map = engine.getWorldManager().getCityMap();
            for (Map.Entry<Integer, String[]> entry : parsedResStates.entrySet()) {
                Residential res = (Residential) map.get(entry.getKey());
                String[] resData = entry.getValue();
                res.setOwned(Boolean.parseBoolean(resData[1]));
                List<Room> rooms = res.getRooms();
                for (int r = 0; r < rooms.size() && r + 2 < resData.length; r++) {
                    rooms.get(r).setMaxCapacity(Integer.parseInt(resData[r + 2]));
                }
            }

            // Furniture layout and container contents (version 2+)
            if (encoded) {
                for (Building b : map) {
                    if (b instanceof Residential) {
                        for (Room r : ((Residential) b).getRooms()) {
                            r.clearFurniture();
                        }
                    }
                }
                for (String[] furn : furnLines) {
                    Room room = roomAt(map, Integer.parseInt(furn[0]), Integer.parseInt(furn[1]));
                    Interactable instance = InteractableFactory.create(decode(furn[2]));
                    if (instance != null) {
                        room.placeFurniture(instance, Integer.parseInt(furn[3]));
                    }
                }
                for (String stored : storedLines) {
                    String[] head = stored.split(",", 4);
                    Room room = roomAt(map, Integer.parseInt(head[0]), Integer.parseInt(head[1]));
                    Interactable holder = room.getInteractables().get(Integer.parseInt(head[2]));
                    Savable item = Savable.fromSaveString(head[3]);
                    if (holder instanceof Container && item instanceof Item) {
                        ((Container) holder).storeItem((Item) item);
                    }
                }
            }

            // Location and rooms
            if (loadedLocIndex >= 0 && loadedLocIndex < map.size()) {
                engine.getWorldManager().setCurrentLocation(map.get(loadedLocIndex));
                Building location = engine.getWorldManager().getCurrentLocation();
                if (location instanceof Residential) {
                    List<Room> rooms = ((Residential) location).getRooms();
                    if (encoded) {
                        for (int i = 0; i < loadedNeighborhood.size(); i++) {
                            int roomIndex = savedRoomIndices.get(i);
                            if (roomIndex >= 0 && roomIndex < rooms.size()) {
                                loadedNeighborhood.get(i).setCurrentRoom(rooms.get(roomIndex));
                            }
                        }
                    } else if (loadedRoomIndex >= 0 && loadedRoomIndex < rooms.size()) {
                        // Version 1 stored only the active player's room.
                        engine.getActivePlayer().setCurrentRoom(rooms.get(loadedRoomIndex));
                    }
                }
            }

            return engine;
        } catch (Exception e) {
            UIManager.printWarning("Could not load world '" + worldName
                    + "': the save file is unreadable or corrupted (" + e + ").");
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Parsing helpers
    // -------------------------------------------------------------------------

    private static Sim parseSim(String[] data, boolean encoded) {
        String name = encoded ? decode(data[0]) : data[0];
        Sim sim = new Sim(name, Integer.parseInt(data[1]), Gender.valueOf(data[2]), Job.valueOf(data[3]));
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
        if (data.length > 14) {
            sim.setHealth(Integer.parseInt(data[13]));
            sim.setStarvingTicks(Integer.parseInt(data[14]));
        }
        if (data.length > 15) {
            sim.setDaysAlive(Integer.parseInt(data[15]));
        }
        if (data.length > 21) {
            restoreTraits(sim, data[16]);
            sim.setTotalMoneyEarned(Integer.parseInt(data[17]));
            sim.setTotalItemsBought(Integer.parseInt(data[18]));
            sim.getCareerManager().setConsecutiveDaysMissed(Integer.parseInt(data[19]));
            sim.getCareerManager().setShiftsWorkedToday(Integer.parseInt(data[20]));
            sim.setWarnedAboutOverwork(Boolean.parseBoolean(data[21]));
        }
        restoreState(sim);
        return sim;
    }

    private static NPCSim parseNPC(String[] data, boolean encoded) {
        String name = encoded ? decode(data[0]) : data[0];
        NPCSim npc = new NPCSim(name, Integer.parseInt(data[1]), Gender.valueOf(data[2]), Job.valueOf(data[3]));
        npc.getHunger().setValue(Integer.parseInt(data[4]));
        npc.getEnergy().setValue(Integer.parseInt(data[5]));
        npc.getHappiness().setValue(Integer.parseInt(data[6]));
        npc.getHygiene().setValue(Integer.parseInt(data[7]));
        npc.getSocial().setValue(Integer.parseInt(data[8]));
        if (data.length > 10) {
            npc.setHealth(Integer.parseInt(data[9]));
            npc.setStarvingTicks(Integer.parseInt(data[10]));
        }
        if (data.length > 11) {
            npc.setDaysAlive(Integer.parseInt(data[11]));
        }
        if (data.length > 12) {
            restoreTraits(npc, data[12]);
        }
        restoreState(npc);
        return npc;
    }

    /** Recomputes state from needs; Sims past the death age stay dead. */
    private static void restoreState(Sim sim) {
        sim.updateState();
        if (sim.getAge() >= GameConstants.DEATH_AGE) {
            sim.markAsDead();
        }
    }

    private static String encodeTraits(Sim sim) {
        StringBuilder sb = new StringBuilder();
        for (Trait t : sim.getTraits()) {
            if (sb.length() > 0) sb.append('|');
            sb.append(t.name());
        }
        return sb.toString();
    }

    private static void restoreTraits(Sim sim, String field) {
        if (field.isEmpty()) {
            return;
        }
        sim.getTraits().clear();
        for (String t : field.split("\\|")) {
            sim.getTraits().add(Trait.valueOf(t));
        }
    }

    private static Room roomAt(List<Building> map, int buildingIndex, int roomIndex) {
        return ((Residential) map.get(buildingIndex)).getRooms().get(roomIndex);
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private static String decode(String text) {
        return URLDecoder.decode(text, StandardCharsets.UTF_8);
    }

    /**
     * Turns a reference from the save file back into a Sim: positional ids
     * ({@code H0}, {@code N3}) in version 2+, plain names in version 1.
     */
    private static final class SimResolver {
        private final boolean byId;
        private final List<Sim> household;
        private final List<NPCSim> npcs;
        private final List<Sim> all;

        SimResolver(boolean byId, List<Sim> household, List<NPCSim> npcs, List<Sim> all) {
            this.byId = byId;
            this.household = household;
            this.npcs = npcs;
            this.all = all;
        }

        Sim resolve(String ref) {
            if (!byId) {
                return findSimByName(all, ref);
            }
            int index = Integer.parseInt(ref.substring(1));
            switch (ref.charAt(0)) {
                case 'H': return household.get(index);
                case 'N': return npcs.get(index);
                default: throw new IllegalArgumentException("bad Sim reference: " + ref);
            }
        }
    }

    /**
     * Finds the first Sim with the given name (version 1 saves only).
     * @param list the list of sims to search.
     * @param name the name to look for.
     * @return the matching Sim, or null if not found.
     */
    private static Sim findSimByName(List<Sim> list, String name) {
        for (Sim s : list) {
            if (s.getName().equals(name)) return s;
        }
        return null;
    }
}
