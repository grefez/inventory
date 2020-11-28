package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.INVALID_QUANTITY;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_ARTICLES;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_PRODUCT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.ArticleBatch;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.TakeFromInventoryIn;
import com.hal9000.warehouse.inventory.port.out.ProductCatalogueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCatalogueService implements ProductCatalogueUseCase {

    private final ProductCatalogueRepository productCatalogueRepository;
    private final InventoryRepository inventoryRepository;


    public void addToCatalogue(ProductCatalogueIn productCatalogueIn) {

        List<Product.Component> componentList = productCatalogueIn.getProductList().stream()
            .flatMap(product -> product.getComponents().stream()).collect(toList());

        if (componentList.stream().anyMatch(component -> component.getQuantity() <= 0)) {
            throw new ProductCatalogueException(
                INVALID_QUANTITY, "Article quantities must be > 0");
        }

        List<Integer> articlesIdsNotInCatalogue = componentList.stream()
            .map (Product.Component::getArticleId)
            .filter(articleId -> inventoryRepository.findArticleSupplyById(articleId).isEmpty())
            .collect(toList());

        if (!articlesIdsNotInCatalogue.isEmpty()) {
            throw new ProductCatalogueException(
                NON_EXISTENT_ARTICLES, format("Articles %s are not in the catalogue", articlesIdsNotInCatalogue));
        }

        productCatalogueRepository.addToCatalogue(new ProductCatalogueRepository.ProductCatalogueIn(productCatalogueIn.getProductList()));

    }

    public boolean sellProduct(String productName, int productQuantity) {

        if (productQuantity <= 0)
            throw new ProductCatalogueException(INVALID_QUANTITY, "Product quantity must be > 0");

        return productCatalogueRepository.findProductByName(productName)
            .map (product -> product.getComponents().stream()
                .map(component -> new Product.Component(component.getArticleId(), component.getQuantity()))
                .collect(toList()))
            .map(componentList -> inventoryRepository.takeFromInventory(
                new TakeFromInventoryIn(componentList.stream()
                    .map(component -> new ArticleBatch(component.getArticleId(), component.getQuantity() * productQuantity))
                    .collect(toList()))))
            .orElseThrow(() -> new ProductCatalogueException(NON_EXISTENT_PRODUCT, format("Product with name %s does not exist in catalogue", productName)));

    }

    public AvailableProducts getAvailableProducts() {
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
}
