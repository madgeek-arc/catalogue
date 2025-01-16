/**
 * Copyright 2021-2025 OpenAIRE AMKE
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

package gr.athenarc.catalogue.config.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import java.io.IOException;

@Component
public abstract class AbstractLogContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLogContextFilter.class);

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    /**
     * Uses MDCAdapter to edit MDC. Passes down ServletRequest and ServletResponse for more customized functionality.
     *
     * @param mdc      {@link MDCAdapter}
     * @param request  {@link ServletRequest}
     * @param response {@link ServletResponse}
     */
    public abstract void editMDC(MDCAdapter mdc, ServletRequest request, ServletResponse response);

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        logger.trace("Editing MDC");
        editMDC(MDC.getMDCAdapter(), request, response);

        try {
            chain.doFilter(request, response);
        } finally {
            logger.trace("Clearing MDC");
            MDC.clear();
        }
    }

}
