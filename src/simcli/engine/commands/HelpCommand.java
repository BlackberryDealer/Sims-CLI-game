package simcli.engine.commands;

import simcli.engine.CommandResult;
import simcli.ui.Panel;
import simcli.utils.GameConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Command that shows the help screen: every key, how long things take,
 * and the main rules of the game.
 */
public class HelpCommand extends BaseCommand {

    /**
     * Constructs a {@code HelpCommand}.
     *
     * @param ctx shared command context.
     */
    public HelpCommand(CommandContext ctx) {
        super(ctx);
    }

    /**
     * Prints the help screen and waits for the user to press ENTER.
     *
     * @return {@link CommandResult#NO_TICK} — reading help does not advance time.
     */
    @Override
    protected CommandResult run() {
        List<String> lines = new ArrayList<>();
        lines.add("{W}Keys{/}");
        lines.add(key("1-9", "use something listed under HERE {k}(1 hour; sleep lasts till morning){/}"));
        lines.add(key("W", "work a shift on a weekday {k}(the shift's length){/}"));
        lines.add(key("T", "travel somewhere, or buy a home {k}(1 hour){/}"));
        lines.add(key("J", "job market"));
        lines.add(key("C", "chat with someone at home {k}(1 hour){/}"));
        lines.add(key("M H U", "move room / house info / upgrade a room ($" + GameConstants.UPGRADE_COST + ")"));
        lines.add(key("I V", "status (skills, job) / inventory"));
        lines.add(key("K", "switch to another Sim in the household"));
        lines.add(key("L", "family: dates, babies, gifts"));
        lines.add(key("G", "goals: aspirations and achievements"));
        lines.add(key("R", "wants, fears and the reward store"));
        lines.add(key("A", "family album: everyone's memories"));
        lines.add(key("S", "save and quit to the menu"));
        lines.add(key("0", "go back, in any menu"));
        lines.add("{k}Menus and info screens never take any time.{/}");
        lines.add("");
        lines.add("{W}Rules of thumb{/}");
        lines.add(rule("Needs fall every hour. At hunger 0 health drains fast."));
        lines.add(rule("A Sim dies when their health reaches 0."));
        lines.add(rule("Sleep skips to " + String.format("%02d:00", GameConstants.MORNING_HOUR)
                + "; the whole household goes to bed with you."));
        lines.add(rule("Sims you aren't controlling eat, shower, nap and chat on their own."));
        lines.add(rule("Mood is your needs plus your moodlets (feelings that wear off)."));
        lines.add(rule("A great mood (" + GameConstants.GREAT_MOOD_THRESHOLD
                + "+) means more pay and faster skills."));
        lines.add(rule("Each day brings new wants (earn points) and a fear (costs points)."));
        lines.add(rule("Good shifts fill your performance meter; a full meter is a promotion."));
        lines.add(rule("Skills can only learn " + GameConstants.SKILL_DAILY_XP_CAP
                + " points a day, so practise a little daily."));
        lines.add(rule("Food goes off, faster outside the fridge. Cook raw food."));
        lines.add(rule("Bills for every home you own are due each Monday."));
        lines.add(rule("Babies arrive " + GameConstants.PREGNANCY_DAYS
                + " days after you conceive, and eat from the fridge."));
        lines.add(rule("Fulfil every Sim's aspiration to complete your legacy and win;"));
        lines.add("  {k}then pick new ones, and pass the legacy on to your children.{/}");

        System.out.println(Panel.box("{W}{*}Help{/}", 'c', lines));
        pause();
        return CommandResult.NO_TICK;
    }

    private static String key(String keys, String what) {
        return String.format("{C}%-6s{/}%s", keys, what);
    }

    private static String rule(String text) {
        return "{k}" + Panel.DOT + "{/} " + text;
    }
}
