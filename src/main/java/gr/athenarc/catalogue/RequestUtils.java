package gr.athenarc.catalogue;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String getUrlWithParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append('?');
            sb.append(request.getQueryString());
        }
        return sb.toString();
    }

    private RequestUtils() {
    }
}
