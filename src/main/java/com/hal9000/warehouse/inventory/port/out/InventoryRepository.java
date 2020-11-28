package com.hal9000.warehouse.inventory.port.out;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import java.util.List;
import java.util.Optional;
import lombok.Value;

public interface InventoryRepository {

    @Value
    class AddInventoryIn {
        List<ArticleSupply> articleSupplies;
    }

    @Value
    class ArticleBatch {
        int articleId;
        int quantity;
    }

    @Value
    class TakeFromInventoryIn {
        List<ArticleBatch> articleBatch;
    }

    void addToInventory(AddInventoryIn addInventoryIn);

    boolean takeFromInventory(TakeFromInventoryIn articleSupplies);

    Optional<ArticleSupply> findArticleSupplyById (int articleId);

}
