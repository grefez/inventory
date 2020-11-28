package com.hal9000.warehouse.inventory.port.out;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.domain.Product;
import java.util.List;
import java.util.Optional;
import lombok.Value;

public interface InventoryRepository {

    @Value
    class AddInventoryIn {
        List<ArticleSupply> articleSupplies;
    }

    @Value
    class TakeFromInventoryIn {
        List<Product.Component> productComponentList;
        int quantity;
    }

    void addToInventory(AddInventoryIn addInventoryIn);

    boolean takeFromInventory(TakeFromInventoryIn articleSupplies);

    Optional<ArticleSupply> findArticleSupplyById (int articleId);

}
