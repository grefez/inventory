package com.hal9000.warehouse.inventory.port.in;

import com.hal9000.warehouse.inventory.domain.Product;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Value;

public interface ProductCatalogueUseCase {

    enum ErrorType {
        NON_EXISTENT_ARTICLES, NON_EXISTENT_PRODUCT, INVALID_QUANTITY, NOT_ENOUGH_SUPPLIES
    }

    @Getter
    class ProductCatalogueException extends RuntimeException {
        private final ErrorType errorType;
        public ProductCatalogueException(ErrorType errorType, String message) {
            super(message);
            this.errorType = errorType;
        }
    }

    @Value
    class ProductCatalogueIn {
        List<Product> productList;
    }

    @Value
    class AvailableProducts {
        Set<AvailableProduct> availableProductList;
    }

    @Value
    class AvailableProduct {
        int quantity;
        String productName;
    }

    void addToCatalogue(ProductCatalogueIn productCatalogueIn) throws ProductCatalogueException;

    boolean sellProduct (String productName, int quantity) throws ProductCatalogueException;

    AvailableProducts getAvailableProducts ();


}
