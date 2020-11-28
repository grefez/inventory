package com.hal9000.warehouse.inventory.port.in;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import java.util.List;
import lombok.Getter;
import lombok.Value;

public interface InventoryUseCase {

    enum ErrorType {
        INVALID_QUANTITY
    }

    @Getter
    class InventoryException extends RuntimeException {
        private final ErrorType errorType;
        public InventoryException(ErrorType errorType, String message) {
            super(message);
            this.errorType = errorType;
        }
    }

    @Value
    class AddInventoryIn {
        List<ArticleSupply> articleSupplies;
    }

    void addToInventory(AddInventoryIn addInventoryIn);
}
