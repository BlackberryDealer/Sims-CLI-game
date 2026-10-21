package simcli.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small JSON reader for the game's content files, so the game needs no
 * external libraries.
 *
 * <p>{@link #parse(String)} turns JSON text into plain Java values:
 * objects become {@code Map<String, Object>} (keeping key order), arrays
 * {@code List<Object>}, strings {@code String}, numbers {@code Double},
 * {@code true}/{@code false} {@code Boolean}, and {@code null} {@code null}.
 * Errors report the line and column.</p>
 */
public final class Json {

    private final String text;
    private int pos;

    private Json(String text) {
        this.text = text;
    }

    /**
     * Parses a JSON document.
     *
     * @param text the JSON text.
     * @return the parsed value.
     * @throws IllegalArgumentException if the text isn't valid JSON.
     */
    public static Object parse(String text) {
        Json parser = new Json(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos < text.length()) {
            throw parser.error("unexpected text after the end of the document");
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (pos >= text.length()) {
            throw error("unexpected end of file");
        }
        char c = text.charAt(pos);
        switch (c) {
            case '{': return readObject();
            case '[': return readArray();
            case '"': return readString();
            case 't': expectWord("true"); return Boolean.TRUE;
            case 'f': expectWord("false"); return Boolean.FALSE;
            case 'n': expectWord("null"); return null;
            default:
                if (c == '-' || Character.isDigit(c)) {
                    return readNumber();
                }
                throw error("unexpected character '" + c + "'");
        }
    }

    private Map<String, Object> readObject() {
        Map<String, Object> object = new LinkedHashMap<>();
        pos++; // {
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return object;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw error("expected a quoted key");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            object.put(key, readValue());
            skipWhitespace();
            if (peek() == ',') {
                pos++;
            } else if (peek() == '}') {
                pos++;
                return object;
            } else {
                throw error("expected ',' or '}'");
            }
        }
    }

    private List<Object> readArray() {
        List<Object> array = new ArrayList<>();
        pos++; // [
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return array;
        }
        while (true) {
            array.add(readValue());
            skipWhitespace();
            if (peek() == ',') {
                pos++;
            } else if (peek() == ']') {
                pos++;
                return array;
            } else {
                throw error("expected ',' or ']'");
            }
        }
    }

    private String readString() {
        pos++; // opening quote
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= text.length()) {
                throw error("unterminated string");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= text.length()) {
                throw error("unterminated string");
            }
            char esc = text.charAt(pos++);
            switch (esc) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > text.length()) {
                        throw error("bad \\u escape");
                    }
                    sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                    pos += 4;
                    break;
                default:
                    throw error("bad escape '\\" + esc + "'");
            }
        }
    }

    private Double readNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < text.length() && "0123456789.eE+-".indexOf(text.charAt(pos)) >= 0) {
            pos++;
        }
        try {
            return Double.parseDouble(text.substring(start, pos));
        } catch (NumberFormatException e) {
            pos = start;
            throw error("bad number");
        }
    }

    private void expectWord(String word) {
        if (!text.startsWith(word, pos)) {
            throw error("expected '" + word + "'");
        }
        pos += word.length();
    }

    private void expect(char c) {
        if (peek() != c) {
            throw error("expected '" + c + "'");
        }
        pos++;
    }

    private char peek() {
        return pos < text.length() ? text.charAt(pos) : '\0';
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private IllegalArgumentException error(String message) {
        int line = 1;
        int column = 1;
        for (int i = 0; i < Math.min(pos, text.length()); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        return new IllegalArgumentException("JSON error at line " + line + ", column " + column + ": " + message);
    }
}
