package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.port.in.InventoryUseCase.ErrorType.INVALID_QUANTITY;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.port.in.InventoryUseCase;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements InventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public void addToInventory(AddInventoryIn addInventoryIn) {

        if (invalidQuantitiesFound(addInventoryIn)) {
            raiseError("All article quantities must be >= 0");
        }

        inventoryRepository.addToInventory(new InventoryRepository.AddInventoryIn(addInventoryIn.getArticleSupplies()));
        log.info("Articles '{}' were added to inventory", getArticleNames(addInventoryIn.getArticleSupplies()));
    }

    private boolean invalidQuantitiesFound(AddInventoryIn addInventoryIn) {
        return addInventoryIn.getArticleSupplies().stream()
            .anyMatch(articleSupply -> articleSupply.getQuantity() <= 0);
    }

    private String getArticleNames(List<ArticleSupply> articleSupplies) {
        return arrayToCommaDelimitedString(articleSupplies.stream()
            .map(articleSupply -> articleSupply.getArticle().getName())
            .toArray());
    }

    private void raiseError(String message) {
        log.error("{}: {}", InventoryException.class.getSimpleName(), message);
        throw new InventoryException(INVALID_QUANTITY, message);
    }
}
