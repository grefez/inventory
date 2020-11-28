package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.INVALID_QUANTITY;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_ARTICLES;
import static com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType.NON_EXISTENT_PRODUCT;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hal9000.warehouse.inventory.domain.Article;
import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.domain.Product.Component;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.AvailableProduct;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.AvailableProducts;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ProductCatalogueException;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ProductCatalogueIn;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.TakeFromInventoryIn;
import com.hal9000.warehouse.inventory.port.out.ProductCatalogueRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ProductCatalogueServiceTest {

    private static final String KALIGULA = "Kaligula";
    private static final String NERO = "Nero";

    public static final int PRODUCT_QUANTITY = 2;

    @Mock
    private ProductCatalogueRepository productCatalogueRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductCatalogueService productCatalogueService;

    private final Component kaligulaComponent1 = new Component(1, 2);
    private final Component kaligulaComponent2 = new Component(2, 4);

    private final Product kaligulaTable = new Product(KALIGULA,
        List.of(kaligulaComponent1, kaligulaComponent2));

    private final Product wrongKaligulaTable1 = new Product(KALIGULA,
        List.of(kaligulaComponent1, new Component(2, -1)));

    private final Product wrongKaligulaTable2 = new Product(KALIGULA,
        List.of(new Component(1, 0), kaligulaComponent2));

    private final Component neroComponent1 = new Component(1, 3);
    private final Component neroComponent2 = new Component(2, 5);

    private final Product neroTable = new Product(NERO,
        List.of(neroComponent1, neroComponent2));

    private final List<Product> productList = singletonList(kaligulaTable);

    @Test
    @DisplayName("When adding products to catalogue which components are in inventory, should not raise any errors")
    public void addToCatalogue () {
        when(inventoryRepository.findArticleSupplyById(kaligulaComponent1.getArticleId()))
            .thenReturn(Optional.of(new ArticleSupply(new Article(1, "leg"), 1)));

        when(inventoryRepository.findArticleSupplyById(kaligulaComponent2.getArticleId()))
            .thenReturn(Optional.of(new ArticleSupply(new Article(2, "screw"), 1)));

        productCatalogueService.addToCatalogue(new ProductCatalogueIn(productList));

        verify(productCatalogueRepository).addToCatalogue(new ProductCatalogueRepository.ProductCatalogueIn(productList));
    }

    @Test
    @DisplayName("When adding products to catalogue with invalid articles, should raise an error")
    public void addToCatalogueInvalidArticles () {

        when(inventoryRepository.findArticleSupplyById(kaligulaComponent1.getArticleId())).thenReturn(empty());

        validateError(() -> productCatalogueService.addToCatalogue(new ProductCatalogueIn(productList)),
            NON_EXISTENT_ARTICLES);

    }

    @Test
    @DisplayName("When adding products to catalogue with invalid quantities, should raise an error")
    public void addToCatalogueInvalidQuantities () {
        validateError(() -> productCatalogueService.addToCatalogue(new ProductCatalogueIn(singletonList(wrongKaligulaTable1))), INVALID_QUANTITY);
        validateError(() -> productCatalogueService.addToCatalogue(new ProductCatalogueIn(singletonList(wrongKaligulaTable2))), INVALID_QUANTITY);
    }


    @Test
    @DisplayName("When selling an existent product with enough supplies, should return true")
    public void sellingProduct() {
        when(productCatalogueRepository.findProductByName(eq(KALIGULA))).thenReturn(Optional.of(kaligulaTable));

        when(inventoryRepository.takeFromInventory(new TakeFromInventoryIn(
            List.of(
                new Component(kaligulaComponent1.getArticleId(),kaligulaComponent1.getQuantity()),
                new Component(kaligulaComponent2.getArticleId(),kaligulaComponent2.getQuantity())),
            PRODUCT_QUANTITY)))
            .thenReturn(true);

        assertTrue(productCatalogueService.sellProduct(KALIGULA, PRODUCT_QUANTITY));

    }

    @Test
    @DisplayName("When selling an existent product with not enough supplies, should return false")
    public void sellingProductsWithNotEnoughInventory () {
        when(productCatalogueRepository.findProductByName(KALIGULA)).thenReturn(Optional.of(kaligulaTable));
        when(inventoryRepository.takeFromInventory(any(TakeFromInventoryIn.class))).thenReturn(false);
        assertFalse(productCatalogueService.sellProduct(KALIGULA, PRODUCT_QUANTITY));

    }

    @Test
    @DisplayName("When selling a non existent product, should raise an error")
    public void sellingNonExistentProduct () {
        when(productCatalogueRepository.findProductByName(KALIGULA)).thenReturn(empty());
        validateError(() -> productCatalogueService.sellProduct(KALIGULA, PRODUCT_QUANTITY), NON_EXISTENT_PRODUCT);
    }


    @Test
    @DisplayName("When selling a product with negative quantity, should raise an error")
    public void sellingNegativeQuantity () {
        validateError(() -> productCatalogueService.sellProduct(KALIGULA, 0), INVALID_QUANTITY);
        validateError(() -> productCatalogueService.sellProduct(KALIGULA, -1), INVALID_QUANTITY);
    }

    @Test
    @DisplayName("Should return a list of products that can be sold")
    public void listOfProductsThatCanBeSold() {

        when(productCatalogueRepository.findAllProducts()).thenReturn(List.of(kaligulaTable, neroTable));
        when(inventoryRepository.findArticleSupplyById(1))
            .thenReturn(Optional.of(new ArticleSupply(new Article(1, "leg"), 9)));
        when(inventoryRepository.findArticleSupplyById(2))
            .thenReturn(Optional.of(new ArticleSupply(new Article(2, "screw"), 12)));

        assertEquals(
            new AvailableProducts(List.of(
                new AvailableProduct(3, KALIGULA),
                new AvailableProduct(2, NERO))),
            productCatalogueService.getAvailableProducts());

    }

    private void validateError(Executable executable, ErrorType errorType) {
        ProductCatalogueException productCatalogueException = assertThrows(ProductCatalogueException.class,
            executable);

        assertEquals(errorType, productCatalogueException.getErrorType());
    }


}