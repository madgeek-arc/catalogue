package gr.athenarc.catalogue.ui.domain;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URL;

public class Request {

    HttpMethod method;
    URL url;
    HttpHeaders headers;
    String body;

    public Request() {
        // no-arg constructor
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
