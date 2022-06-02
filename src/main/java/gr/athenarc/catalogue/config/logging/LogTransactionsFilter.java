package gr.athenarc.catalogue.config.logging;

import org.slf4j.spi.MDCAdapter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(0)
public class LogTransactionsFilter extends AbstractLogContextFilter {

    @Override
    public void editMDC(MDCAdapter mdc) {
        String transactionId = UUID.randomUUID().toString();
        mdc.put("transaction_id", transactionId);
    }

}
