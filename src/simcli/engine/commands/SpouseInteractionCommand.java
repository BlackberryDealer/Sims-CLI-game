package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.engine.HomeSupplies;
import simcli.engine.SimulationException;
import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.entities.models.Gender;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.ui.ItemInfo;
import simcli.ui.Panel;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.List;

/**
 * Command to interact with a spouse from anywhere if they are in the household.
 * Supports dating, having babies, feeding babies and giving gifts.
 *
 * <p>Previously coupled to {@code GameEngine} directly. Now uses
 * {@link CommandContext} for all data access and mutations.</p>
 */
public class SpouseInteractionCommand extends BaseCommand {

    /**
     * Constructs a {@code SpouseInteractionCommand} with the given context.
     *
     * @param ctx shared command context.
     */
    public SpouseInteractionCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Presents the marriage interaction menu (date, have baby, feed babies).
     *
     * @return {@link CommandResult#TICK_FORWARD} after dating or feeding babies,
     *         {@link CommandResult#NO_TICK} if the action is cancelled or unavailable.
     * @throws SimulationException if pregnancy rules prevent having a baby.
     */
    @Override
    protected CommandResult run() throws SimulationException {
        Sim activePlayer = ctx.getActivePlayer();
        Sim spouse = activePlayer.getRelationshipManager().getSpouse();
        
        // Check for any baby (non-playable child) in household
        boolean hasBabies = hasBabiesInHousehold();

        UIManager.title("Family");

        // Dating and having a baby need a living spouse at home;
        // feeding the babies is always possible.
        boolean spouseAvailable = true;
        if (spouse == null) {
            UIManager.info("You are not married. Find someone special at the park first!");
            spouseAvailable = false;
        } else if (spouse.getState() == SimState.DEAD) {
            UIManager.info(spouse.getName() + " has passed away. Their memory lives on.");
            spouseAvailable = false;
        } else if (!ctx.getNeighborhood().contains(spouse)) {
            UIManager.info(spouse.getName() + " is currently busy and not at home.");
            spouseAvailable = false;
        }

        if (!spouseAvailable && !hasBabies) {
            pause();
            return CommandResult.NO_TICK;
        }

        if (spouseAvailable) {
            int bond = activePlayer.getRelationshipManager().getRelationship(spouse);
            UIManager.printMessage("  {M}" + Panel.HEART + "{/} Married to {W}" + spouse.getName() + "{/}  "
                    + Panel.bar(bond, 10) + "\n");
            UIManager.option("1", "Go on a date", "+fun +social for you both");
            UIManager.option("2", "Try for a baby", babyStatus(activePlayer, spouse));
        }
        if (hasBabies) {
            UIManager.option("3", "Feed the babies", "+" + GameConstants.BABY_FEED_AMOUNT
                    + " hunger each, uses food from the fridge");
        }
        if (spouseAvailable) {
            UIManager.option("4", "Give a gift", activePlayer.getInventory().isEmpty()
                    ? "{k}your bag is empty{/}" : "pricier gifts mean more");
        }

        UIManager.option("0", "Back");
        UIManager.prompt("\nFamily> ");

        try {
            String input = ctx.getScanner().nextLine().trim();
            if (input.equals("0") || input.isEmpty()) return CommandResult.NO_TICK;

            int choice = Integer.parseInt(input);
            if (choice == 1 && spouseAvailable) {
                handleDate(activePlayer, spouse);
                return CommandResult.TICK_FORWARD;
            } else if (choice == 2 && spouseAvailable) {
                return handleHaveBaby(activePlayer);
            } else if (choice == 3 && hasBabies) {
                return handleFeedBabies(activePlayer);
            } else if (choice == 4 && spouseAvailable) {
                return handleGift(activePlayer, spouse);
            }
        } catch (NumberFormatException e) {
            UIManager.error("Invalid selection.");
            pause();
        }

        return CommandResult.NO_TICK;
    }

    /** Checks whether any non-playable child (baby) exists in the household. */
    private boolean hasBabiesInHousehold() {
        for (Sim s : ctx.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a date interaction between the active Sim and their spouse.
     * Increases happiness, social, and relationship values for both partners.
     *
     * @param sim    the active player Sim.
     * @param spouse the spouse Sim.
     */
    private void handleDate(Sim sim, Sim spouse) {
        sim.setCurrentAction(ActionState.SOCIALIZING);
        ctx.getLogger().logAnimation(sim);
        ctx.getLogger().log(sim.getName() + " and " + spouse.getName() + " spent a lovely time together on a date!");
        sim.getHappiness().increase(30);
        sim.getSocial().increase(50);
        spouse.getHappiness().increase(30);
        spouse.getSocial().increase(50);
        
        sim.getRelationshipManager().increaseRelationship(spouse, 10);
        spouse.getRelationshipManager().increaseRelationship(sim, 10);
        sim.trainSkill(SkillType.CHARISMA, GameConstants.SOCIAL_CHARISMA_XP);
    }

    /**
     * Lets the player pick an item from the bag and give it to the spouse.
     *
     * @param sim    the active player Sim.
     * @param spouse the spouse receiving the gift.
     * @return {@link CommandResult#TICK_FORWARD} if a gift was given,
     *         {@link CommandResult#NO_TICK} if the bag is empty or the player backs out.
     */
    private CommandResult handleGift(Sim sim, Sim spouse) throws SimulationException {
        List<Item> bag = sim.getInventory();
        if (bag.isEmpty()) {
            UIManager.info(sim.getName() + " has nothing to give. {k}Buy something at a shop first.{/}");
            pause();
            return CommandResult.NO_TICK;
        }
        UIManager.title("Give " + spouse.getName() + " a gift");
        for (int i = 0; i < bag.size(); i++) {
            Item item = bag.get(i);
            UIManager.option(String.valueOf(i + 1), Style.fit(item.getObjectName(), 18) + ItemInfo.describe(item));
        }
        UIManager.option("0", "Never mind");
        UIManager.prompt("\nGift> ");
        try {
            int choice = Integer.parseInt(ctx.getScanner().nextLine().trim());
            if (choice > 0 && choice <= bag.size()) {
                Item gift = bag.get(choice - 1);
                sim.setCurrentAction(ActionState.SOCIALIZING);
                int gained = sim.getRelationshipManager().giveGift(spouse, gift);
                ctx.getLogger().log(sim.getName() + " gives " + spouse.getName() + " a " + gift.getObjectName()
                        + ". " + spouse.getName() + " is delighted! (+" + gained + " relationship)");
                return CommandResult.TICK_FORWARD;
            }
        } catch (NumberFormatException e) {
            UIManager.error("Invalid selection.");
            pause();
        }
        return CommandResult.NO_TICK;
    }

    /** Which partner carries the baby: the female one. */
    private static Sim mother(Sim sim, Sim spouse) {
        return sim.getGender() == Gender.FEMALE ? sim : spouse;
    }

    /** Why the couple can't try for a baby right now, or {@code null} if they can. */
    private String babyBlocker(Sim sim, Sim spouse) {
        Sim mother = mother(sim, spouse);
        int today = ctx.getTimeManager().getCurrentDay();
        if (mother.isPregnant()) {
            int days = Math.max(0, mother.getPregnancyDueDay() - today);
            return mother.getName() + " is expecting - due " + (days <= 0 ? "tonight" : "in " + days
                    + (days == 1 ? " day" : " days"));
        }
        if (mother.getLastBirthDay() >= 0 && today - mother.getLastBirthDay() < GameConstants.BABY_COOLDOWN_DAYS) {
            return mother.getName() + " needs a little rest after the last baby";
        }
        return null;
    }

    /** The hint shown next to "Try for a baby". */
    private String babyStatus(Sim sim, Sim spouse) {
        if (sim.getGender() == spouse.getGender()) {
            return "{k}not possible for this couple{/}";
        }
        String blocker = babyBlocker(sim, spouse);
        return blocker != null ? "{k}" + blocker + "{/}"
                : "50% chance, born after " + GameConstants.PREGNANCY_DAYS + " days";
    }

    /**
     * Handles the "Have a Baby" action — teleports both parents home and
     * attempts pregnancy via the relationship manager. A success starts a
     * pregnancy; the baby is born {@link GameConstants#PREGNANCY_DAYS} days
     * later (see {@link simcli.engine.GameLoop}).
     *
     * @param sim the active player Sim.
     * @return {@link CommandResult#TICK_FORWARD} after any attempt (successful or not),
     *         {@link CommandResult#NO_TICK} if the couple cannot try at all.
     */
    private CommandResult handleHaveBaby(Sim sim) {
        Sim partner = sim.getRelationshipManager().getSpouse();
        if (partner != null && sim.getGender() != partner.getGender()) {
            String blocker = babyBlocker(sim, partner);
            if (blocker != null) {
                UIManager.info(blocker + ".");
                pause();
                return CommandResult.NO_TICK;
            }
        }
        Residential home = findHome();

        if (home != null && !home.getRooms().isEmpty()) {
            if (ctx.getWorldManager().getCurrentLocation() != home) {
                ctx.getWorldManager().setCurrentLocation(home);
                ctx.getLogger().log("Teleported home to " + home.getName() + " to try for a baby...");
            }
            if (!home.getRooms().contains(sim.getCurrentRoom())) {
                sim.setCurrentRoom(home.getRooms().get(0));
            }

            Sim spouse = sim.getRelationshipManager().getSpouse();
            if (spouse != null) {
                spouse.setCurrentRoom(sim.getCurrentRoom());
            }

            try {
                Gender babyGender = sim.getRelationshipManager().attemptPregnancy();
                if (babyGender != null) {
                    UIManager.prompt("Enter a name for the new " + babyGender + " baby: ");
                    String babyName = simcli.ui.MainMenu.cleanName(ctx.getScanner().nextLine());
                    if (babyName.isEmpty()) babyName = "Baby " + sim.getName();

                    Sim mother = mother(sim, spouse);
                    int dueDay = ctx.getTimeManager().getCurrentDay() + GameConstants.PREGNANCY_DAYS;
                    mother.startPregnancy(dueDay, babyName, babyGender);
                    ctx.getLogger().log(mother.getName() + " is expecting! " + babyName
                            + " is due on day " + dueDay + ".");
                }
                // An attempt takes time whether or not it succeeds — otherwise
                // the player could retry for free until it works.
                return CommandResult.TICK_FORWARD;
            } catch (SimulationException e) {
                ctx.getLogger().logWarning(e.getMessage());
            }
        }
        return CommandResult.NO_TICK;
    }

    /**
     * Picks where the couple goes to try for a baby: the current building if
     * it is a home, otherwise the most valuable residential property the
     * household owns.
     */
    private Residential findHome() {
        Building current = ctx.getWorldManager().getCurrentLocation();
        if (current instanceof Residential && ((Residential) current).isOwned()) {
            return (Residential) current;
        }
        Residential best = null;
        for (Building b : ctx.getWorldManager().getCityMap()) {
            if (b instanceof Residential) {
                Residential res = (Residential) b;
                if (res.isOwned() && (best == null || res.getPurchasePrice() > best.getPurchasePrice())) {
                    best = res;
                }
            }
        }
        return best;
    }

    /**
     * Feeds every living baby (non-playable child) in the household, one
     * piece of food from the fridge each. Also grants a small social and
     * happiness boost to the parent.
     *
     * @param parent the Sim performing the feeding.
     * @return {@link CommandResult#TICK_FORWARD} if any baby was fed,
     *         {@link CommandResult#NO_TICK} if the fridge had nothing for them.
     */
    private CommandResult handleFeedBabies(Sim parent) {
        int count = 0;
        for (Sim s : ctx.getNeighborhood()) {
            if (s.isChildSim() && !s.isPlayable() && s.getState() != SimState.DEAD
                    && HomeSupplies.feedBaby(parent, s, ctx.getWorldManager(), ctx.getLogger())) {
                count++;
            }
        }
        if (count == 0) {
            UIManager.info("There's no food in the fridge for the babies! {k}Buy some at the Supermarket.{/}");
            pause();
            return CommandResult.NO_TICK;
        }
        parent.getSocial().increase(10);
        parent.getHappiness().increase(5);
        return CommandResult.TICK_FORWARD;
    }
}
