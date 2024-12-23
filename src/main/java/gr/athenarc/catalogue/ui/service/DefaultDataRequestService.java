/**
 * Copyright 2021-2024 OpenAIRE AMKE
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

package gr.athenarc.catalogue.ui.service;

import com.jayway.jsonpath.JsonPath;
import gr.athenarc.catalogue.exception.ResourceException;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.domain.DataRequest;
import gr.athenarc.catalogue.ui.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
import java.util.Map;

@Service
public class DefaultDataRequestService implements DataRequestService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDataRequestService.class);

    @Override
    public Mono<?> retrieve(DataRequest dataRequest) {
        Request request = dataRequest.getRequest();

        Mono<String> response;
        try {
            WebClient client = WebClient
                    .builder()
                    .uriBuilderFactory(createUriBuilderFactory(request.getUrl(), dataRequest.getParams()))
                    .build();
            response = client
                    .method(request.getMethod())
                    .body(Mono.justOrEmpty(request.getBody()), String.class)
                    .headers(headers -> {
                        if (request.getHeaders() != null) {
                            headers.addAll(request.getHeaders());
                        }
                    })
                    .retrieve()
                    .onStatus(
                            HttpStatus.INTERNAL_SERVER_ERROR::equals,
                            r -> r.bodyToMono(String.class).map(i -> new ResourceException(i, HttpStatus.BAD_REQUEST)))
                    .onStatus(
                            HttpStatus.BAD_REQUEST::equals,
                            r -> r.bodyToMono(String.class).map(i -> new ResourceException(i, HttpStatus.BAD_REQUEST)))
                    .bodyToMono(String.class)
                    .map(obj ->
                            extractValue(
                                    obj,
                                    dataRequest.getExpression(),
                                    dataRequest.getContentType()
                            )
                    );
            return response;
        } catch (IllegalArgumentException e) {
            throw new ResourceException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    UriBuilderFactory createUriBuilderFactory(URL url, Map<String, ?> params) {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(url.toString());
        uriBuilderFactory.setDefaultUriVariables(params);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return uriBuilderFactory;
    }

    String extractValue(String body, String expression, String contentType) {
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

    private String extractJSONValue(String body, String jsonPath) {
        return JsonPath.parse(body).read(jsonPath);
    }

    private String extractXMLValue(String body, String xpath) {
        XPathExpression expression;
        try {
            expression = XPathFactory.newInstance().newXPath().compile(xpath);
            return expression.evaluate(body);
        } catch (XPathExpressionException e) {
            logger.error(e.getMessage(), e);
        }
        return body;
    }


}
