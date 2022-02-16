package gr.athenarc.catalogue.ui.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.athenarc.catalogue.ui.domain.Form;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class FormJpaConverter implements AttributeConverter<Form, String> {

    private static final Logger logger = LogManager.getLogger(FormJpaConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(Form attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            logger.warn("Could not convert attribute to json");
            return null;
        }
    }

    @Override
    public Form convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, Form.class);
        } catch (IOException ex) {
            logger.error("Error while decoding json from database: " + dbData);
            return null;
        }
    }
}
