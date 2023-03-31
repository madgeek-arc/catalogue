package gr.athenarc.catalogue.config.logging;

import gr.athenarc.catalogue.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LogRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LogRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String url = RequestUtils.getUrlWithParams(request);
        logger.info(url);
        filterChain.doFilter(request, response);
    }
}
