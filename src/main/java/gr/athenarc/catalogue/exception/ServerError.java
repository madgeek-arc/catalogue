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

package gr.athenarc.catalogue.exception;

import gr.athenarc.catalogue.config.logging.LogTransactionsFilter;
import gr.athenarc.catalogue.utils.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatusCode;

import java.util.Date;

/**
 * Error reporting class. It is returned every time an exception is thrown.
 */
public class ServerError {

    /**
     * The status code to return.
     */
    int status;
    /**
     * The id of the erroneous transaction.
     */
    String transactionId;
    /**
     * The timestamp the error occurred.
     */
    Date timestamp;
    /**
     * The requested url that the error occurred.
     */
    String url;
    /**
     * The error message to display.
     */
    String message;

    public ServerError() {
        timestamp = new Date();
    }

    public ServerError(String transactionId, String url, String message) {
        timestamp = new Date();
        this.transactionId = transactionId;
        this.url = url;
        this.message = message;
    }

    public ServerError(HttpStatusCode status, String transactionId, String url, String message) {
        timestamp = new Date();
        this.status = status.value();
        this.transactionId = transactionId;
        this.url = url;
        this.message = message;
    }

    public ServerError(int status, String transactionId, String url, String message) {
        timestamp = new Date();
        this.status = status;
        this.transactionId = transactionId;
        this.url = url;
        this.message = message;
    }

    public ServerError(HttpStatusCode status, HttpServletRequest req, Exception exception) {
        timestamp = new Date();
        this.status = status.value();
        this.transactionId = LogTransactionsFilter.getTransactionId();
        this.url = RequestUtils.getUrlWithParams(req);
        this.message = exception.getMessage();
    }

    public ServerError(HttpStatusCode status, String transactionId, HttpServletRequest req, Exception exception) {
        timestamp = new Date();
        this.status = status.value();
        this.transactionId = transactionId;
        this.url = RequestUtils.getUrlWithParams(req);
        this.message = exception.getMessage();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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
