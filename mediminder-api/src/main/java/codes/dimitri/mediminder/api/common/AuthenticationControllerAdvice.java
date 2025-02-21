package codes.dimitri.mediminder.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
class AuthenticationControllerAdvice {

    @ExceptionHandler(DisabledException.class)
    public ErrorResponse handleDisabledException(DisabledException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
            .title("Authentication failure")
            .type(URI.create("https://mediminder/authentication/disabled"))
            .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
            .title("Authentication failure")
            .type(URI.create("https://mediminder/authentication"))
            .build();
    }
}
