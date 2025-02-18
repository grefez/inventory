package com.hal9000.warehouse.inventory.adapter.in.error;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.ResponseEntity.status;

import com.hal9000.warehouse.inventory.port.in.InventoryUseCase.InventoryException;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ProductCatalogueException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<ErrorResponse> handleInventoryException (InventoryException exception) {
        return getResponse(exception.getErrorType().toString(), exception.getMessage());
    }

    @ExceptionHandler(ProductCatalogueException.class)
    public ResponseEntity<ErrorResponse> handleProductCatalogueException (ProductCatalogueException exception) {
        return getResponse(exception.getErrorType().toString(), exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> getResponse(String code, String message) {
        return status(CONFLICT).body(new ErrorResponse(code, message));
    }




}
