package com.tnt.aggregator.error;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TntCustomException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -1L;
    private TntCustomError custError;

    public TntCustomException(String status, String message, Exception exception) {
        super(exception.getMessage());
        this.custError = new TntCustomError(status, message, exception.getMessage());
    }

}

