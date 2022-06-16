package gr.athenarc.catalogue.config.logging;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(0)
public class LogTransactionsFilter extends AbstractLogContextFilter {

    private static final String TRANSACTION_ID = "transaction_id";

    @Override
    public void editMDC(MDCAdapter mdc) {
        String transactionId = UUID.randomUUID().toString();
        mdc.put(TRANSACTION_ID, transactionId);
    }

    public static String getTransactionId() {
        return MDC.get(TRANSACTION_ID);
    }
}
