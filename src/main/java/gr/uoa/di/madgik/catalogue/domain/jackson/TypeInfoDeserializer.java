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

import gr.uoa.di.madgik.catalogue.dto.IdLabel;
import gr.uoa.di.madgik.catalogue.domain.DataRequest;
import gr.uoa.di.madgik.catalogue.domain.FieldType;
import gr.uoa.di.madgik.catalogue.domain.TypeInfo;
import gr.uoa.di.madgik.catalogue.domain.types.CustomProperties;
import gr.uoa.di.madgik.catalogue.domain.types.DateProperties;
import gr.uoa.di.madgik.catalogue.domain.types.NumberProperties;
import gr.uoa.di.madgik.catalogue.domain.types.PatternProperties;
import gr.uoa.di.madgik.catalogue.domain.types.TextProperties;
import gr.uoa.di.madgik.catalogue.domain.types.TypeProperties;
import gr.uoa.di.madgik.catalogue.domain.types.UrlProperties;
import gr.uoa.di.madgik.catalogue.domain.types.VocabularyProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.net.URI;
import java.util.List;

/**
 * Deserializes {@link TypeInfo} for the {@code tools.jackson} stack.
 *
 * <p>This class exists for the same reason as {@link TypeInfoDeserializer}, but against the alternate
 * Jackson API used by the tools layer. {@link TypeInfo#getProperties()} is polymorphic and its concrete type
 * depends on the sibling {@code type} field, so generic bean deserialization is insufficient. The
 * deserializer must inspect {@code type} first and then bind {@code properties} to the matching
 * {@link TypeProperties} implementation.
 *
 * <p>It also keeps older serialized models readable by rebuilding missing {@code properties} from the
 * legacy payload format and by normalizing legacy {@code values} arrays that may still arrive as simple
 * strings rather than {@link IdLabel} objects.
 */
public class TypeInfoDeserializer extends StdDeserializer<TypeInfo> {

    public TypeInfoDeserializer() {
        super(TypeInfo.class);
    }

    @Override
    public TypeInfo deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = (JsonNode) p.readValueAsTree();

        FieldType type = mapper.treeToValue(node.path("type"), FieldType.class);
        String defaultValue = node.path("defaultValue").asText(null);
        List<IdLabel> values;
        try {
            values = mapper.treeToValue(node.path("values"), new TypeReference<>() {});
        } catch (Exception e) {
            List<String> vals = mapper.treeToValue(node.path("values"), new TypeReference<>() {});
            values = vals == null ? null : vals.stream().map(item -> new IdLabel(item, item)).toList();
        }
        boolean multiplicity = node.path("multiplicity").asBoolean(false);
        DataRequest prefill = mapper.treeToValue(node.path("prefill"), DataRequest.class);
        JsonNode propsNode = node.get("properties");

        String vocabulary = node.path("vocabulary").asText(null);
        TypeProperties properties;

        if (propsNode == null || propsNode.isNull()) {
            properties = switch (type) {
                case number -> {
                    NumberProperties numberProperties = new NumberProperties();
                    if (values != null && !values.isEmpty()) {
                        IdLabel value = values.getFirst();
                        String[] split = value.id().split("\\.");
                        if (split.length > 1) {
                            numberProperties.setDecimals(split[1].length());
                        }
                        values = null;
                    }
                    yield numberProperties;
                }
                case string, largeText -> new TextProperties();
                case date -> new DateProperties();
                case url -> new UrlProperties();
                case vocabulary -> {
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
                case email, phone -> new PatternProperties();
                default -> new CustomProperties();
            };
        } else {
            properties = switch (type) {
                case number -> mapper.treeToValue(propsNode, NumberProperties.class);
                case string, largeText -> mapper.treeToValue(propsNode, TextProperties.class);
                case date -> mapper.treeToValue(propsNode, DateProperties.class);
                case url -> mapper.treeToValue(propsNode, UrlProperties.class);
                case vocabulary -> mapper.treeToValue(propsNode, VocabularyProperties.class);
                case email, phone -> mapper.treeToValue(propsNode, PatternProperties.class);
                default -> mapper.treeToValue(propsNode, CustomProperties.class);
            };
        }

        return new TypeInfo(vocabulary, type, defaultValue, values, properties, multiplicity, prefill);
    }
}
