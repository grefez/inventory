package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.INVALID_QUANTITY;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_ARTICLES;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_PRODUCT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.ArticleBatch;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.TakeFromInventoryIn;
import com.hal9000.warehouse.inventory.port.out.ProductCatalogueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogueService implements ProductCatalogueUseCase {

    private final ProductCatalogueRepository productCatalogueRepository;
    private final InventoryRepository inventoryRepository;


    public void addToCatalogue(ProductCatalogueIn productCatalogueIn) {

        List<Product.Component> componentList = productCatalogueIn.getProductList().stream()
            .flatMap(product -> product.getComponents().stream()).collect(toList());

        if (componentList.stream().anyMatch(component -> component.getQuantity() <= 0)) {
            throw productCatalogueException(INVALID_QUANTITY, "Article quantities must be > 0");
        }

        List<Integer> articleIdsNotInInventory = getArticleIdsNotInInventory(componentList);

        if (!articleIdsNotInInventory.isEmpty())
            throw productCatalogueException(NON_EXISTENT_ARTICLES, format("Articles with IDs %s are not in inventory",
                arrayToCommaDelimitedString(articleIdsNotInInventory.toArray())));

        productCatalogueRepository.addToCatalogue(new ProductCatalogueRepository.ProductCatalogueIn(productCatalogueIn.getProductList()));
        log.info ("Products '{}' where added to product catalogue", getProductNames(productCatalogueIn.getProductList()));

    }

    private String getProductNames(List<Product> productList) {
        return arrayToCommaDelimitedString(productList.stream()
            .map(Product::getName)
            .toArray());
    }


    private List<Integer> getArticleIdsNotInInventory(List<Product.Component> componentList) {
        return componentList.stream()
            .map(Product.Component::getArticleId)
            .filter(articleId -> inventoryRepository.findArticleSupplyById(articleId).isEmpty())
            .collect(toList());
    }

    public boolean sellProduct(String productName, int productQuantity) {

        if (productQuantity <= 0)
            throw productCatalogueException(INVALID_QUANTITY, "Product quantity must be > 0");

        return productCatalogueRepository.findProductByName(productName)
            .map(Product::getComponents)
            .map(componentList -> tryToTakeFromInventory(productName, productQuantity, componentList))
            .orElseThrow(() -> productCatalogueException(NON_EXISTENT_PRODUCT, format("Product with name %s does not exist in catalogue", productName)));

    }

    private boolean tryToTakeFromInventory(String productName, int productQuantity, List<Product.Component> componentList) {
        boolean success = inventoryRepository.takeFromInventory(
            new TakeFromInventoryIn(componentList.stream()
                .map(component -> new ArticleBatch(component.getArticleId(), component.getQuantity() * productQuantity))
                .collect(toList())));
        log.info ("{} units of product '{}' " + (success ? "were sold" : "could not be sold"), productQuantity, productName);
        return success;
    }

    public AvailableProducts getAvailableProducts() {
        log.info("Requested available products");
        return new AvailableProducts(
            productCatalogueRepository.findAllProducts().stream()
            .map(product -> new AvailableProduct(findAvailableQuantity(product), product.getName()))
            .filter(availableProduct -> availableProduct.getQuantity() > 0)
            .collect(toSet()));
    }

    private int findAvailableQuantity(Product product) {
        return product.getComponents().stream()
            .mapToInt(this::availableComponentUnits)
            .min()
            .orElse(0);
    }

    private int availableComponentUnits(Product.Component component) {
        Integer articleUnitsInInventory = inventoryRepository.findArticleSupplyById(component.getArticleId())
            .map(ArticleSupply::getQuantity)
            .orElse(0);
        return articleUnitsInInventory / component.getQuantity();
    }

    private ProductCatalogueException productCatalogueException(ErrorType errorType, String message) {
        log.error("{}: {}", ProductCatalogueException.class.getSimpleName(), message);
        return new ProductCatalogueException(errorType, message);
    }
}
