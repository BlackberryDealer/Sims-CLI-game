package simcli.ui;

import simcli.ui.ascii.Sprites;

import java.util.ArrayList;
import java.util.List;

/**
 * AsciiArt — utility class for one-off art not tied to a Sim action or
 * location: the title logo, the travel and sleep animations, and the
 * memorial shown when a Sim dies.
 *
 * <p>
 * Location and action ASCII art lives exclusively in the
 * {@code simcli.ui.ascii.providers} package, rendered through
 * {@link simcli.ui.ascii.AsciiEngine}. Screen clearing is handled exclusively
 * by {@link UIManager#clearScreen()}.
 * </p>
 */
public class AsciiArt {

    private static final String[] PLUMBOB = {
            "    /\\    ",
            "   /  \\   ",
            "  /    \\  ",
            "  \\    /  ",
            "   \\  /   ",
            "    \\/    ",
    };

    private static final String[][] SIM_LETTERS = {
            {"  ____  ", " / ___| ", " \\___ \\ ", "  ___) |", " |____/ "},
            {" _  ", "(_) ", "| | ", "| | ", "|_| "},
            {"            ", " _ __ ___   ", "| '_ ` _ \\  ", "| | | | | | ", "|_| |_| |_| "},
    };

    private static final String[][] CLI_LETTERS = {
            {"  ____  ", " / ___| ", "| |     ", "| |___  ", " \\____| "},
            {" _      ", "| |     ", "| |     ", "| |___  ", "|_____| "},
            {" ___  ", "|_ _| ", " | |  ", " | |  ", "|___| "},
    };

    /** Prints the title logo: a glowing plumbob beside "SimCLI". */
    public static void printLogo() {
        StringBuilder sb = new StringBuilder("\n");
        for (int row = 0; row < PLUMBOB.length; row++) {
            int letterRow = row - 1; // letters sit one row lower than the plumbob tip
            StringBuilder sim = new StringBuilder();
            StringBuilder cli = new StringBuilder();
            if (letterRow >= 0 && letterRow < 5) {
                for (String[] letter : SIM_LETTERS) sim.append(letter[letterRow]);
                for (String[] letter : CLI_LETTERS) cli.append(letter[letterRow]);
            }
            sb.append("    {G}").append(PLUMBOB[row]).append("{/}  ")
                    .append("{G}{*}").append(sim).append("{/}")
                    .append("{C}{*}").append(cli).append("{/}\n");
        }
        sb.append("                 {k}~ live, work and love - right in your terminal ~{/}\n");
        System.out.print(Style.render(sb.toString()));
    }

    /**
     * A little car drives across the screen on the way to {@code destination}.
     *
     * @param destination where the Sim is headed (shown above the road).
     */
    public static void printTravelAnimation(String destination) {
        UIManager.printMessage("\n  {k}Heading to{/} {W}" + destination + "{/}{k}...{/}\n");
        String car = "    {R}______{/}\n"
                + " {R}__/__{C}||{R}__\\___{/}\n"
                + "{R}'-({k}o{R})-----({k}o{R})-'{/}";
        String road = "{k}" + "- ".repeat(36) + "{/}";
        Animator.play(Animator.slide(car, road, 14, 56), 60);
    }

    /**
     * Night sky with twinkling stars, then a sunrise.
     *
     * @param name  the sleeping Sim's name.
     * @param hours how long they sleep.
     */
    public static void printSleepAnimation(String name, int hours) {
        String[][] stars = {
                {"   {W}*{/}        {k}.{/}          {W}*{/}         {k}.{/}       {Y}( ){/}",
                        "        {k}.{/}         {W}*{/}         {k}.{/}        {W}*{/}",
                        "  {k}.{/}        {W}*{/}         {k}.{/}          {W}*{/}        {k}.{/}"},
                {"   {k}.{/}        {W}*{/}          {k}.{/}         {W}*{/}       {Y}( ){/}",
                        "        {W}*{/}         {k}.{/}         {W}*{/}        {k}.{/}",
                        "  {W}*{/}        {k}.{/}         {W}*{/}          {k}.{/}        {W}*{/}"},
        };
        String house = "\n          {b}_____{/}\n         {b}/     \\{/}      {C}" + name + " is sleeping for " + hours
                + (hours == 1 ? " hour" : " hours") + "...{/}\n         {b}|{Y}[]{b} {Y}[]{b}|{/}"
                + "\n  {k}" + "_".repeat(50) + "{/}";
        String sunrise = "   {Y}\\   |   /{/}\n"
                + "     {Y}.-'''-.{/}           {W}Good morning, " + name + "!{/}\n"
                + "  {Y}--(       )--{/}\n"
                + "{y}" + "~".repeat(54) + "{/}";
        String morningHouse = "\n          {b}_____{/}\n         {b}/     \\{/}      {G}Rise and shine!{/}\n"
                + "         {b}|{k}[]{b} {k}[]{b}|{/}\n  {k}" + "_".repeat(50) + "{/}";

        List<String> frames = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            frames.add(String.join("\n", stars[i % 2]) + house);
        }
        frames.add(sunrise + morningHouse);
        Animator.play(frames, 280);
    }

    /**
     * A gravestone for a Sim who has passed away.
     *
     * @param name the Sim's name.
     * @param age  the age they reached.
     */
    public static String tombstone(String name, int age) {
        String shownName = name.length() > 13 ? name.substring(0, 13) : name;
        String stone = "       {w}_______________{/}\n"
                + "      {w}/               \\{/}\n"
                + "     {w}/     R . I . P   \\{/}\n"
                + "     {w}|" + Style.center("{W}" + shownName + "{w}", 17) + "|{/}\n"
                + "     {w}|" + Style.center("aged " + age, 17) + "|{/}\n"
                + "     {w}|  {k}gone but not{w}   |{/}\n"
                + "     {w}|  {k}forgotten{w}      |{/}\n"
                + "   {G},,{w}|_________________|{G},,,{/}";
        return Sprites.indent(stone, 4);
    }
}
