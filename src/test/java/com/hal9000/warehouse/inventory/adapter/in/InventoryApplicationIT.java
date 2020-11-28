package com.hal9000.warehouse.inventory.adapter.in;

import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_PRODUCT;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NOT_ENOUGH_SUPPLIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hal9000.warehouse.inventory.adapter.in.InventoryController.AddInventoryIn;
import com.hal9000.warehouse.inventory.adapter.in.ProductCatalogueController.AddProductsIn;
import com.hal9000.warehouse.inventory.adapter.in.ProductCatalogueController.AvailableProducts;
import com.hal9000.warehouse.inventory.adapter.in.ProductCatalogueController.SellProductIn;
import com.hal9000.warehouse.inventory.adapter.in.error.ErrorResponse;
import com.hal9000.warehouse.inventory.port.in.InventoryUseCase;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class InventoryApplicationIT {

    public static final String TEST_FILES_FOLDER = "test-files";
    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp () {
        restTemplate = new RestTemplateBuilder().rootUri("http://localhost:" + port).build();

        restTemplate.postForLocation("/inventory/update", getPayload("inventory.json", AddInventoryIn.class));
        restTemplate.postForLocation("/products/update", getPayload("products.json", AddProductsIn.class));

    }

    @Test
    @DisplayName ("When trying to sell a product that has enough supplies, it should return OK")
    void sellProduct() {

        ResponseEntity<ErrorResponse> responseEntity = restTemplate
            .postForEntity("/products/sell", getPayload("sell-product.json", SellProductIn.class), ErrorResponse.class);

        assertEquals(OK, responseEntity.getStatusCode());

    }

    @Test
    @DisplayName ("When trying to sell a product that has not enough supplies, it should show an error")
    void sellProductNotEnoughSupplies() {

        HttpClientErrorException httpException = assertThrows(HttpClientErrorException.class,
            () -> restTemplate.postForLocation("/products/sell", getPayload("sell-product-no-supplies.json", SellProductIn.class)));

        assertEquals(CONFLICT, httpException.getStatusCode());
        assertEquals(NOT_ENOUGH_SUPPLIES, ProductCatalogueUseCase.ErrorType.valueOf(getErrorResponse(httpException).getCode()));

    }

    @Test
    @DisplayName ("It should list all available products")
    void listAvailableProducts () {
        AvailableProducts availableProducts = getPayload("available-products.json", AvailableProducts.class);

        ResponseEntity<AvailableProducts> responseEntity = restTemplate.getForEntity("/products/available", AvailableProducts.class);

        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(availableProducts, responseEntity.getBody());

    }

    @Test
    @DisplayName ("It should show errors loading articles in the inventory")
    void errorsLoadingArticles () {
        HttpClientErrorException httpException = assertThrows(HttpClientErrorException.class,
            () -> restTemplate.postForLocation("/inventory/update", getPayload("invalid-inventory.json", AddInventoryIn.class)));

        assertEquals(CONFLICT, httpException.getStatusCode());
        assertEquals(InventoryUseCase.ErrorType.INVALID_QUANTITY,
            InventoryUseCase.ErrorType.valueOf(getErrorResponse(httpException).getCode()));
    }

    @Test
    @DisplayName ("It should show errors loading products with wrong parameters")
    void errorsLoadingProducts () {
        HttpClientErrorException httpException = assertThrows(HttpClientErrorException.class,
            () -> restTemplate.postForLocation("/products/update", getPayload("products-invalid-quantity.json", AddProductsIn.class)));

        assertEquals(CONFLICT, httpException.getStatusCode());
        assertEquals(ProductCatalogueUseCase.ErrorType.INVALID_QUANTITY,
            ProductCatalogueUseCase.ErrorType.valueOf(getErrorResponse(httpException).getCode()));

        httpException = assertThrows(HttpClientErrorException.class,
            () -> restTemplate.postForLocation("/products/update", getPayload("products-invalid-articles.json", AddProductsIn.class)));

        assertEquals(CONFLICT, httpException.getStatusCode());
        assertEquals(ErrorType.NON_EXISTENT_ARTICLES,
            ProductCatalogueUseCase.ErrorType.valueOf(getErrorResponse(httpException).getCode()));
    }

    @Test
    @DisplayName ("It should show errors trying to sell a not existing product")
    void errorsSellingNotExistingProduct () {
        HttpClientErrorException httpException = assertThrows(HttpClientErrorException.class,
            () -> restTemplate.postForLocation("/products/sell", getPayload("sell-product-not-existing.json", SellProductIn.class)));

        assertEquals(CONFLICT, httpException.getStatusCode());
        assertEquals(NON_EXISTENT_PRODUCT, ProductCatalogueUseCase.ErrorType.valueOf(getErrorResponse(httpException).getCode()));

    }

    @SneakyThrows
    private <T> T getPayload(String jsonFile, Class<T> type) {
        String jsonContent = Files.readString(Paths.get(TEST_FILES_FOLDER + "/" + jsonFile));
        return objectMapper.readValue(jsonContent, type);
    }

    @SneakyThrows
    private ErrorResponse getErrorResponse(HttpClientErrorException httpException) {
        return objectMapper.readValue(httpException.getResponseBodyAsString(), ErrorResponse.class);
    }

}
