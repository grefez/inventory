package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.domain.ProductExamples.KALIGULA;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.NERO;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.kaligulaComponent1;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.kaligulaComponent2;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.kaligulaTable;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.neroTable;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.productList;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.wrongKaligulaTable1;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.wrongKaligulaTable2;
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
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.AvailableProduct;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.AvailableProducts;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ErrorType;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ProductCatalogueException;
import com.hal9000.warehouse.inventory.port.in.ProductCatalogueUseCase.ProductCatalogueIn;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.ArticleBatch;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.TakeFromInventoryIn;
import com.hal9000.warehouse.inventory.port.out.ProductCatalogueRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ProductCatalogueServiceTest {

    @Mock
    private ProductCatalogueRepository productCatalogueRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductCatalogueService productCatalogueService;

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
                new ArticleBatch(kaligulaComponent1.getArticleId(),kaligulaComponent1.getQuantity() * 2),
                new ArticleBatch(kaligulaComponent2.getArticleId(),kaligulaComponent2.getQuantity() * 2)))))
            .thenReturn(true);

        assertTrue(productCatalogueService.sellProduct(KALIGULA, 2));

    }

    @Test
    @DisplayName("When selling an existent product with not enough supplies, should return false")
    public void sellingProductsWithNotEnoughInventory () {
        when(productCatalogueRepository.findProductByName(KALIGULA)).thenReturn(Optional.of(kaligulaTable));
        when(inventoryRepository.takeFromInventory(any(TakeFromInventoryIn.class))).thenReturn(false);
        assertFalse(productCatalogueService.sellProduct(KALIGULA, 2));

    }

    @Test
    @DisplayName("When selling a non existent product, should raise an error")
    public void sellingNonExistentProduct () {
        when(productCatalogueRepository.findProductByName(KALIGULA)).thenReturn(empty());
        validateError(() -> productCatalogueService.sellProduct(KALIGULA, 2), NON_EXISTENT_PRODUCT);
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

        when(productCatalogueRepository.findAllProducts()).thenReturn(Set.of(kaligulaTable, neroTable));
        when(inventoryRepository.findArticleSupplyById(1))
            .thenReturn(Optional.of(new ArticleSupply(new Article(1, "leg"), 9)));
        when(inventoryRepository.findArticleSupplyById(2))
            .thenReturn(Optional.of(new ArticleSupply(new Article(2, "screw"), 12)));

        assertEquals(
            new AvailableProducts(Set.of(
                new AvailableProduct(3, KALIGULA),
                new AvailableProduct(2, NERO))),
            productCatalogueService.getAvailableProducts());

    }

    @Test
    @DisplayName("Should not include a product in the list of products that can be sold if there is no inventory for it")
    public void productsThatCanNotBeSold () {

        when(productCatalogueRepository.findAllProducts()).thenReturn(Set.of(kaligulaTable, neroTable));
        when(inventoryRepository.findArticleSupplyById(1))
            .thenReturn(Optional.of(new ArticleSupply(new Article(1, "leg"), 2)));
        when(inventoryRepository.findArticleSupplyById(2))
            .thenReturn(Optional.of(new ArticleSupply(new Article(2, "screw"), 4)));

        assertEquals(
            new AvailableProducts(Set.of(
                new AvailableProduct(1, KALIGULA))),
            productCatalogueService.getAvailableProducts());

    }

    private void validateError(Executable executable, ErrorType errorType) {
        ProductCatalogueException productCatalogueException = assertThrows(ProductCatalogueException.class,
            executable);

        assertEquals(errorType, productCatalogueException.getErrorType());
    }


}