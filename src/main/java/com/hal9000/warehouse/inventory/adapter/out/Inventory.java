package com.hal9000.warehouse.inventory.adapter.out;

import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class Inventory implements InventoryRepository {

    private final Map<Integer, ArticleSupply> instance = new ConcurrentHashMap<>();

    public void addToInventory(AddInventoryIn addInventoryIn) {
        addInventoryIn.getArticleSupplies()
            .forEach(articleSupply -> instance.put(articleSupply.getArticle().getId(), articleSupply));

    }

    public boolean takeFromInventory(TakeFromInventoryIn takeFromInventoryIn) {
        synchronized (instance) {
            boolean enoughInventoryPresent = takeFromInventoryIn.getArticleBatchList().stream()
                .allMatch(articleBatch -> instance.get(articleBatch.getArticleId()).getQuantity() >= articleBatch.getQuantity());
            if (enoughInventoryPresent) {
                takeFromInventoryIn.getArticleBatchList()
                    .forEach(articleBatch -> instance.put(articleBatch.getArticleId(), getUpdatedArticleSupply(articleBatch)));
            }

            return enoughInventoryPresent;
        }
    }

    private ArticleSupply getUpdatedArticleSupply(ArticleBatch articleBatch) {
        int articleId = articleBatch.getArticleId();
        ArticleSupply currentArticleSupply = instance.get(articleId);

        return new ArticleSupply(currentArticleSupply.getArticle(), currentArticleSupply.getQuantity() - articleBatch.getQuantity());
    }

    public Optional<ArticleSupply> findArticleSupplyById(int articleId) {
        return Optional.ofNullable(instance.get(articleId));
    }



}
