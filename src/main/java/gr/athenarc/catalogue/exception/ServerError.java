package gr.athenarc.catalogue.exception;

import gr.athenarc.catalogue.RequestUtils;
import gr.athenarc.catalogue.config.logging.LogTransactionsFilter;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class ServerError {

    int status;
    String transactionId;
    Date timestamp;
    String url;
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

    public ServerError(HttpStatus status, String transactionId, String url, String message) {
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

    public ServerError(HttpStatus status, HttpServletRequest req, Exception exception) {
        timestamp = new Date();
        this.status = status.value();
        this.transactionId = LogTransactionsFilter.getTransactionId();
        this.url = RequestUtils.getUrlWithParams(req);
        this.message = exception.getMessage();
    }

    public ServerError(HttpStatus status, String transactionId, HttpServletRequest req, Exception exception) {
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
