package com.hal9000.warehouse.inventory.port.out;

import com.hal9000.warehouse.inventory.domain.Product;
import java.util.List;
import java.util.Optional;
import lombok.Value;

public interface ProductCatalogueRepository {

    @Value
    class ProductCatalogueIn {
        List<Product> productList;
    }

    void addToCatalogue(ProductCatalogueIn productCatalogueIn);

    Optional<Product> findProductByName(String productName);

    List<Product> findAllProducts ();
}
