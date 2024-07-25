package ch.osiv.helper;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JsonSerializable class
 *
 * @author Arno van der Ende
 */
public class JsonSerializable {

    /**
     * Deserializes into this object
     *
     * @param The Json string respresentation of the object
     */
    public void deserialize(String json) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.readerForUpdating(this).readValue(json);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserializes into object
     * For example use as:
     * param = Parameter.deserialize(jsonString, Parameter.class)
     *
     * @param The Json string respresentation of the object
     * @return Json string representation of this object
     */
    public static <T extends JsonSerializable> T deserialize(String json,
                                                             Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Serializes this object
     *
     * @return Json string representation of this object
     */
    public String serialize() {

        return JsonSerializable.serialize(this);

    }

    /**
     * Serializes the given object
     *
     * @param obj The object to serialize
     * @return Json string representation of this object
     */
    public static String serialize(Object obj) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Serializes this object into a file (formatted)
     *
     * @param fileName The filename to store the JSON in
     * @return Json string representation of this object
     * @throws IOException
     */
    public void serialize(String fileName) throws IOException {

        JsonSerializable.serialize(this, fileName);

    }

    /**
     * Serializes this object into a file (formatted)
     *
     * @param obj      The object to serialize
     * @param fileName The filename to store the JSON in
     * @return Json string representation of this object
     * @throws IOException
     */
    public static void serialize(Object obj,
                                 String fileName) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), obj);
    }

}
