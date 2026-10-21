package simcli.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Draws the boxes, bars and menu rows that every screen is built from.
 *
 * <p>All output is {@link Term#WIDTH} columns wide. Line-drawing glyphs fall
 * back to ASCII on consoles that can't print them (see {@link Term#glyph}).</p>
 */
public final class Panel {

    // Single-line frame
    static final String H = Term.glyph("\u2500", "-");
    static final String V = Term.glyph("\u2502", "|");
    static final String TL = Term.glyph("\u250c", "+");
    static final String TR = Term.glyph("\u2510", "+");
    static final String BL = Term.glyph("\u2514", "+");
    static final String BR = Term.glyph("\u2518", "+");
    static final String LT = Term.glyph("\u251c", "+");
    static final String RT = Term.glyph("\u2524", "+");

    // Double-line frame
    static final String DH = Term.glyph("\u2550", "=");
    static final String DV = Term.glyph("\u2551", "|");
    static final String DTL = Term.glyph("\u2554", "+");
    static final String DTR = Term.glyph("\u2557", "+");
    static final String DBL = Term.glyph("\u255a", "+");
    static final String DBR = Term.glyph("\u255d", "+");

    // Bars and symbols
    static final String FULL = Term.glyph("\u2588", "#");
    static final String EMPTY = Term.glyph("\u2591", ".");
    public static final String DOT = Term.glyph("\u00b7", "-");
    public static final String ARROW = Term.glyph("\u25ba", ">");
    public static final String HEART = Term.glyph("\u2665", "<3");
    public static final String STAR = Term.glyph("\u2605", "*");
    public static final String SUN = Term.glyph("\u263c", "*");
    public static final String MOON = Term.glyph("\u263e", "C");
    public static final String CHECK = Term.glyph("\u2714", "+");
    public static final String CROSS = Term.glyph("\u2718", "x");
    public static final String WARN = Term.glyph("\u25b2", "!");

    /** Columns available for content inside a box ({@code "│ " + content + " │"}). */
    public static final int INNER = Term.WIDTH - 4;

    private Panel() {
    }

    /**
     * Draws a single-line box with a title in its top border.
     *
     * @param title  title text (tags allowed), or {@code null} for none.
     * @param color  colour code for the frame, e.g. {@code 'c'}.
     * @param lines  content lines (tags allowed); each is padded/truncated.
     */
    public static String box(String title, char color, List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append(topBorder(title, color, TL, H, TR)).append('\n');
        for (String line : lines) {
            sb.append(row(line, color)).append('\n');
        }
        sb.append(Style.paint(BL + H.repeat(Term.WIDTH - 2) + BR, color));
        return sb.toString();
    }

    /** Convenience overload of {@link #box(String, char, List)}. */
    public static String box(String title, char color, String... lines) {
        return box(title, color, Arrays.asList(lines));
    }

    /** Draws a double-line banner with left- and right-aligned text. */
    public static String banner(String left, String right, char color) {
        int gap = Term.WIDTH - 4 - Style.visibleLength(left) - Style.visibleLength(right);
        String middle = gap > 0 ? left + " ".repeat(gap) + right : Style.fit(left, Term.WIDTH - 4);
        return Style.paint(DTL + DH.repeat(Term.WIDTH - 2) + DTR, color) + '\n'
                + Style.paint(DV, color) + " " + Style.render(middle) + " " + Style.paint(DV, color) + '\n'
                + Style.paint(DBL + DH.repeat(Term.WIDTH - 2) + DBR, color);
    }

    /** A divider line with an optional caption, e.g. {@code ── Events ────}. */
    public static String rule(String caption, char color) {
        if (caption == null || caption.isEmpty()) {
            return Style.paint(H.repeat(Term.WIDTH), color);
        }
        String head = H.repeat(2) + " ";
        int rest = Term.WIDTH - 3 - Style.visibleLength(caption) - 1;
        return Style.paint(head, color) + Style.render(caption) + " " + Style.paint(H.repeat(Math.max(0, rest)), color);
    }

    /**
     * A need bar like {@code ██████░░░░  62}, coloured by level.
     *
     * @param value current value (0-100).
     * @param width number of bar cells.
     */
    public static String bar(int value, int width) {
        int clamped = Math.max(0, Math.min(100, value));
        return gauge(clamped, width) + Style.paint(String.format(" %3d", clamped), Style.levelColor(clamped));
    }

    /** A bar without the number, coloured by level. */
    public static String gauge(int value, int width) {
        int clamped = Math.max(0, Math.min(100, value));
        int filled = (int) Math.round(clamped * width / 100.0);
        return Style.paint(FULL.repeat(filled), Style.levelColor(clamped))
                + Style.paint(EMPTY.repeat(width - filled), 'k');
    }

    /** A labelled need bar, e.g. {@code Hunger   ██████░░░░  62}. */
    public static String meter(String label, int value, int width) {
        String marker = value < 30 ? Style.paint(WARN, 'R') : " ";
        return Style.fit(label, 9) + bar(value, width) + " " + marker;
    }

    /** A menu option: highlighted key, label, and optional greyed hint. */
    public static String option(String key, String label, String hint) {
        String k = Style.render("{C}{*}[" + key + "]{/}");
        String text = k + " " + label;
        if (hint != null && !hint.isEmpty()) {
            text += Style.paint("  " + hint, 'k');
        }
        return text;
    }

    /** A menu option without a hint. */
    public static String option(String key, String label) {
        return option(key, label, null);
    }

    /** Lays out short cells (e.g. menu options) across rows of a fixed column width. */
    public static List<String> columns(List<String> cells, int columnWidth) {
        List<String> rows = new ArrayList<>();
        int perRow = Math.max(1, INNER / columnWidth);
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            boolean last = (i % perRow == perRow - 1) || i == cells.size() - 1;
            row.append(last ? cells.get(i) : Style.fit(cells.get(i), columnWidth));
            if (last) {
                rows.add(row.toString());
                row.setLength(0);
            }
        }
        return rows;
    }

    private static String row(String content, char color) {
        return Style.paint(V, color) + " " + Style.fit(Style.render(content), INNER) + " " + Style.paint(V, color);
    }

    private static String topBorder(String title, char color, String left, String line, String right) {
        if (title == null || title.isEmpty()) {
            return Style.paint(left + line.repeat(Term.WIDTH - 2) + right, color);
        }
        String rendered = Style.render(title);
        int used = 2 + 1 + Style.visibleLength(rendered) + 1;
        int rest = Term.WIDTH - used - 1;
        if (rest < 1) {
            rendered = Style.fit(rendered, Term.WIDTH - 6);
            rest = 1;
        }
        return Style.paint(left + line, color) + " " + rendered + " " + Style.paint(line.repeat(rest) + right, color);
    }
}
