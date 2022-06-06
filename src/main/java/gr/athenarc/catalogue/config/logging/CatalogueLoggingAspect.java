package gr.athenarc.catalogue.config.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CatalogueLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(CatalogueLoggingAspect.class);

    @Pointcut("within(gr.athenarc.catalogue.controller.*) || within(gr.athenarc.catalogue.service.*)")
    private void everyLibraryMethod() {}

    @Pointcut("within(gr.athenarc.catalogue.ui..*)")
    private void everyLibraryUiMethod() {}

    @Pointcut("everyLibraryMethod() || everyLibraryUiMethod()")
    private void everyMethod() {}

    @Before("gr.athenarc.catalogue.config.logging.CatalogueLoggingAspect.everyMethod()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        logger.trace("Entering method: {}", joinPoint.getSignature());
    }

    @After("gr.athenarc.catalogue.config.logging.CatalogueLoggingAspect.everyMethod()")
    public void logAfterMethod(JoinPoint joinPoint) {
        logger.trace("Leaving method: {}", joinPoint.getSignature());
    }
}
