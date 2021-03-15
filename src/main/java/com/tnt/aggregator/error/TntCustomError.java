package com.tnt.aggregator.error;

import lombok.Data;

@Data
public class TntCustomError {


    private static final long serialVersionUID = -8979390207199719926L;

    private String status;
    private String errorCode;
    private String errorMessage;

    public TntCustomError() {
        super();
    }

    public TntCustomError(String status, String errorCode, String errorMessage) {
        super();
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public TntCustomError(String errorCode) {
        super();
        this.errorCode = errorCode;
    }
}
