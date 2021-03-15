package com.tnt.aggregator.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class TntCustomExceptionHandler {

    private static final Logger LOGGER =
            Logger.getLogger(TntCustomExceptionHandler.class.getName());



    @ExceptionHandler({TntCustomException.class})
    @ResponseBody
    @Produces({MediaType.APPLICATION_JSON})
    public TntCustomError serviceError(HttpServletResponse httpRes,
                                       TntCustomException exception) {

        String errorCode = exception.getCustError().getErrorCode();

        if (errorCode.equalsIgnoreCase(TntErrorConstants.NO_DATA_FOUND_E1001)) {
            httpRes.setStatus(HttpStatus.NOT_FOUND.value());
        }
        else {
            httpRes.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        LOGGER.log(Level.SEVERE,
                "DataServiceException Caught in Handler - " + exception.getCustError().getErrorCode() + "-"
                        + exception.getCustError().getErrorMessage());

        return exception.getCustError();
    }

}
