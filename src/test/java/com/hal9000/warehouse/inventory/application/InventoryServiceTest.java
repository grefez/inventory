package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.port.in.InventoryUseCase.ErrorType.INVALID_QUANTITY;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.hal9000.warehouse.inventory.domain.Article;
import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.port.in.InventoryUseCase.AddInventoryIn;
import com.hal9000.warehouse.inventory.port.in.InventoryUseCase.InventoryException;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Add articles to inventory")
    public void addToInventory () {
        List<ArticleSupply> articleSupplies = singletonList(
            new ArticleSupply(
                new Article(2, "leg"), 1));

        inventoryService.addToInventory(new AddInventoryIn(articleSupplies));

        verify(inventoryRepository).addToInventory(new InventoryRepository.AddInventoryIn(articleSupplies));

    }

    @Test
    @DisplayName("When adding articles to inventory with invalid quantities, should raise an error")
    public void addToInventoryWrongQuantity () {
        List<ArticleSupply> articleSupplies = singletonList(
            new ArticleSupply(
                new Article(2, "leg"), -1));

        InventoryException inventoryException = assertThrows(InventoryException.class,
            () -> inventoryService.addToInventory(new AddInventoryIn(articleSupplies)));

        assertEquals(INVALID_QUANTITY, inventoryException.getErrorType());

    }

}