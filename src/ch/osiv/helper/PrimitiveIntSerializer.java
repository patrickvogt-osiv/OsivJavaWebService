package ch.osiv.helper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * PrimitiveIntSerializer class
 *
 * @author Arno van der Ende
 */
public class PrimitiveIntSerializer
    extends JsonSerializer<Integer> {

    @Override
    public void serialize(Integer value,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        if (value != null && value != 0) {
            gen.writeNumber(value);
        } else {
            gen.writeNull();
        }
    }

}
