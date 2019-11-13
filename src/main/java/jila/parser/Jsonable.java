package jila.parser;

import java.util.Map;

import javax.json.JsonObject;

public interface Jsonable {
    public JsonObject toJsonObject();
    public JsonObject toJsonObject(Map<String, String> attributes);
}
