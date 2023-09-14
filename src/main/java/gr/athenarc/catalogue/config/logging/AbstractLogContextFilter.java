package gr.athenarc.catalogue.config.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
public abstract class AbstractLogContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLogContextFilter.class);

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    /**
     * Uses MDCAdapter to edit MDC.
     */
    public abstract void editMDC(MDCAdapter mdc);

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        logger.trace("Editing MDC");
        editMDC(MDC.getMDCAdapter());

        try {
            chain.doFilter(request, response);
        } finally {
            logger.trace("Clearing MDC");
            MDC.clear();
        }
    }

}
