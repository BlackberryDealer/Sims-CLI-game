package simcli.ui;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.*;
import simcli.world.Building;
import simcli.world.interactables.Interactable;
import simcli.ui.ascii.AsciiEngine;
import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link IRenderer} for terminal-based display.
 *
 * <p>The game screen is built from {@link Panel}s: a date/location banner
 * and scene art, the active Sim's need bars, the rest of the household,
 * and the action menu grouped by purpose.</p>
 */
public class TerminalRenderer implements IRenderer {
    private final AsciiEngine asciiEngine;

    /** Friendly verbs for the things a Sim can use, keyed by object name. */
    private static final Map<String, String> VERBS = new HashMap<>();
    static {
        VERBS.put("Bed", "Sleep");
        VERBS.put("Fridge", "Open fridge");
        VERBS.put("Shower", "Shower");
        VERBS.put("Weight Bench", "Work out");
        VERBS.put("Computer", "Study");
        VERBS.put("Storage Chest", "Storage chest");
        VERBS.put("Store Register", "Shop groceries");
        VERBS.put("Bookshop Register", "Browse books");
        VERBS.put("Socialize with NPCs", "Meet people");
    }

    public TerminalRenderer() {
        this.asciiEngine = new AsciiEngine();
    }

    @Override
    public void clear() {
        UIManager.clearScreen();
    }

    @Override
    public void printHint() {
        UIManager.printHint();
    }

    @Override
    public void renderHUD(Sim player, Building location, int day, String formattedTime,
            String timeOfDay, boolean inRoom, String roomName) {

        String icon;
        switch (timeOfDay) {
            case "Morning":   icon = "{Y}" + Panel.SUN + "{/}"; break;
            case "Afternoon": icon = "{Y}" + Panel.SUN + "{/}"; break;
            case "Evening":   icon = "{y}" + Panel.SUN + "{/}"; break;
            default:          icon = "{B}" + Panel.MOON + "{/}"; break;
        }
        String weekday = TimeManager.dayOfWeekName(day);
        String weekend = TimeManager.isWeekend(day) ? "  {M}weekend{/}" : "";
        String left = "{W}{*}DAY " + day + "{/}  {C}" + weekday + "{/}  " + icon + " {W}" + formattedTime
                + "{/} {k}" + timeOfDay + "{/}" + weekend;

        String locationName = location != null ? location.getName() : "Unknown";
        String right = "{G}" + locationName + "{/}" + (inRoom ? " {k}" + Panel.ARROW + "{/} {W}" + roomName + "{/}" : "");

        UIManager.printMessage(Panel.banner(left, right, 'c'));
        System.out.println();
        System.out.println(asciiEngine.render(player, location));
        System.out.println();

        // Reset action to IDLE after rendering so art doesn't persist next frame
        player.setCurrentAction(ActionState.IDLE);
    }

    @Override
    public void renderHouseholdDashboard(List<Sim> neighborhood, Sim activePlayer) {
        List<String> cells = new ArrayList<>();
        for (Sim sim : neighborhood) {
            if (sim == activePlayer) {
                continue;
            }
            cells.add(householdCell(sim));
        }
        if (cells.isEmpty()) {
            return;
        }
        System.out.println(Panel.box("{W}Household{/}", 'k', Panel.columns(cells, 37)));
    }

    private String householdCell(Sim sim) {
        if (sim.getState() == SimState.DEAD) {
            return "{k}" + Panel.CROSS + " " + sim.getName() + " (R.I.P.){/}";
        }
        char mood = Sprites.moodColor(sim);
        String name = "{" + mood + "}" + Panel.STAR + "{/} {W}" + sim.getName() + "{/}";
        if (sim.isChildSim() && !sim.isPlayable()) {
            int hunger = sim.getHunger().getValue();
            return name + " {k}baby{/}  hunger {" + Style.levelColor(hunger) + "}" + hunger + "{/}";
        }
        String state = sim.getState() == SimState.HEALTHY ? ""
                : "  {Y}" + sim.getState().name().toLowerCase() + "{/}";
        int health = sim.getHealth();
        return name + "  {" + Style.levelColor(health) + "}" + Panel.HEART + " " + health + "{/}" + state;
    }

    @Override
    public void renderActiveSimStats(Sim activePlayer, List<Sim> neighborhood) {
        String job = activePlayer.getCareer() == Job.UNEMPLOYED ? "Unemployed"
                : activePlayer.getCareer().getTitle() + " (tier " + activePlayer.getJobTier() + ")";
        char mood = Sprites.moodColor(activePlayer);
        String title = "{" + mood + "}" + Panel.STAR + "{/} {W}{*}" + activePlayer.getName().toUpperCase() + "{/}"
                + "  {k}" + activePlayer.getAge() + " " + Panel.DOT + " " + activePlayer.getCurrentStageName()
                + " " + Panel.DOT + " " + job + "{/}";

        int w = 18;
        List<String> lines = new ArrayList<>();
        lines.add(Panel.meter("Hunger", activePlayer.getHunger().getValue(), w) + "   "
                + Panel.meter("Energy", activePlayer.getEnergy().getValue(), w));
        lines.add(Panel.meter("Hygiene", activePlayer.getHygiene().getValue(), w) + "   "
                + Panel.meter("Fun", activePlayer.getHappiness().getValue(), w));
        lines.add(Panel.meter("Social", activePlayer.getSocial().getValue(), w) + "   "
                + Panel.meter("Health", activePlayer.getHealth(), w));

        String state;
        switch (activePlayer.getState()) {
            case HUNGRY: state = "{R}{*}HUNGRY - eat something!{/}"; break;
            case TIRED:  state = "{Y}{*}TIRED - get some sleep{/}"; break;
            case DEAD:   state = "{k}deceased{/}"; break;
            default:     state = "{G}feeling fine{/}"; break;
        }
        lines.add("{G}{*}$" + activePlayer.getMoney() + "{/}   {k}bag{/} " + activePlayer.getInventory().size()
                + "/" + activePlayer.getInventoryCapacity() + "   {k}traits{/} " + traits(activePlayer)
                + "   " + state);

        System.out.println(Panel.box(title, mood == 'R' ? 'r' : 'c', lines));
    }

    private static String traits(Sim sim) {
        StringBuilder sb = new StringBuilder();
        for (Trait t : sim.getTraits()) {
            if (sb.length() > 0) sb.append(", ");
            String name = t.name();
            sb.append(name.charAt(0)).append(name.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    @Override
    public void renderDeathStats(Sim deadSim) {
        clear();
        System.out.println();
        UIManager.printMessage(AsciiArt.tombstone(deadSim.getName(), deadSim.getAge()));
        System.out.println();
        System.out.println(Panel.box("{W}In loving memory of " + deadSim.getName() + "{/}", 'k',
                "{k}Age{/}            " + deadSim.getAge(),
                "{k}Final needs{/}    hunger " + deadSim.getHunger().getValue()
                        + "  energy " + deadSim.getEnergy().getValue()
                        + "  hygiene " + deadSim.getHygiene().getValue()
                        + "  fun " + deadSim.getHappiness().getValue()
                        + "  social " + deadSim.getSocial().getValue(),
                "{k}Money left{/}     $" + deadSim.getMoney(),
                "{k}Total earned{/}   {G}$" + deadSim.getTotalMoneyEarned() + "{/}"));
    }

    @Override
    public void renderActions(Sim activePlayer, List<Interactable> items, boolean isResidential) {
        List<String> lines = new ArrayList<>();

        List<String> here = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            here.add(Panel.option(String.valueOf(i + 1), verb(items.get(i))));
        }
        if (here.isEmpty()) {
            here.add("{k}nothing to use here{/}");
        }
        addGroup(lines, "HERE", here, 22);

        addGroup(lines, "OUT", List.of(
                Panel.option("W", "Work"),
                Panel.option("T", "Travel"),
                Panel.option("J", "Jobs")), 16);

        if (isResidential) {
            addGroup(lines, "HOME", List.of(
                    Panel.option("M", "Move room"),
                    Panel.option("H", "House info"),
                    Panel.option("U", "Upgrade room")), 16);
        }

        List<String> me = new ArrayList<>(List.of(
                Panel.option("I", "Status"),
                Panel.option("V", "Inventory"),
                Panel.option("K", "Switch Sim"),
                Panel.option("G", "Goals")));
        if (activePlayer.getRelationshipManager().getSpouse() != null) {
            me.add(Panel.option("L", "Family"));
        }
        addGroup(lines, "SIM", me, 16);

        addGroup(lines, "GAME", List.of(Panel.option("S", "Save & quit to menu")), 16);

        System.out.println(Panel.box("What should {W}" + activePlayer.getName() + "{/} do?", 'b', lines));
    }

    /** Adds a labelled group of options, wrapping onto more rows if needed. */
    private static void addGroup(List<String> lines, String label, List<String> cells, int cellWidth) {
        int labelWidth = 6;
        int perRow = Math.max(1, (Panel.INNER - labelWidth) / cellWidth);
        for (int start = 0; start < cells.size(); start += perRow) {
            List<String> chunk = cells.subList(start, Math.min(cells.size(), start + perRow));
            String head = start == 0 ? "{k}" + label + "{/}" : "";
            StringBuilder row = new StringBuilder(Style.fit(Style.render(head), labelWidth));
            for (int i = 0; i < chunk.size(); i++) {
                String cell = Style.render(chunk.get(i));
                row.append(i == chunk.size() - 1 ? cell : Style.fit(cell, cellWidth));
            }
            lines.add(row.toString());
        }
    }

    private static String verb(Interactable item) {
        String name = item.getObjectName();
        return VERBS.getOrDefault(name, "Use " + name);
    }
}
