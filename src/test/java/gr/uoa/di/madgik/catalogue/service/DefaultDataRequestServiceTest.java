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

import com.sun.net.httpserver.HttpServer;
import gr.uoa.di.madgik.catalogue.config.CatalogueLibProperties;
import gr.uoa.di.madgik.catalogue.domain.DataRequest;
import gr.uoa.di.madgik.catalogue.domain.Request;
import gr.uoa.di.madgik.registry.exception.ResourceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultDataRequestServiceTest {

    private HttpServer server;
    private final AtomicReference<String> lastRequest = new AtomicReference<>();
    private volatile long delayMillis;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            lastRequest.set(exchange.getRequestMethod() + " " + exchange.getRequestURI());
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (exchange.getRequestURI().getPath().equals("/redirect")) {
                exchange.getResponseHeaders().add("Location", "http://127.0.0.1:" + server.getAddress().getPort() + "/target");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }
            byte[] body = "{\"name\":\"value\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void requestToPrivateAddressIsRejectedWith400WithoutBeingSent() throws Exception {
        DefaultDataRequestService service = new DefaultDataRequestService(new CatalogueLibProperties());

        ResourceException e = assertThrows(ResourceException.class,
                () -> service.retrieve(dataRequest(url("/data"), null)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(null, lastRequest.get());
    }

    @Test
    void cloudMetadataAddressIsRejected() {
        DefaultDataRequestService service = new DefaultDataRequestService(new CatalogueLibProperties());

        assertThrows(ResourceException.class,
                () -> service.retrieve(dataRequest("http://169.254.169.254/latest/meta-data/", null)));
    }

    @Test
    void allowedRequestExpandsParametersAndExtractsTheValue() throws Exception {
        DefaultDataRequestService service = new DefaultDataRequestService(allowingPrivateAddresses());
        DataRequest request = dataRequest(url("/items/{id}"), "$.name");
        request.setParams(Map.of("id", "42"));

        Object value = service.retrieve(request).block(Duration.ofSeconds(5));

        assertEquals("value", value);
        assertEquals("GET /items/42", lastRequest.get());
    }

    @Test
    void redirectsAreNotFollowed() throws Exception {
        DefaultDataRequestService service = new DefaultDataRequestService(allowingPrivateAddresses());

        service.retrieve(dataRequest(url("/redirect"), null)).onErrorComplete().block(Duration.ofSeconds(5));

        assertEquals("GET /redirect", lastRequest.get());
    }

    @Test
    void slowResponseFailsAfterResponseTimeout() throws Exception {
        CatalogueLibProperties properties = allowingPrivateAddresses();
        properties.getDataRequest().setResponseTimeout(Duration.ofMillis(200));
        DefaultDataRequestService service = new DefaultDataRequestService(properties);
        delayMillis = 2_000;

        assertThrows(Exception.class, () -> service.retrieve(dataRequest(url("/data"), null)).block(Duration.ofSeconds(5)));
    }

    private CatalogueLibProperties allowingPrivateAddresses() {
        CatalogueLibProperties properties = new CatalogueLibProperties();
        properties.getDataRequest().setAllowPrivateAddresses(true);
        return properties;
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    @SuppressWarnings("deprecation") // java.net.URL keeps {variable} templates, which java.net.URI rejects
    private static DataRequest dataRequest(String url, String expression) throws Exception {
        Request request = new Request();
        request.setMethod(HttpMethod.GET);
        request.setUrl(new URL(url));
        DataRequest dataRequest = new DataRequest();
        dataRequest.setRequest(request);
        dataRequest.setParams(Map.of());
        dataRequest.setContentType("application/json");
        dataRequest.setExpression(expression);
        return dataRequest;
    }
}
