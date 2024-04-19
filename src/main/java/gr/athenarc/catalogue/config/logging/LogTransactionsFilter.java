/*
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

package gr.athenarc.catalogue.config.logging;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
@Order(0)
public class LogTransactionsFilter extends AbstractLogContextFilter {

    private static final String TRANSACTION_ID = "transaction_id";

    @Override
    public void editMDC(MDCAdapter mdc, ServletRequest request, ServletResponse response) {
        String transactionId = UUID.randomUUID().toString();
        mdc.put(TRANSACTION_ID, transactionId);
        if (StringUtils.hasText(((HttpServletRequest) request).getHeader("X-Forwarded-For"))) {
            mdc.put("remote_ip", ((HttpServletRequest) request).getHeader("X-Forwarded-For"));
        } else {
            mdc.put("remote_ip", request.getRemoteAddr());
        }
    }

    public static String getTransactionId() {
        return MDC.get(TRANSACTION_ID);
    }
}
