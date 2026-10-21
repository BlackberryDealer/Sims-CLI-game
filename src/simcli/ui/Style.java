package simcli.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colour markup and ANSI-aware text helpers.
 *
 * <p>Text (including ASCII art) can carry lightweight colour tags such as
 * {@code {g}grass{/}}. {@link #render(String)} turns the tags into ANSI
 * escapes when colours are on, and strips them when they're off, so the
 * same strings work everywhere.</p>
 *
 * <table>
 *   <caption>Tags</caption>
 *   <tr><td>{r} {g} {y} {b} {m} {c} {w}</td><td>red, green, yellow, blue, magenta, cyan, white</td></tr>
 *   <tr><td>{R} {G} {Y} {B} {M} {C} {W}</td><td>bright variants</td></tr>
 *   <tr><td>{k}</td><td>grey (dim)</td></tr>
 *   <tr><td>{*}</td><td>bold</td></tr>
 *   <tr><td>{/}</td><td>reset</td></tr>
 * </table>
 */
public final class Style {

    private static final Map<Character, String> CODES = new HashMap<>();
    static {
        String[][] table = {
                {"r", "31"}, {"g", "32"}, {"y", "33"}, {"b", "34"}, {"m", "35"}, {"c", "36"}, {"w", "37"},
                {"R", "91"}, {"G", "92"}, {"Y", "93"}, {"B", "94"}, {"M", "95"}, {"C", "96"}, {"W", "97"},
                {"k", "90"}, {"*", "1"}, {"/", "0"},
        };
        for (String[] row : table) {
            CODES.put(row[0].charAt(0), "\u001b[" + row[1] + "m");
        }
    }

    private static final Pattern TAG = Pattern.compile("\\{([rgybmcwRGYBMCWk*/])\\}");
    private static final Pattern ANSI = Pattern.compile("\u001b\\[[0-9;]*[A-Za-z]");

    private Style() {
    }

    /** Converts colour tags to ANSI escapes (or strips them when colours are off). */
    public static String render(String markup) {
        if (markup == null || markup.indexOf('{') < 0) {
            return markup;
        }
        Matcher m = TAG.matcher(markup);
        StringBuilder sb = new StringBuilder();
        boolean color = Term.isColorEnabled();
        while (m.find()) {
            m.appendReplacement(sb, color ? Matcher.quoteReplacement(CODES.get(m.group(1).charAt(0))) : "");
        }
        m.appendTail(sb);
        if (color) {
            sb.append(CODES.get('/'));
        }
        return sb.toString();
    }

    /** Wraps {@code text} in a single colour tag and renders it. */
    public static String paint(String text, char code) {
        return render("{" + code + "}" + text + "{/}");
    }

    /** Removes colour tags and ANSI escapes, leaving only visible text. */
    public static String plain(String text) {
        return ANSI.matcher(TAG.matcher(text).replaceAll("")).replaceAll("");
    }

    /** Number of columns {@code text} occupies on screen. */
    public static int visibleLength(String text) {
        return plain(text).length();
    }

    /**
     * Pads (or truncates) rendered or tagged text to exactly {@code width}
     * visible columns. Truncated text loses its colours and ends in "..".
     */
    public static String fit(String text, int width) {
        int len = visibleLength(text);
        if (len == width) {
            return text;
        }
        if (len < width) {
            return text + " ".repeat(width - len);
        }
        String plain = plain(text);
        return width <= 2 ? plain.substring(0, width) : plain.substring(0, width - 2) + "..";
    }

    /** Centres text within {@code width} visible columns. */
    public static String center(String text, int width) {
        int len = visibleLength(text);
        if (len >= width) {
            return fit(text, width);
        }
        int left = (width - len) / 2;
        return " ".repeat(left) + text + " ".repeat(width - len - left);
    }

    /** Colour code for a 0-100 value: green when healthy, yellow when low, red when critical. */
    public static char levelColor(int value) {
        if (value >= 60) return 'G';
        if (value >= 30) return 'Y';
        return 'R';
    }
}
