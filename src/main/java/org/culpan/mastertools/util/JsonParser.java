package org.culpan.mastertools.util;

import java.util.*;

public class JsonParser {
    public class JsonBase {
        public boolean isJsonObject() {
            return this instanceof JsonObject;
        }

        public boolean isJsonArray() {
            return this instanceof JsonArray;
        }

        public boolean isJsonValue() {
            return this instanceof JsonValue;
        }
    }

    public class JsonArray extends JsonBase {
        private final List<JsonBase> array = new ArrayList<>();

        public JsonBase get(int index) {
            return array.get(index);
        }

        public List<JsonBase> getAll() {
            return array;
        }

        public int size() {
            return array.size();
        }

        public void add(JsonBase o) {
            array.add(o);
        }
    }

    public class JsonObject extends JsonBase {
        private final Map<String, JsonBase> properties = new HashMap<>();

        private int startingLoc;

        private int endingLoc;

        public void setProperty(String key, JsonBase value) {
            properties.put(key, value);
        }

        public JsonBase getProperty(String key) {
            return properties.get(key);
        }

        public boolean exists(String key) {
            return getProperty(key) != null;
        }

        public String getPropertyValue(String key) {
            JsonBase o = getProperty(key);
            if (o != null && o.isJsonValue()) {
                return ((JsonValue)o).getValue();
            }
            return null;
        }

        public int getStartingLoc() {
            return startingLoc;
        }

        public void setStartingLoc(int startingLoc) {
            this.startingLoc = startingLoc;
        }

        public int getEndingLoc() {
            return endingLoc;
        }

        public void setEndingLoc(int endingLoc) {
            this.endingLoc = endingLoc;
        }
    }

    public class JsonValue extends JsonBase {
        private String value;

        public JsonValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private int currLocation = 0;

    private String json;

    private final List<String> errors = new ArrayList<>();

    private String tokenPushback = null;

    private int lineCount = 1;

    public List<String> getErrors() {
        return errors;
    }

    public JsonParser() {

    }

    public JsonBase parse(String json) {
        this.json = json;
        lineCount = 1;
        currLocation = 0;
        tokenPushback = null;
        errors.clear();

        String firstToken = getNextToken();
        if (firstToken.equalsIgnoreCase("[")) {
            return parseArray();
        } else if (firstToken.equalsIgnoreCase("{")) {
            return parseObject();
        } else {
            return null;
        }
    }

    private JsonBase parseObject() {
        JsonObject result = new JsonObject();
        result.setStartingLoc(currLocation - 1);

        String next = getNextToken();

        if (next != null && !next.equalsIgnoreCase("}")) {
            while (next != null) {
                parseProperty(result, next);
                if (!swallow(",", false)) {
                    break;
                }
                next = getNextToken();
            }
        } else {
            pushback(next);
        }

        swallow("}", true);
        result.setEndingLoc(currLocation - 1);

        return result;
    }

    private void parseProperty(JsonObject object, String next) {
        String key = next;

        if (!swallow(":", true)) {
            return;
        }

        String value = getNextToken();
        if (value == null) {
            errors.add("Expected value for property '" + key + "'; found EOL");
        } else if (value.equalsIgnoreCase("[")) {
            object.setProperty(key, parseArray());
        } else if (value.equalsIgnoreCase("{")) {
            object.setProperty(key, parseObject());
        } else {
            object.setProperty(key, new JsonValue(value));
        }
    }

    private JsonBase parseArray() {
        JsonArray result = new JsonArray();
        String next = getNextToken();

        while (true) {
            if (next.equalsIgnoreCase("[")) {
                result.add(parseArray());
            } else if (next.equalsIgnoreCase("{")) {
                result.add(parseObject());
            } else if (next.equalsIgnoreCase("]")) {
                pushback(next);
            }

            if (!swallow(",", false)) break;
            next = getNextToken();
        }

        swallow("]", true);

        return result;
    }

    private String peek() {
        String v = getNextToken();
        pushback(v);
        return v;
    }

    private boolean swallow(String token, boolean logError) {
        String next = getNextToken();
        if (next == null) {
            if (logError) errors.add(String.format("Expected '%s'; found EOL at line %d", token, lineCount));
            return false;
        } else if (!next.equalsIgnoreCase(token)) {
            if (logError) errors.add(String.format("Expected '%s'; found '%s' at line %d", token, next, lineCount));
            pushback(next);
            return false;
        } else {
            return true;
        }
    }

    private void pushback(String token) {
        tokenPushback = token;
    }

    private char peekNextChar() {
        if (currLocation + 1>= json.length()) {
            return '\0';
        } else {
            return json.charAt(currLocation + 1);
        }
    }

    private String getNextToken() {
        if (tokenPushback != null) {
            String result = tokenPushback;
            tokenPushback = null;
            return result;
        }

        boolean inQuotes = false;
        String currBuffer = "";
        while (currLocation < json.length()) {
            char c = json.charAt(currLocation);
            if (c == '"' && inQuotes) {
                currLocation += 1;
                return currBuffer;
            } else if (c == '"' && currBuffer.isEmpty()) {
                currLocation += 1;
                inQuotes = true;
            } else if (c == '\\' && peekNextChar() == '"') {
                currBuffer += '\"';
                currLocation += 2;
            } else if (c == '\\' && peekNextChar() == 'n') {
                currBuffer += '\n';
                currLocation += 2;
            } else if (c == '"') {
                return currBuffer;
            } else if (inQuotes) {
                currBuffer += Character.toString(c);
                currLocation += 1;
            } else if (c == 10) {
                lineCount++;
                currLocation += 1;
            } else if (Character.isWhitespace(c)) {
                currLocation += 1;
            } else if ((c == '[' || c == ']' || c == '{' || c == '}' || c == ',' || c == ':') && !currBuffer.isEmpty()) {
                return currBuffer;
            } else if (c == '[' || c == ']' || c == '{' || c == '}' || c == ',' || c == ':') {
                currLocation += 1;
                return Character.toString(c);
            } else {
                currBuffer += Character.toString(c);
                currLocation += 1;
            }
        }

        if (!currBuffer.isEmpty()) {
            return currBuffer;
        } else {
            return null;
        }
    }
}
