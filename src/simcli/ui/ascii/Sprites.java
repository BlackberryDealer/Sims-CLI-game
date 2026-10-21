package simcli.ui.ascii;

import simcli.entities.actors.Sim;
import simcli.ui.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared building blocks for the ASCII scenes: the Sim character (with a
 * mood-coloured "plumbob" floating above its head) and a helper that sets
 * two pieces of art side by side.
 */
public final class Sprites {

    private Sprites() {
    }

    /**
     * Colour tag for the Sim's plumbob, based on their average needs:
     * green when content, yellow when needs are slipping, red when in trouble.
     */
    public static char moodColor(Sim sim) {
        int avg = (sim.getHunger().getValue() + sim.getEnergy().getValue() + sim.getHygiene().getValue()
                + sim.getHappiness().getValue() + sim.getSocial().getValue()) / 5;
        if (sim.getHealth() < 40) {
            return 'R';
        }
        return Style.levelColor(avg);
    }

    /**
     * The Sim's head, 11 columns wide and 7 rows tall.
     *
     * @param sim   the Sim (decides the plumbob colour).
     * @param eyes  one character used for both eyes, e.g. {@code 'o'} or {@code '-'}.
     * @param mouth exactly three characters, e.g. {@code "\\_/"}.
     */
    public static String head(Sim sim, char eyes, String mouth) {
        char p = moodColor(sim);
        return "    {" + p + "}/\\{/}     \n"
                + "    {" + p + "}\\/{/}     \n"
                + "  {w}.-\"\"\"-.{/}  \n"
                + " {w}/ " + eyes + "   " + eyes + " \\{/} \n"
                + "{w}|    ^    |{/}\n"
                + " {w}\\  " + mouth + "  /{/} \n"
                + "  {w}'-...-'{/}  ";
    }

    /**
     * Places {@code right} to the right of {@code left}, top-aligned,
     * {@code gap} columns apart. Both may contain colour tags.
     */
    public static String beside(String left, String right, int gap) {
        String[] l = left.split("\n", -1);
        String[] r = right.split("\n", -1);
        int leftWidth = 0;
        for (String line : l) {
            leftWidth = Math.max(leftWidth, Style.visibleLength(line));
        }
        int rows = Math.max(l.length, r.length);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            String a = i < l.length ? l[i] : "";
            String b = i < r.length ? r[i] : "";
            String padded = a + " ".repeat(leftWidth - Style.visibleLength(a) + gap);
            out.add(b.isEmpty() ? padded.stripTrailing() : padded + b);
        }
        return String.join("\n", out);
    }

    /** Shifts every line of {@code art} right by {@code columns} spaces. */
    public static String indent(String art, int columns) {
        String pad = " ".repeat(columns);
        StringBuilder sb = new StringBuilder();
        String[] lines = art.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i].isEmpty() ? "" : pad + lines[i]);
        }
        return sb.toString();
    }

    /** A caption line under a scene, e.g. "  Zed is fast asleep". */
    public static String caption(String text) {
        return "\n  {k}~{/} " + text + " {k}~{/}";
    }
}
