package utilities;

import models.commons.RequestContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;

import static models.logger.LoggerFields.*;


final public class LoggerUtilities {
    public static void fillInLoggerFields(@NonNull final RequestContext requestContext){
        MDC.put(LOG_USER_ID, String.valueOf(requestContext.userId()));
        MDC.put(LOG_DISH_NAME, requestContext.dishName());
        MDC.put(LOG_COMMAND_TYPE, requestContext.commandType());
        MDC.put(LOG_COMMAND_STATE, requestContext.commandState());
    }

    public static void clearLoggerField(){
        MDC.clear();
    }
}
