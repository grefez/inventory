package com.hal9000.warehouse.inventory.adapter.out;

import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.port.out.ProductCatalogueRepository;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ProductCatalogue implements ProductCatalogueRepository {

    private final Map<String, Product> instance = new ConcurrentHashMap<>();

    public void addToCatalogue(ProductCatalogueIn productCatalogueIn) {
        productCatalogueIn.getProductList()
            .forEach(product -> instance.put(product.getName(), product));

    }

    public Optional<Product> findProductByName(String productName) {
        return Optional.ofNullable(instance.get(productName));
    }

    public Set<Product> findAllProducts() {
        return new HashSet<>(instance.values());
    }
}
