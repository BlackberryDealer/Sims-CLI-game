package simcli.persistence;

import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.items.Food;
import simcli.entities.items.Furniture;
import simcli.entities.items.Ingredient;
import simcli.entities.items.Item;
import simcli.entities.models.Job;
import simcli.utils.GameConstants;
import simcli.utils.Json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The game's content — shop catalogs and the city layout — loaded from the
 * JSON files in {@code resources/simcli/data/}, so new items, shops and
 * buildings can be added without touching the code.
 *
 * <p>Files are read from the classpath first (how the Maven build and the
 * batch scripts run the game), then from a {@code resources/} folder next to
 * the compiled classes or in the working directory (for IDEs that don't put
 * it on the classpath). They are loaded once and cached.</p>
 */
public final class GameContent {

    private static final String DATA_DIR = "simcli/data/";

    private static Map<String, Map<String, Object>> shops;
    private static List<Map<String, Object>> buildings;

    private GameContent() {
    }

    // -------------------------------------------------------------------------
    // Shops
    // -------------------------------------------------------------------------

    /**
     * Returns what a shop sells to the given Sim: every item in the shop's
     * catalog whose {@code availableIf} conditions (if any) the Sim meets.
     *
     * @param shopId the shop's id in {@code shops.json}.
     * @param sim    the shopper.
     * @return fresh item instances, in catalog order.
     * @throws IllegalStateException if the shop doesn't exist.
     */
    public static List<Item> shopCatalog(String shopId, Sim sim) {
        List<Item> catalog = new ArrayList<>();
        for (Object entry : list(shop(shopId).get("items"), "items")) {
            Map<String, Object> item = map(entry, "shop item");
            if (isAvailable(item, sim)) {
                catalog.add(item(item));
            }
        }
        return catalog;
    }

    /**
     * Returns the name of a shop's register (what the player uses to shop).
     *
     * @param shopId the shop's id in {@code shops.json}.
     * @return the register name, e.g. "Store Register".
     */
    public static String registerName(String shopId) {
        return string(shop(shopId), "register");
    }

    private static Map<String, Object> shop(String shopId) {
        if (shops == null) {
            Map<String, Map<String, Object>> byId = new HashMap<>();
            for (Object entry : list(load("shops.json").get("shops"), "shops")) {
                Map<String, Object> shop = map(entry, "shop");
                byId.put(string(shop, "id"), shop);
            }
            shops = byId;
        }
        Map<String, Object> shop = shops.get(shopId);
        if (shop == null) {
            throw new IllegalStateException("shops.json has no shop with id '" + shopId + "'");
        }
        return shop;
    }

    /** An item from its JSON description. */
    static Item item(Map<String, Object> json) {
        String type = string(json, "type");
        String name = string(json, "name");
        int price = integer(json, "price", 0);
        switch (type) {
            case "food":
                return new Food(name, price, integer(json, "hunger", 0), integer(json, "energy", 0),
                        integer(json, "freshness", GameConstants.FOOD_SHELF_LIFE_DAYS));
            case "ingredient":
                return new Ingredient(name, price, integer(json, "hunger", 0), string(json, "meal"),
                        integer(json, "mealHunger", 0),
                        integer(json, "freshness", GameConstants.INGREDIENT_SHELF_LIFE_DAYS));
            case "consumable":
                return new Consumable(name, price, integer(json, "hunger", 0), integer(json, "energy", 0),
                        integer(json, "fun", 0));
            case "furniture":
                return new Furniture(name, price, integer(json, "space", 0));
            default:
                throw new IllegalStateException("unknown item type '" + type + "' for " + name);
        }
    }

    /** Whether the Sim meets at least one of the item's conditions (or it has none). */
    static boolean isAvailable(Map<String, Object> json, Sim sim) {
        Object conditions = json.get("availableIf");
        if (conditions == null) {
            return true;
        }
        for (Object entry : list(conditions, "availableIf")) {
            Map<String, Object> condition = map(entry, "condition");
            boolean met = sim.getAge() >= integer(condition, "minAge", 0)
                    && sim.getMoney() >= integer(condition, "minMoney", 0);
            if (condition.containsKey("career")) {
                met &= sim.getCareer() == Job.valueOf(string(condition, "career"));
            }
            if (met) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Buildings
    // -------------------------------------------------------------------------

    /**
     * Returns the city layout from {@code buildings.json}, in travel-menu order.
     *
     * @return the building descriptions (read-only).
     */
    public static List<Map<String, Object>> buildings() {
        if (buildings == null) {
            List<Map<String, Object>> loaded = new ArrayList<>();
            for (Object entry : list(load("buildings.json").get("buildings"), "buildings")) {
                loaded.add(map(entry, "building"));
            }
            buildings = Collections.unmodifiableList(loaded);
        }
        return buildings;
    }

    // -------------------------------------------------------------------------
    // JSON helpers (also used by the world builder)
    // -------------------------------------------------------------------------

    /**
     * Reads a required text field.
     *
     * @param json the object.
     * @param key  the field name.
     * @return the field's value.
     * @throws IllegalStateException if it's missing or not text.
     */
    public static String string(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (!(value instanceof String)) {
            throw new IllegalStateException("expected text for '" + key + "' in " + json);
        }
        return (String) value;
    }

    /**
     * Reads a whole-number field, or a default if it's missing.
     *
     * @param json     the object.
     * @param key      the field name.
     * @param fallback the value to use if the field is missing.
     * @return the field's value.
     */
    public static int integer(Map<String, Object> json, String key, int fallback) {
        Object value = json.get(key);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Double)) {
            throw new IllegalStateException("expected a number for '" + key + "' in " + json);
        }
        return (int) Math.round((Double) value);
    }

    /**
     * Reads a list field, or an empty list if it's missing.
     *
     * @param json the object.
     * @param key  the field name.
     * @return the list.
     */
    public static List<Object> list(Map<String, Object> json, String key) {
        Object value = json.get(key);
        return value == null ? Collections.emptyList() : list(value, key);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value, String what) {
        if (!(value instanceof List)) {
            throw new IllegalStateException("expected a list for '" + what + "'");
        }
        return (List<Object>) value;
    }

    /**
     * Casts a parsed JSON value to an object.
     *
     * @param value the parsed value.
     * @param what  what it should be, for the error message.
     * @return the object.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Object value, String what) {
        if (!(value instanceof Map)) {
            throw new IllegalStateException("expected an object for " + what + ", got " + value);
        }
        return (Map<String, Object>) value;
    }

    // -------------------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------------------

    private static Map<String, Object> load(String file) {
        String text = read(file);
        try {
            return map(Json.parse(text), file);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(file + ": " + e.getMessage(), e);
        }
    }

    private static String read(String file) {
        try (InputStream in = GameContent.class.getClassLoader().getResourceAsStream(DATA_DIR + file)) {
            if (in != null) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + file + ": " + e.getMessage(), e);
        }
        for (Path candidate : fallbackLocations(file)) {
            if (Files.isRegularFile(candidate)) {
                try {
                    return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("could not read " + candidate + ": " + e.getMessage(), e);
                }
            }
        }
        throw new IllegalStateException("game data file " + file + " not found - expected it on the classpath"
                + " under " + DATA_DIR + " or in resources/" + DATA_DIR);
    }

    /** resources/ beside the compiled classes' folder, then in the working directory. */
    private static List<Path> fallbackLocations(String file) {
        List<Path> paths = new ArrayList<>();
        try {
            Path classes = Paths.get(GameContent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classes.getParent() != null) {
                paths.add(classes.getParent().resolve("resources").resolve(DATA_DIR + file));
            }
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            // no code source to go by; try the working directory
        }
        paths.add(Paths.get("resources", DATA_DIR + file));
        return paths;
    }
}
