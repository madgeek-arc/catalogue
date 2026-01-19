package gr.uoa.di.madgik.catalogue.ui.domain.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import gr.uoa.di.madgik.catalogue.dto.IdLabel;
import gr.uoa.di.madgik.catalogue.ui.domain.DataRequest;
import gr.uoa.di.madgik.catalogue.ui.domain.FieldType;
import gr.uoa.di.madgik.catalogue.ui.domain.TypeInfo;
import gr.uoa.di.madgik.catalogue.ui.domain.types.*;

import java.io.IOException;
import java.util.List;

public class TypeInfoDeserializer extends StdDeserializer<TypeInfo> {

    public TypeInfoDeserializer() {
        super(TypeInfo.class);
    }

    @Override
    public TypeInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        FieldType type = mapper.treeToValue(node.path("type"), FieldType.class);
        String defaultValue = node.path("defaultValue").asText(null);
        List<IdLabel> values;
        try {
            values = mapper.treeToValue(node.path("values"), new TypeReference<>(){});
        } catch (Exception e) {
            List<String> vals = mapper.treeToValue(node.path("values"), new TypeReference<>(){});
            values = vals.stream().map(item -> new IdLabel(item, item)).toList();
        }
        boolean multiplicity = node.path("multiplicity").asBoolean(false);
        DataRequest prefill = mapper.treeToValue(node.path("prefill"), DataRequest.class);
        JsonNode propsNode = node.get("properties");

        String vocabulary = node.path("vocabulary").asText(null);
        TypeProperties properties;

        if (propsNode == null || propsNode.isNull()) { // Backward compatibility: old models don't have "properties"
            properties = switch (type) { // create properties fields with null values for each type
                case number             -> new NumberProperties();
                case string, largeText  -> new TextProperties();
                case date               -> new DateProperties();
                case url                -> new UrlProperties();
                case vocabulary         -> new VocabularyProperties();
                case email, phone       -> new PatternProperties();
                default                 -> new CustomProperties();
            };
        } else {
            properties = switch (type) {
                case number             -> mapper.treeToValue(propsNode, NumberProperties.class);
                case string, largeText  -> mapper.treeToValue(propsNode, TextProperties.class);
                case date               -> mapper.treeToValue(propsNode, DateProperties.class);
                case url                -> mapper.treeToValue(propsNode, UrlProperties.class);
                case vocabulary         -> mapper.treeToValue(propsNode, VocabularyProperties.class);
                case email, phone       -> mapper.treeToValue(propsNode, PatternProperties.class);
                default                 -> mapper.treeToValue(propsNode, CustomProperties.class);
            };
        }
        return new TypeInfo(vocabulary, type, defaultValue, values, properties, multiplicity, prefill);
    }
}
