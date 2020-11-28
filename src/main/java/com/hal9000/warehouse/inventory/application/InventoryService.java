package com.hal9000.warehouse.inventory.application;

import static com.hal9000.warehouse.inventory.port.in.InventoryUseCase.ErrorType.INVALID_QUANTITY;

import com.hal9000.warehouse.inventory.port.in.InventoryUseCase;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryService implements InventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public void addToInventory(AddInventoryIn addInventoryIn) {
        boolean invalidQuantitiesExist = addInventoryIn.getArticleSupplies().stream()
            .anyMatch(articleSupply -> articleSupply.getQuantity() <= 0);

        if (invalidQuantitiesExist)
            throw new InventoryException(INVALID_QUANTITY, "All article quantities must be >= 0");

        inventoryRepository.addToInventory(new InventoryRepository.AddInventoryIn(addInventoryIn.getArticleSupplies()));
    }
}
