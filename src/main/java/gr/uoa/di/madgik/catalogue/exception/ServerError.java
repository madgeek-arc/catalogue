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

package gr.uoa.di.madgik.catalogue.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;

/**
 * Legacy error payload previously returned by catalogue exception handlers.
 *
 * @deprecated Since 10.0.0, use {@link org.springframework.http.ProblemDetail} for HTTP API
 * error responses instead. {@code ProblemDetail} is now the standard error contract used by the
 * catalogue exception handling layer, while this class remains only for backward compatibility
 * with downstream projects that may still reference the old payload shape.
 */
@Deprecated(since = "10.0.0", forRemoval = true)
public class ServerError {

    /**
     * The status code to return.
     */
    int status;
    /**
     * The id of the erroneous transaction.
     */
    String traceId;
    /**
     * The timestamp the error occurred.
     */
    Instant timestamp;
    /**
     * The requested url that the error occurred.
     */
    String url;
    /**
     * The error message to display.
     */
    String message;

    public ServerError() {
        timestamp = Instant.now();
    }

    public ServerError(String traceId, String url, String message) {
        timestamp = Instant.now();
        this.traceId = traceId;
        this.url = url;
        this.message = message;
    }

    public ServerError(HttpStatusCode status, String traceId, String url, String message) {
        timestamp = Instant.now();
        this.status = status.value();
        this.traceId = traceId;
        this.url = url;
        this.message = message;
    }

    public ServerError(int status, String traceId, String url, String message) {
        timestamp = Instant.now();
        this.status = status;
        this.traceId = traceId;
        this.url = url;
        this.message = message;
    }

    public ServerError(HttpStatusCode status, HttpServletRequest req, String message) {
        timestamp = Instant.now();
        this.status = status.value();
        this.traceId = MDC.get("traceId");
        this.url = getUrlWithParams(req);
        this.message = message;
    }

    public ServerError(HttpStatusCode status, HttpServletRequest req, Exception exception) {
        timestamp = Instant.now();
        this.status = status.value();
        this.traceId = MDC.get("traceId");
        this.url = getUrlWithParams(req);
        this.message = exception.getMessage();
    }

    public ServerError(HttpStatusCode status, String traceId, HttpServletRequest req, Exception exception) {
        timestamp = Instant.now();
        this.status = status.value();
        this.traceId = traceId;
        this.url = getUrlWithParams(req);
        this.message = exception.getMessage();
    }

    public static String getUrlWithParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod());
        sb.append(": ");
        sb.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append('?');
            sb.append(request.getQueryString());
        }
        return sb.toString();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
