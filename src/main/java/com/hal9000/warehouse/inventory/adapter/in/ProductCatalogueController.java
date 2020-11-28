package com.hal9000.warehouse.inventory.adapter.in;

import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NOT_ENOUGH_SUPPLIES;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hal9000.warehouse.inventory.adapter.in.ProductCatalogueController.AvailableProducts.AvailableProduct;
import com.hal9000.warehouse.inventory.adapter.in.error.ErrorResponse;
import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.domain.Product.Component;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ProductCatalogueIn;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductCatalogueController {

    private final ProductCatalogueUseCase productCatalogueUseCase;

    @Value
    @NoArgsConstructor(force = true, access = PRIVATE)
    @AllArgsConstructor
    static class SellProductIn {
        String productName;
        int productQuantity;
    }

    @Value
    @NoArgsConstructor(force = true, access = PRIVATE)
    @AllArgsConstructor
    static class AddProductsIn {
        @Value
        @NoArgsConstructor(force = true, access = PRIVATE)
        @AllArgsConstructor
        static class Product {

            @Value
            @NoArgsConstructor(force = true, access = PRIVATE)
            @AllArgsConstructor
            static class Component {
                @JsonProperty("art_id")
                int articleId;

                @JsonProperty("amount_of")
                int amountOf;
            }

            String name;

            @JsonProperty("contain_articles")
            List<Component> containArticles;

        }

        List<Product> products;
    }

    @Value
    @NoArgsConstructor(force = true, access = PRIVATE)
    @AllArgsConstructor
    static class AvailableProducts {

        @Value
        @NoArgsConstructor(force = true, access = PRIVATE)
        @AllArgsConstructor
        static class AvailableProduct {
            String name;

            @JsonProperty("amount_of")
            int amountOf;
        }

        List<AvailableProduct> products;

    }

    @PostMapping("update")
    public void addProducts (@RequestBody AddProductsIn addProductsIn) {

        List<Product> productList = addProductsIn.getProducts().stream()
            .map(this::getProduct)
            .collect(toList());
        productCatalogueUseCase.addToCatalogue(new ProductCatalogueIn(productList));
    }

    @PostMapping("sell")
    public ResponseEntity<ErrorResponse> sellProduct (@RequestBody SellProductIn sellProductIn) {

        return productCatalogueUseCase.sellProduct(sellProductIn.getProductName(), sellProductIn.getProductQuantity()) ?
            status(OK).build() :
            status(CONFLICT).body(new ErrorResponse(NOT_ENOUGH_SUPPLIES.toString(), "Product cannot be sold"));
    }

    @GetMapping("available")
    public AvailableProducts getAvailableProducts () {

        List<AvailableProduct> availableProducts =
            productCatalogueUseCase.getAvailableProducts().getAvailableProductList().stream()
            .map(availableProduct -> new AvailableProduct(availableProduct.getProductName(), availableProduct.getQuantity()))
            .collect(toList());

        return new AvailableProducts(availableProducts);
    }

    private Product getProduct(AddProductsIn.Product product) {
        List<Component> componentList = ofNullable(product.containArticles).map(
            containArticles -> containArticles.stream()
                .map(component -> new Component(component.getArticleId(), component.getAmountOf()))
                .collect(toList()))
            .orElse(emptyList());
        return new Product(product.name, componentList);

    }

}
