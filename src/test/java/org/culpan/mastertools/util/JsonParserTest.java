package org.culpan.mastertools.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class JsonParserTest {
    @Test
    public void testJsonParser1() throws Exception {
        JsonParser parser = new JsonParser();
        JsonParser.JsonBase jsonBase = parser.parse("[{" +
                "   \"hello\":1," +
                "   \"test\": \"value\", " +
                "   \"new_obj\": { \"text\":\"Hello there\" }" +
                "}]");

        for (String s : parser.getErrors()) {
            System.out.println(String.format("Error: %s", s));
        }

        assertEquals(0, parser.getErrors().size());
        assertTrue(jsonBase instanceof JsonParser.JsonArray);

        JsonParser.JsonArray array = (JsonParser.JsonArray) jsonBase;
        jsonBase = ((JsonParser.JsonArray) jsonBase).get(0);

        JsonParser.JsonValue value = (JsonParser.JsonValue)((JsonParser.JsonObject) jsonBase).getProperty("hello");
        assertEquals("1", value.getValue());
        value = (JsonParser.JsonValue)((JsonParser.JsonObject) jsonBase).getProperty("test");
        assertEquals("value", value.getValue());

        JsonParser.JsonObject object = (JsonParser.JsonObject)((JsonParser.JsonObject) jsonBase).getProperty("new_obj");
        value = (JsonParser.JsonValue)object.getProperty("text");
        assertEquals("Hello there", value.getValue());
    }

    @Test
    public void testJsonParser2() throws Exception {
        JsonParser parser = new JsonParser();
        JsonParser.JsonBase jsonBase = parser.parse(
                "[{" +
                "   \"num\":1," +
                "   \"name\": \"value\", " +
                "   \"new_obj\": { \"text\":\"Hello there\" }" +
                "}, {" +
                        "\"num\":2," +
                        "\"name\":\"dino\", " +
                        "\"test-array\": [ { \"test-key\":\"test value\" }]" +
                        "}]");

        for (String s : parser.getErrors()) {
            System.out.println(String.format("Error: %s", s));
        }

        assertEquals(0, parser.getErrors().size());
        assertTrue(jsonBase instanceof JsonParser.JsonArray);
        JsonParser.JsonArray array = (JsonParser.JsonArray) jsonBase;

        assertEquals(2, array.size());
        jsonBase = array.get(0);

        JsonParser.JsonValue value = (JsonParser.JsonValue)((JsonParser.JsonObject) jsonBase).getProperty("num");
        assertEquals("1", value.getValue());
        value = (JsonParser.JsonValue)((JsonParser.JsonObject) jsonBase).getProperty("name");
        assertEquals("value", value.getValue());

        JsonParser.JsonObject object = (JsonParser.JsonObject)((JsonParser.JsonObject) jsonBase).getProperty("new_obj");
        value = (JsonParser.JsonValue)object.getProperty("text");
        assertEquals("Hello there", value.getValue());

        jsonBase = array.get(1);

        value = (JsonParser.JsonValue)((JsonParser.JsonObject) jsonBase).getProperty("num");
        assertEquals("2", value.getValue());
        value = (JsonParser.JsonValue)((JsonParser.JsonObject) jsonBase).getProperty("name");
        assertEquals("dino", value.getValue());
    }

    @Test
    public void testJsonParser3() throws Exception {
        JsonParser parser = new JsonParser();
        JsonParser.JsonBase jsonBase = parser.parse(
                "{\"dmg_res\":[]}");

        for (String s : parser.getErrors()) {
            System.out.println(String.format("Error: %s", s));
        }

        assertEquals(0, parser.getErrors().size());
    }

    @Test
    public void testJsonParser4() throws Exception {
        JsonParser parser = new JsonParser();
        JsonParser.JsonBase jsonBase = parser.parse(
                "{\"dmg_res\":{}}");

        for (String s : parser.getErrors()) {
            System.out.println(String.format("Error: %s", s));
        }

        assertEquals(0, parser.getErrors().size());
    }

    private String readFile(String filePath) throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(getClass().getResource(filePath).toURI())));
    }

    @Test
    public void testReadingFile() throws Exception {
        String json = readFile("/5e-SRD-Monsters.json");
        JsonParser parser = new JsonParser();
        JsonParser.JsonBase base = parser.parse(json);

        for (String s : parser.getErrors()) {
            System.out.println(String.format("Error: %s", s));
        }

        assertNotNull(base);
        assertEquals(0, parser.getErrors().size());
        assertTrue(base instanceof JsonParser.JsonArray);
        JsonParser.JsonArray baseArray = (JsonParser.JsonArray)base;

        for (int i = 0; i < baseArray.size(); i++) {
            JsonParser.JsonObject object = (JsonParser.JsonObject)baseArray.get(i);
            JsonParser.JsonValue name = (JsonParser.JsonValue)object.getProperty("name");
            System.out.println(String.format("%d : %s", i + 1, name.getValue()));
        }
    }

    @Test
    public void testReadingFile2() throws Exception {
        String json = readFile("/test.json");
        JsonParser parser = new JsonParser();
        JsonParser.JsonBase base = parser.parse(json);

        for (String s : parser.getErrors()) {
            System.out.println(String.format("Error: %s", s));
        }

        assertNotNull(base);
        assertEquals(0, parser.getErrors().size());
    }
}