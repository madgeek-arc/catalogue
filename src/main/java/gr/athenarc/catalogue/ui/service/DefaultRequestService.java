package gr.athenarc.catalogue.ui.service;

import com.jayway.jsonpath.JsonPath;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.Prefill;
import gr.athenarc.catalogue.ui.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class DefaultRequestService implements RequestService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRequestService.class);
    private final RestTemplate restTemplate;

    public DefaultRequestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object perform(Prefill prefillRequestData) {
        Request request = prefillRequestData.getRequest();
        HttpEntity<?> entity = new HttpEntity<>(
                replaceParams(request.getBody(), prefillRequestData.getParams()),
                request.getHeaders()
        );
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    request.getUrl().toString(),
                    request.getMethod(),
                    entity,
                    String.class,
                    prefillRequestData.getParams()
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractValue(
                        response.getBody(),
                        prefillRequestData.getExpression(),
                        prefillRequestData.getContentType()
                );
            }
        } catch (IllegalArgumentException e) {
            throw new ResourceException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (HttpServerErrorException e) {

        }

        return null;
    }

    String replaceParams(String text, Map<String, String> params) {
        if (text != null && params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }

    Object extractValue(String body, String expression, String contentType) {
        if (body == null) {
            throw new ResourceNotFoundException("Cannot extract values : [body=null]");
        }
        if (expression == null || "".equals(expression)) {
            return body;
        }
        if (contentType.equalsIgnoreCase("JSON") || contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)) {
            logger.debug("Using JSONPath");
            return extractJSONValue(body, expression);
        } else if (contentType.equalsIgnoreCase("XML") || contentType.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) {
            logger.debug("Using XPath");
            return extractXMLValue(body, expression);
        }
        return body;
    }

    private Object extractJSONValue(String body, String jsonPath) {
        return JsonPath.parse(body).read(jsonPath);
    }

    private String extractXMLValue(String body, String xpath) {
        return null;
    }


}
