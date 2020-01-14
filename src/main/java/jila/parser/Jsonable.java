package jila.parser;

import java.util.Map;

import javax.json.JsonObject;

/**
 * Interface used to serialize object into json object.
 * TODO: remove
 */
public interface Jsonable {
    JsonObject toJsonObject();
    JsonObject toJsonObject(Map<String, String> attributes);
}
