package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.managers.WantsManager;
import simcli.entities.models.Fear;
import simcli.entities.models.Job;
import simcli.entities.models.Moodlet;
import simcli.entities.models.Reward;
import simcli.entities.models.SimSnapshot;
import simcli.entities.models.SkillType;
import simcli.entities.models.Want;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.interactables.Computer;
import simcli.world.interactables.WeightBench;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gives the household an inner life: feelings ({@link Moodlet}s), daily
 * {@link Want}s and {@link Fear}s, and memories for the family album.
 *
 * <p>Rather than every action reporting what it did, this manager takes a
 * {@link SimSnapshot} of each Sim after every turn and compares it with the
 * last one. A rise in meals cooked means a home-cooked meal; a new name in
 * the friends list means a new friend; a new spouse means a wedding. Each
 * change can bring a moodlet, a memory, or both.</p>
 *
 * <p>Each new day every playable Sim rolls a few wants and one fear. Wants
 * done earn satisfaction points (spent on {@link Reward}s); a fear that
 * comes true costs points and leaves a bad moodlet.</p>
 */
public class InnerLifeManager {

    private final IWorldManager worldManager;
    private final SimulationLogger logger;

    /** Each Sim as they were at the end of the last turn. */
    private final Map<Sim, SimSnapshot> lastTurn = new IdentityHashMap<>();
    /** Each Sim as they were when today's wants were rolled. */
    private final Map<Sim, SimSnapshot> atRoll = new IdentityHashMap<>();
    /** Homes the household owned at the end of the last turn, or {@code null} before the first. */
    private Set<String> ownedHomes;
    /** Whether the first look at the household has been taken. */
    private boolean started;

    /**
     * Creates an InnerLifeManager.
     *
     * @param worldManager the city, for checking homes and furniture (may be {@code null}).
     * @param logger       where wants, fears and big feelings are announced.
     */
    public InnerLifeManager(IWorldManager worldManager, SimulationLogger logger) {
        this.worldManager = worldManager;
        this.logger = logger;
    }

    /**
     * Looks at what changed for every Sim since the last turn: hands out
     * moodlets and memories, settles wants and fears, and rolls new ones
     * when a new day has begun.
     *
     * @param household every Sim in the household.
     * @param day       today's day number.
     */
    public void afterTurn(List<Sim> household, int day) {
        List<String> newHomes = checkHomes();
        for (Sim sim : new ArrayList<>(household)) {
            SimSnapshot now = SimSnapshot.of(sim);
            SimSnapshot before = lastTurn.get(sim);
            if (before == null) {
                welcome(sim, household, day);
            } else {
                notice(sim, before, now, day);
            }
            if (now.alive && sim.isPlayable()) {
                for (String home : newHomes) {
                    sim.remember(day, "Moved into " + home);
                }
                WantsManager wants = sim.getWants();
                SimSnapshot rolled = atRoll.get(sim);
                if (rolled != null) {
                    settle(sim, rolled, now);
                }
                if (wants.getDay() != day) {
                    rollDay(sim, day);
                    atRoll.put(sim, SimSnapshot.of(sim));
                } else if (rolled == null) {
                    // loaded mid-day: wants already rolled, progress counts from now
                    atRoll.put(sim, now);
                }
            }
            lastTurn.put(sim, SimSnapshot.of(sim));
        }
        started = true;
    }

    // -------------------------------------------------------------------------
    // Moodlets and memories
    // -------------------------------------------------------------------------

    /**
     * First time this Sim is seen: at the start of a session (a brand-new
     * household starts its story), or later on (a newborn, or a spouse
     * who moved in).
     */
    private void welcome(Sim sim, List<Sim> household, int day) {
        if (!sim.getMemories().isEmpty()) {
            return;
        }
        if (!started) {
            if (day <= 1) {
                Building home = worldManager != null ? worldManager.getCurrentLocation() : null;
                sim.remember(day, home != null ? "Started a new life at " + home.getName() : "Started a new life");
            }
        } else if (sim.isChildSim() && !sim.isPlayable()) {
            List<String> parents = new ArrayList<>();
            for (Sim other : household) {
                if (other.getRelationshipManager().getChildren().contains(sim)) {
                    parents.add(other.getName());
                }
            }
            sim.remember(day, parents.isEmpty() ? "Was born" : "Was born to " + String.join(" and ", parents));
        } else if (sim.getRelationshipManager().getSpouse() != null) {
            sim.remember(day, "Moved in with " + sim.getRelationshipManager().getSpouse().getName());
        }
    }

    /** Hands out moodlets and memories for whatever changed this turn. */
    private void notice(Sim sim, SimSnapshot before, SimSnapshot now, int day) {
        if (!now.alive) {
            if (before.alive) {
                sim.remember(day, "Passed away at the age of " + now.age);
            }
            return;
        }
        boolean feels = sim.isPlayable();

        if (now.age > before.age && feels) {
            feel(sim, Moodlet.BIRTHDAY);
        }
        if (!now.stage.equals(before.stage)) {
            sim.remember(day, "Became " + withArticle(now.stage.toLowerCase()));
        }
        if (now.career != before.career) {
            if (now.career == Job.UNEMPLOYED && now.timesFired > before.timesFired) {
                sim.remember(day, "Was fired from the " + before.career.getTitle() + " job");
                feel(sim, Moodlet.FIRED);
            } else if (now.career == Job.UNEMPLOYED) {
                sim.remember(day, "Left the " + before.career.getTitle() + " job");
            } else {
                sim.remember(day, "Got a job as " + withArticle(now.career.getTitle()));
            }
        } else if (now.jobTier > before.jobTier && now.career != Job.UNEMPLOYED) {
            sim.remember(day, now.jobTier >= now.career.getMaxTier()
                    ? "Reached the top of the " + now.career.getTitle() + " career"
                    : "Was promoted to tier " + now.jobTier + " as " + withArticle(now.career.getTitle()));
            feel(sim, Moodlet.PROMOTED);
        }
        for (SkillType skill : SkillType.values()) {
            int was = before.level(skill);
            int is = now.level(skill);
            if (is > was) {
                if (feels) {
                    feel(sim, Moodlet.LEARNED_SOMETHING);
                }
                for (int milestone : new int[]{5, GameConstants.MAX_SKILL_LEVEL}) {
                    if (was < milestone && is >= milestone) {
                        sim.remember(day, "Reached " + skill.getDisplayName() + " level " + milestone);
                    }
                }
            }
        }
        for (Sim friend : SimSnapshot.added(before.friends, now.friends)) {
            sim.remember(day, "Became friends with " + friend.getName());
            feel(sim, Moodlet.NEW_FRIEND);
        }
        if (before.spouse == null && now.spouse != null) {
            sim.remember(day, "Married " + now.spouse.getName());
            feel(sim, Moodlet.NEWLYWED);
        }
        for (Sim baby : SimSnapshot.added(before.children, now.children)) {
            sim.remember(day, "Welcomed a baby, " + baby.getName());
            feel(sim, Moodlet.NEW_PARENT);
        }
        if (now.aspirationsFulfilled > before.aspirationsFulfilled) {
            sim.remember(day, "Fulfilled a life's dream: " + sim.getAspiration().getDisplayName());
            feel(sim, Moodlet.LIVING_THE_DREAM);
        }
        for (Sim lost : SimSnapshot.added(before.lostLovedOnes, now.lostLovedOnes)) {
            sim.remember(day, "Lost " + lost.getName());
            if (feels) {
                feel(sim, Moodlet.GRIEVING);
                logger.log(sim.getName() + " is grieving for " + lost.getName() + ".");
            }
        }
        if (!feels) {
            return;
        }

        if (now.meals > before.meals) {
            feel(sim, Moodlet.HOME_COOKED);
        }
        if (now.energy >= 90 && now.energy - before.energy >= 40) {
            feel(sim, Moodlet.WELL_RESTED);
        }
        if (now.hygiene - before.hygiene >= 40) {
            feel(sim, Moodlet.SQUEAKY_CLEAN);
        }
        if (now.fun - before.fun >= 20) {
            feel(sim, Moodlet.HAD_A_BLAST);
        }
        if (now.social - before.social >= 15) {
            feel(sim, Moodlet.GOOD_COMPANY);
        }
        if (now.anyRelationshipDroppedSince(before)) {
            feel(sim, Moodlet.REJECTED);
        }
        if (now.daysMissed > before.daysMissed) {
            feel(sim, Moodlet.GUILTY);
        }
    }

    private static void feel(Sim sim, Moodlet moodlet) {
        sim.getMoodlets().add(moodlet);
    }

    /** Names of homes bought since the last turn (none on the first turn). */
    private List<String> checkHomes() {
        List<String> bought = new ArrayList<>();
        if (worldManager == null) {
            return bought;
        }
        Set<String> owned = new LinkedHashSet<>();
        for (Building building : worldManager.getCityMap()) {
            if (building instanceof Residential && ((Residential) building).isOwned()) {
                owned.add(building.getName());
            }
        }
        if (ownedHomes != null) {
            for (String home : owned) {
                if (!ownedHomes.contains(home)) {
                    bought.add(home);
                }
            }
        }
        ownedHomes = owned;
        return bought;
    }

    // -------------------------------------------------------------------------
    // Wants and fears
    // -------------------------------------------------------------------------

    /** Pays out wants that are done and punishes a fear that came true. */
    private void settle(Sim sim, SimSnapshot rolled, SimSnapshot now) {
        WantsManager wants = sim.getWants();
        for (Want want : wants.getWants()) {
            if (!wants.isDone(want) && want.isDone(rolled, now)) {
                wants.markDone(want);
                wants.addPoints(want.getPoints());
                feel(sim, Moodlet.WISH_CAME_TRUE);
                logger.log(sim.getName() + " did something they wanted: " + want.getText()
                        + " (+" + want.getPoints() + " satisfaction)");
                if (wants.getDoneCount() == wants.getWants().size()) {
                    wants.addPoints(GameConstants.ALL_WANTS_BONUS);
                    logger.log("\n*** " + sim.getName() + " did everything they wanted today! (+"
                            + GameConstants.ALL_WANTS_BONUS + " bonus satisfaction) ***");
                }
            }
        }
        Fear fear = wants.getFear();
        if (fear != null && !wants.hasFearCameTrue() && fear.cameTrue(rolled, now)) {
            wants.markFearCameTrue();
            int lost = wants.losePoints(GameConstants.FEAR_POINT_PENALTY);
            feel(sim, Moodlet.WORST_FEAR);
            logger.logWarning(sim.getName() + "'s fear came true: " + fear.getText().toLowerCase()
                    + (lost > 0 ? " (-" + lost + " satisfaction)" : ""));
        }
    }

    /** Gives a Sim their wants and fear for a new day. */
    void rollDay(Sim sim, int day) {
        int count = GameConstants.WANTS_PER_DAY + (sim.hasPerk(Reward.BIG_DREAMER) ? 1 : 0);
        List<Want> pool = new ArrayList<>();
        for (Want want : Want.values()) {
            if (want.suits(sim, day) && homeAllows(sim, want)) {
                pool.add(want);
            }
        }
        List<Want> chosen = new ArrayList<>();
        while (chosen.size() < count && !pool.isEmpty()) {
            int total = 0;
            for (Want want : pool) {
                total += want.weightFor(sim);
            }
            int roll = GameRandom.RANDOM.nextInt(total);
            for (Want want : pool) {
                roll -= want.weightFor(sim);
                if (roll < 0) {
                    chosen.add(want);
                    pool.remove(want);
                    break;
                }
            }
        }
        List<Fear> fears = new ArrayList<>();
        for (Fear fear : Fear.values()) {
            if (fear.suits(sim)) {
                fears.add(fear);
            }
        }
        Fear fear = fears.isEmpty() ? null : fears.get(GameRandom.RANDOM.nextInt(fears.size()));
        sim.getWants().startDay(day, chosen, fear);

        StringBuilder sb = new StringBuilder(sim.getName() + " wants to: ");
        for (int i = 0; i < chosen.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(chosen.get(i).getText().toLowerCase());
        }
        if (fear != null) {
            sb.append(". Fears: ").append(fear.getText().toLowerCase());
        }
        logger.log(sb.append('.').toString());
    }

    /** Wants that need furniture only come up if the household's home has it. */
    private boolean homeAllows(Sim sim, Want want) {
        switch (want) {
            case WORK_OUT:
                return !HomeSupplies.find(worldManager, WeightBench.class).isEmpty();
            case STUDY:
                return !HomeSupplies.find(worldManager, Computer.class).isEmpty()
                        || (sim.getLifeStage() != null && sim.getLifeStage().attendsSchool());
            default:
                return true;
        }
    }

    /** "a Teen", "an Elder", "a Barista". */
    private static String withArticle(String noun) {
        return ("AEIOUaeiou".indexOf(noun.charAt(0)) >= 0 ? "an " : "a ") + noun;
    }
}
