package gr.athenarc.catalogue.aspects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.config.CatalogueLibProperties;
import gr.athenarc.catalogue.ui.domain.Model;
import gr.athenarc.catalogue.ui.domain.Section;
import gr.athenarc.catalogue.ui.domain.UiField;
import gr.athenarc.catalogue.ui.service.ModelService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Aspect
@Component
public class GenericResourceValidationAspect {

    private static final Logger logger = LoggerFactory.getLogger(GenericResourceValidationAspect.class);
    private final ModelService modelService;
    private final CatalogueLibProperties properties;


    GenericResourceValidationAspect(ModelService modelService, CatalogueLibProperties properties) {
        this.modelService = modelService;
        this.properties = properties;
    }

    @Before("execution(* gr.athenarc.catalogue.service.AbstractGenericItemService.add(..))")
    public void validateBeforeAdd(JoinPoint joinPoint) {
        if (properties.getValidation().isEnabled()) {
            String resourceTypeName = (String) joinPoint.getArgs()[0];
            Object item = joinPoint.getArgs()[1];
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                logger.debug("Validating resource: {}", objectMapper.writeValueAsString(item));
            } catch (JsonProcessingException ignore) {}
            validate(item, resourceTypeName);
        }
    }

    @Before("execution(* gr.athenarc.catalogue.service.AbstractGenericItemService.update(..))")
    public void validateBeforeUpdate(JoinPoint joinPoint) {
        if (properties.getValidation().isEnabled()) {
            String resourceTypeName = (String) joinPoint.getArgs()[0];
            Object item = joinPoint.getArgs()[2];
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                logger.debug("Validating resource: {}", objectMapper.writeValueAsString(item));
            } catch (JsonProcessingException ignore) {}
            validate(item, resourceTypeName);
        }
    }

    private <T> T validate(T resource, String resourceTypeName) {
        FacetFilter ff = new FacetFilter();
        ff.addFilter("resourceType", resourceTypeName);
        List<Model> models = modelService.browse(ff).getResults();
        if (models == null || models.isEmpty()) {
            logger.warn("Could not find model to validate resource : [resourceType={}]", resourceTypeName);
            return resource;
        } else if (models.size() != 1) {
            throw new RuntimeException(String.format("Found more than one models : [resourceType=%s]", resourceTypeName));
        }
        Model model = models.get(0);
        return (T) validateSections(resource, model.getSections());
    }

    private Object validateSections(Object obj, List<Section> sections) {
        for (Section section : sections) {
            if (section.getSubSections() != null) {
                validateSections(obj, section.getSubSections());
            }
            if (section.getFields() != null) {
                validateFields(obj, section.getFields(), true);
            }
        }
        return obj;
    }

    private boolean validateFields(Object object, List<UiField> fields, boolean mandatory) {
        boolean empty = true;
        for (UiField field : fields) {
            if (field.getSubFields() != null && !field.getSubFields().isEmpty()) {
                if (object == null) {
                    break;
                } else if (object instanceof List) {
                    for (Object ans : (List) object) {
                        empty = empty && validateFields(((LinkedHashMap) ans).get(field.getName()), field.getSubFields(), field.getForm().getMandatory());
                    }
                } else {
                    empty = validateFields(((LinkedHashMap) object).get(field.getName()), field.getSubFields(), field.getForm().getMandatory());
                }

                if ("composite".equals(field.getTypeInfo().getType()) && empty) {
                    if (object instanceof LinkedHashMap) {
                        ((LinkedHashMap) object).put(field.getName(), null);
                    } else {
                        object = null;
                    }
                }
            }
            if (mandatory && field.getForm().getMandatory()) {
                checkMandatoryField(object, field);
            }
            if (containsValue(object)) {
                empty = false;
            }
        }
        return empty;
    }

    private void checkMandatoryField(Object answer, UiField field) {
        if (answer == null) {
            throw new RuntimeException(String.format("Mandatory field %s is empty.", field.getName()));
        } else if (answer instanceof List) {
            if (((List) answer).isEmpty()) {
                throw new RuntimeException(String.format("Mandatory field %s is empty.", field.getName()));
            }
        } else if (((LinkedHashMap) answer).get(field.getName()) == null || ((LinkedHashMap) answer).get(field.getName()).equals("")) {
            throw new RuntimeException(String.format("Mandatory field %s is empty.", field.getName()));
        }
    }

    private boolean containsValue(Object obj) {
        boolean contains = false;
        if (obj instanceof LinkedHashMap) {
            for (Object key : ((LinkedHashMap) obj).keySet()) {
                if (((LinkedHashMap) obj).get(key) instanceof LinkedHashMap || ((LinkedHashMap) obj).get(key) instanceof List) {
                    contains = contains || containsValue(((LinkedHashMap) obj).get(key));
                } else if (((LinkedHashMap) obj).get(key) != null && !((LinkedHashMap) obj).get(key).equals("")) {
                    return true;
                }
            }
        } else if (obj instanceof List) {
            for (Object item : (List) obj) {
                contains = contains || containsValue(item);
                if (contains) {
                    break;
                }
            }
        }
        return contains;
    }
}
