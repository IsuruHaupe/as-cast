package fr.imtatlantique.simulation.Utils;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {
    //Convert JSON into Object
    static public <T> T covertFromJsonToObject(String json, Class<T> var) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, var);//Convert Json into object of Specific Type
    }

    //convert Object into JSON
    public static String covertFromObjectToJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }
}
