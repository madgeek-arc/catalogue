/*
 * Copyright 2021-2026 OpenAIRE AMKE
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

package gr.uoa.di.madgik.catalogue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.catalogue.config.CatalogueLibProperties;
import gr.uoa.di.madgik.catalogue.dto.IdLabel;
import gr.uoa.di.madgik.catalogue.exception.ValidationException;
import gr.uoa.di.madgik.catalogue.ui.domain.*;
import gr.uoa.di.madgik.catalogue.ui.domain.types.*;
import gr.uoa.di.madgik.registry.domain.Browsing;
import gr.uoa.di.madgik.registry.domain.FacetFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelResponseValidatorTest {

    @Mock
    private ModelService modelService;

    private ModelResponseValidator validator;
    private ObjectMapper objectMapper;
    private Model testModel;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Load test model from classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test-model.json")) {
            testModel = objectMapper.readValue(is, Model.class);
        }

        // Build properties manually — @PostConstruct is not triggered outside Spring context
        CatalogueLibProperties properties = new CatalogueLibProperties();
        properties.getValidation().setEnabled(true);
        properties.getValidation().setBaseUrl("http://localhost:8080");

        validator = new ModelResponseValidator(modelService, properties, objectMapper);

        // Inject @Value field that Spring would normally populate
        ReflectionTestUtils.setField(validator, "baseUrl", "http://localhost:8080");

        // Mock WebClient to return HTTP 200 for every request, avoiding real network calls.
        // The validator uses two call chains depending on whether the vocabulary URL is absolute:
        //   1. webClient.mutate().baseUrl(...).build().get().uri(String)...  (relative vocab URLs)
        //   2. webClient.get().uri(URI)...                                  (absolute vocab / strict URL validation)
        ClientResponse mockResponse = mock(ClientResponse.class);
        lenient().when(mockResponse.statusCode()).thenReturn(HttpStatus.OK);

        WebClient mockWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        lenient().when(mockWebClient.mutate().baseUrl(anyString()).build()
                        .get().uri(anyString()).exchangeToMono(any()).block())
                .thenReturn(mockResponse);
        lenient().when(mockWebClient.get().uri(any(URI.class)).exchangeToMono(any()).block())
                .thenReturn(mockResponse);

        ReflectionTestUtils.setField(validator, "webClient", mockWebClient);
    }

    // -------------------------------------------------------------------------
    // Integration test: full model loaded from JSON files
    // -------------------------------------------------------------------------

    @Nested
    class IntegrationTest {

        @Test
        @SuppressWarnings("unchecked")
        void validate_resourceAgainstTestModel_shouldSucceed() throws Exception {
            Browsing<Model> browsing = mock(Browsing.class);
            when(browsing.getResults()).thenReturn(List.of(testModel));
            when(modelService.browse(any(FacetFilter.class))).thenReturn(browsing);

            // Two vocabulary fields use dependsOn (scientificSubdomain → scientificDomain,
            // subcategory → category), which triggers modelService.get + getAllFields.
            // Returning an empty field list leaves `dependedOn` null, so the placeholder
            // in the vocab URL is not substituted — acceptable for this test.
            lenient().when(modelService.get(anyString())).thenReturn(testModel);
            lenient().when(modelService.getAllFields(any(Model.class))).thenReturn(List.of());

            LinkedHashMap<String, Object> resource;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("test-resource.json")) {
                resource = objectMapper.readValue(is, LinkedHashMap.class);
            }

            Object result = validator.validate(resource, "test-model");

            assertNotNull(result);
            verify(modelService).browse(any(FacetFilter.class));
        }
    }

    // -------------------------------------------------------------------------
    // Per-type unit tests — each builds a minimal single-field model on the fly
    // -------------------------------------------------------------------------

    @Nested
    class PhoneType {

        @Test
        void validPhoneNumber_passes() {
            UiField field = buildField("phone", FieldType.phone, new PatternProperties());
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("phone", "+30 210 123 4567"), "test"));
        }

        @Test
        void invalidPhoneNumber_throws() {
            UiField field = buildField("phone", FieldType.phone, new PatternProperties());
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("phone", "not-a-phone"), "test"));
        }

        @Test
        void customPatternOverridesDefault_validValue_passes() {
            PatternProperties props = new PatternProperties();
            props.setPattern("\\d{4}");
            UiField field = buildField("phone", FieldType.phone, props);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("phone", "1234"), "test"));
        }

        @Test
        void customPatternOverridesDefault_invalidValue_throws() {
            PatternProperties props = new PatternProperties();
            props.setPattern("\\d{4}");
            UiField field = buildField("phone", FieldType.phone, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("phone", "abcd"), "test"));
        }
    }

    @Nested
    class SelectType {

        private static final List<IdLabel> OPTIONS =
                List.of(new IdLabel("opt-a", "Option A"), new IdLabel("opt-b", "Option B"));

        @Test
        void knownValue_passes() {
            UiField field = buildSelectField("choice", FieldType.select, OPTIONS);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("choice", "opt-a"), "test"));
        }

        @Test
        void unknownValue_throws() {
            UiField field = buildSelectField("choice", FieldType.select, OPTIONS);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("choice", "opt-z"), "test"));
        }

        @Test
        void emptyValuesList_throws() {
            UiField field = buildSelectField("choice", FieldType.select, List.of());
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("choice", "opt-a"), "test"));
        }
    }

    @Nested
    class RadioType {

        private static final List<IdLabel> OPTIONS =
                List.of(new IdLabel("yes", "Yes"), new IdLabel("no", "No"));

        @Test
        void knownValue_passes() {
            UiField field = buildSelectField("answer", FieldType.radio, OPTIONS);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("answer", "yes"), "test"));
        }

        @Test
        void unknownValue_throws() {
            UiField field = buildSelectField("answer", FieldType.radio, OPTIONS);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("answer", "maybe"), "test"));
        }
    }

    @Nested
    class ScaleType {

        private static final List<IdLabel> LEVELS = List.of(
                new IdLabel("1", "Level 1"), new IdLabel("2", "Level 2"), new IdLabel("3", "Level 3"));

        @Test
        void knownValue_passes() {
            UiField field = buildSelectField("level", FieldType.scale, LEVELS);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("level", "2"), "test"));
        }

        @Test
        void unknownValue_throws() {
            UiField field = buildSelectField("level", FieldType.scale, LEVELS);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("level", "9"), "test"));
        }
    }

    @Nested
    class NumberType {

        @Test
        void numberInRange_passes() {
            NumberProperties props = new NumberProperties();
            props.setMin(1);
            props.setMax(100);
            UiField field = buildField("count", FieldType.number, props);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("count", 42), "test"));
        }

        @Test
        void numberBelowMin_throws() {
            NumberProperties props = new NumberProperties();
            props.setMin(10);
            props.setMax(100);
            UiField field = buildField("count", FieldType.number, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("count", 5), "test"));
        }

        @Test
        void numberAboveMax_throws() {
            NumberProperties props = new NumberProperties();
            props.setMin(1);
            props.setMax(10);
            UiField field = buildField("count", FieldType.number, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("count", 99), "test"));
        }

        @Test
        void tooManyDecimals_throws() {
            NumberProperties props = new NumberProperties();
            props.setDecimals(2);
            UiField field = buildField("ratio", FieldType.number, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("ratio", 3.14159), "test"));
        }

        @Test
        void decimalsWithinLimit_passes() {
            NumberProperties props = new NumberProperties();
            props.setDecimals(2);
            UiField field = buildField("ratio", FieldType.number, props);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("ratio", 3.14), "test"));
        }

        @Test
        void numberMatchesPattern_passes() {
            NumberProperties props = new NumberProperties();
            props.setPattern("\\d{3}");
            UiField field = buildField("code", FieldType.number, props);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("code", 123), "test"));
        }

        @Test
        void numberDoesNotMatchPattern_throws() {
            NumberProperties props = new NumberProperties();
            props.setPattern("\\d{3}");
            UiField field = buildField("code", FieldType.number, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("code", 12), "test"));
        }

        @Test
        void numberAsString_parsedAndValidated() {
            NumberProperties props = new NumberProperties();
            props.setMin(0);
            props.setMax(100);
            UiField field = buildField("score", FieldType.number, props);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("score", "75"), "test"));
        }

        @Test
        void nonNumericString_throws() {
            UiField field = buildField("score", FieldType.number, new NumberProperties());
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("score", "not-a-number"), "test"));
        }
    }

    @Nested
    class BoolType {

        @Test
        void trueBoolean_passes() {
            UiField field = buildField("active", FieldType.bool, new CustomProperties());
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("active", true), "test"));
        }

        @Test
        void falseBoolean_passes() {
            UiField field = buildField("active", FieldType.bool, new CustomProperties());
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("active", false), "test"));
        }

        @Test
        void stringInsteadOfBoolean_throws() {
            UiField field = buildField("active", FieldType.bool, new CustomProperties());
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("active", "true"), "test"));
        }
    }

    @Nested
    class CheckboxType {

        @Test
        void trueCheckbox_passes() {
            UiField field = buildField("agreed", FieldType.checkbox, new CustomProperties());
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("agreed", true), "test"));
        }

        @Test
        void stringInsteadOfBoolean_throws() {
            UiField field = buildField("agreed", FieldType.checkbox, new CustomProperties());
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("agreed", "yes"), "test"));
        }
    }

    @Nested
    class LargeTextType {

        @Test
        void textWithinBounds_passes() {
            TextProperties props = new TextProperties();
            props.setMinLength(5);
            props.setMaxLength(50);
            UiField field = buildField("bio", FieldType.largeText, props);
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(resource("bio", "Hello world"), "test"));
        }

        @Test
        void textTooShort_throws() {
            TextProperties props = new TextProperties();
            props.setMinLength(10);
            UiField field = buildField("bio", FieldType.largeText, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("bio", "Hi"), "test"));
        }

        @Test
        void textTooLong_throws() {
            TextProperties props = new TextProperties();
            props.setMaxLength(5);
            UiField field = buildField("bio", FieldType.largeText, props);
            stubSingleFieldModel(field);
            assertThrows(ValidationException.class,
                    () -> validator.validate(resource("bio", "This is too long"), "test"));
        }

        @Test
        void noLengthConstraint_anyTextPasses() {
            UiField field = buildField("bio", FieldType.largeText, new TextProperties());
            stubSingleFieldModel(field);
            assertDoesNotThrow(() -> validator.validate(
                    resource("bio", "x".repeat(10_000)), "test"));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a UiField with the given name, type and properties.
     * The label is set to the name so path messages are readable.
     * subFields is set to an empty list so validateFields skips composite logic.
     */
    private UiField buildField(String name, FieldType type, TypeProperties properties) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setType(type);
        typeInfo.setProperties(properties);

        UiField field = new UiField();
        field.setName(name);
        field.setLabel(StyledString.of(name));
        field.setTypeInfo(typeInfo);
        field.setSubFields(List.of());
        return field;
    }

    /**
     * Builds a select/radio/scale UiField whose allowed values come from a List<IdLabel>
     * rather than TypeProperties (the validator reads them from TypeInfo.getValues()).
     */
    private UiField buildSelectField(String name, FieldType type, List<IdLabel> values) {
        UiField field = buildField(name, type, new CustomProperties());
        field.getTypeInfo().setValues(values);
        return field;
    }

    /**
     * Wraps a single UiField in a two-level section hierarchy and stubs ModelService
     * to return that model for any browse call.
     *
     * Structure mirroring what validateSections expects:
     *   Section("testSection")
     *     └─ SubSection("testGroup", fields=[field])
     */
    @SuppressWarnings("unchecked")
    private void stubSingleFieldModel(UiField field) {
        Section subSection = new Section();
        subSection.setName("testGroup");
        subSection.setFields(List.of(field));

        Section topSection = new Section();
        topSection.setName("testSection");
        topSection.setSubSections(List.of(subSection));

        Model model = new Model();
        model.setId("test-model");
        model.setSections(List.of(topSection));

        Browsing<Model> browsing = mock(Browsing.class);
        when(browsing.getResults()).thenReturn(List.of(model));
        when(modelService.browse(any(FacetFilter.class))).thenReturn(browsing);
    }

    /** Creates a flat single-entry resource map. */
    private LinkedHashMap<String, Object> resource(String fieldName, Object value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(fieldName, value);
        return map;
    }
}
