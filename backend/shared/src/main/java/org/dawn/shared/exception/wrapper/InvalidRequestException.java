package org.dawn.shared.exception.wrapper;

import org.dawn.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class InvalidRequestException extends ApiException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }


}