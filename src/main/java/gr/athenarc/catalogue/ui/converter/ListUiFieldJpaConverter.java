package gr.athenarc.catalogue.ui.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.athenarc.catalogue.ui.domain.UiField;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.List;

@Converter(autoApply = true)
public class ListUiFieldJpaConverter implements AttributeConverter<List<UiField>, String> {

    private static final Logger logger = LogManager.getLogger(ListUiFieldJpaConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(List<UiField> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            logger.warn("Could not convert attribute to json");
            return null;
        }
    }

    @Override
    public List<UiField> convertToEntityAttribute(String dbData) {
        try {
            return (List<UiField>) objectMapper.readValue(dbData, List.class);
        } catch (IOException ex) {
            logger.error("Error while decoding json from database: " + dbData);
            return null;
        }
    }
}
