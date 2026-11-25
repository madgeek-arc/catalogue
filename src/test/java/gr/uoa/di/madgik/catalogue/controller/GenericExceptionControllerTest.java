/*
 * Copyright 2021-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.controller;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DatabindException;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GenericExceptionControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new GenericExceptionController(
                        new StaticListableBeanFactory().getBeanProvider(Tracer.class)))
                .build();
    }

    @Test
    void aClientThatDisconnectsWhileTheBodyIsWrittenGetsNoErrorResponse() throws Exception {
        mvc.perform(get("/disconnected-while-writing").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void anUnusableResponseGetsNoErrorResponse() throws Exception {
        mvc.perform(get("/not-usable").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void aBodyThatCannotBeSerializedIsAnInternalServerError() throws Exception {
        mvc.perform(get("/not-serializable").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Internal Server Error"));
    }

    @Test
    void aConnectionResetOutsideTheResponseIsAnInternalServerError() throws Exception {
        mvc.perform(get("/backend-reset").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Internal Server Error"));
    }

    @Test
    void aResetWhileAnOutboundRequestIsWrittenIsAnInternalServerError() throws Exception {
        mvc.perform(get("/outbound-reset").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Internal Server Error"));
    }

    @Test
    void anEndOfFileWhileTheBodyIsSerializedIsAnInternalServerError() throws Exception {
        mvc.perform(get("/end-of-file").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Internal Server Error"));
    }

    @RestController
    static class FailingController {

        @GetMapping("/disconnected-while-writing")
        Object disconnectedWhileWriting() {
            AsyncRequestNotUsableException notUsable = new AsyncRequestNotUsableException(
                    "ServletOutputStream failed to write", new IOException("Broken pipe"));
            throw new HttpMessageNotWritableException("Could not write JSON",
                    DatabindException.from((JsonGenerator) null, "write failed", notUsable));
        }

        @GetMapping("/not-usable")
        Object notUsable() throws IOException {
            throw new AsyncRequestNotUsableException("Response not usable after response errors.");
        }

        @GetMapping("/not-serializable")
        Object notSerializable() {
            return new NotSerializable();
        }

        @GetMapping("/outbound-reset")
        Object outboundReset() {
            throw new HttpMessageNotWritableException("Could not write JSON",
                    JacksonIOException.construct(new SocketException("Connection reset by peer")));
        }

        @GetMapping("/end-of-file")
        Object endOfFile() {
            throw new HttpMessageNotWritableException("Could not write JSON",
                    JacksonIOException.construct(new EOFException()));
        }

        @GetMapping("/backend-reset")
        Object backendReset() {
            throw new IllegalStateException("query failed", new IOException("Connection reset by peer"));
        }
    }

    static class NotSerializable {
        public String getValue() {
            throw new IllegalStateException("no value");
        }
    }
}
