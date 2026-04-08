/*
 * Copyright 2025-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.domain.jackson;

import gr.uoa.di.madgik.catalogue.domain.types.*;
import gr.uoa.di.madgik.catalogue.dto.IdLabel;
import gr.uoa.di.madgik.catalogue.domain.DataRequest;
import gr.uoa.di.madgik.catalogue.domain.FieldType;
import gr.uoa.di.madgik.catalogue.domain.TypeInfo;
import gr.uoa.di.madgik.catalogue.domain.types.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class TypeInfoDeserializer extends StdDeserializer<TypeInfo> {

    public TypeInfoDeserializer() {
        super(TypeInfo.class);
    }

    @Override
    public TypeInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = (JsonNode) p.readValueAsTree();

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
                case number             -> {
                    NumberProperties numberProperties = new NumberProperties();
                    if (values != null && !values.isEmpty()) {
                        IdLabel value = values.getFirst();
                        String decimals = value.id().split("\\.")[1];
                        numberProperties.setDecimals(decimals.length());
                        values = null;

                    }
                    yield numberProperties;
                }
                case string, largeText  -> new TextProperties();
                case date               -> new DateProperties();
                case url                -> new UrlProperties();
                case vocabulary         -> {
                    VocabularyProperties vocabularyProperties = new VocabularyProperties();
                    if (vocabulary != null) {
                        vocabularyProperties.setUrl(URI.create("/api/vocabularies-endpoint/" + vocabulary));
                        vocabularyProperties.setLabelField("name");
                        yield vocabularyProperties;
                    } else {
                        type = FieldType.select;
                        yield null;
                    }
                }
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
