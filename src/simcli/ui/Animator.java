package simcli.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Plays short multi-line ASCII animations in place.
 *
 * <p>Frames are redrawn over each other with ANSI cursor movement. When the
 * terminal can't do that, or animations are switched off (and always in
 * tests / piped output), only the final frame is printed, with no delay.</p>
 */
public final class Animator {

    private Animator() {
    }

    /**
     * Plays the frames once.
     *
     * @param frames  frames as tagged strings; lines separated by {@code \n}.
     * @param delayMs time each frame stays on screen.
     */
    public static void play(List<String> frames, long delayMs) {
        if (frames.isEmpty()) {
            return;
        }
        List<List<String>> normalized = normalize(frames);
        boolean animate = Term.isAnimationsEnabled() && Term.isColorEnabled() && frames.size() > 1;

        if (!animate) {
            printFrame(normalized.get(normalized.size() - 1));
            return;
        }

        int height = normalized.get(0).size();
        for (int i = 0; i < normalized.size(); i++) {
            if (i > 0) {
                Term.cursorUp(height);
            }
            printFrame(normalized.get(i));
            System.out.flush();
            Term.pause(delayMs);
        }
    }

    /** Builds frames that slide {@code sprite} from left to right across the screen. */
    public static List<String> slide(String sprite, String ground, int steps, int maxOffset) {
        List<String> frames = new ArrayList<>();
        String[] lines = sprite.split("\n", -1);
        for (int s = 0; s <= steps; s++) {
            int offset = maxOffset * s / steps;
            StringBuilder frame = new StringBuilder();
            for (String line : lines) {
                frame.append(" ".repeat(offset)).append(line).append('\n');
            }
            frame.append(ground);
            frames.add(frame.toString());
        }
        return frames;
    }

    private static void printFrame(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append('\n');
        }
        System.out.print(sb);
    }

    /** Renders tags and pads every frame to the same height and width, so redraws leave no residue. */
    private static List<List<String>> normalize(List<String> frames) {
        int height = 0;
        int width = 0;
        List<String[]> split = new ArrayList<>();
        for (String f : frames) {
            String[] lines = f.split("\n", -1);
            split.add(lines);
            height = Math.max(height, lines.length);
            for (String line : lines) {
                width = Math.max(width, Style.visibleLength(line));
            }
        }
        width = Math.min(width, Term.WIDTH);

        List<List<String>> out = new ArrayList<>();
        for (String[] lines : split) {
            List<String> frame = new ArrayList<>();
            for (int i = 0; i < height; i++) {
                String line = i < lines.length ? Style.render(lines[i]) : "";
                frame.add(Style.fit(line, width));
            }
            out.add(frame);
        }
        return out;
    }
}
