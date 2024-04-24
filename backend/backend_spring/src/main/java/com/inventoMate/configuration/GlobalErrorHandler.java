package com.inventoMate.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.inventoMate.exceptions.ResourceAlreadyExistsException;
import com.inventoMate.exceptions.ResourceNotFoundException;
import com.inventoMate.models.ErrorMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalErrorHandler {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public ErrorMessage handleNotFound(final HttpServletRequest request, final Exception error) {
		return ErrorMessage.from("Not Found",HttpStatus.NOT_FOUND.value());
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public ErrorMessage handleAccessDenied(final HttpServletRequest request, final Exception error) {
		return ErrorMessage.from("Permission denied",HttpStatus.FORBIDDEN.value());
	}
	
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ErrorMessage handleResourceAlreadyExists(final HttpServletRequest request, final Exception error) {
        return ErrorMessage.from(error.getMessage(),HttpStatus.CONFLICT.value());
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorMessage handleResourceNotFound(final HttpServletRequest request, final Exception error) {
        return ErrorMessage.from(error.getMessage(),HttpStatus.NOT_FOUND.value());
    }
    
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Throwable.class)
	public ErrorMessage handleInternalError(final HttpServletRequest request, final Exception error) {
		return ErrorMessage.from(error.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
}
