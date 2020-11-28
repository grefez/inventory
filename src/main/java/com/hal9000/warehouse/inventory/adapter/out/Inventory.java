package com.hal9000.warehouse.inventory.adapter.out;

import com.hal9000.warehouse.inventory.domain.Article;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;

public class Inventory {

    @Value
    private static class ArticleSupply {
        int quantity;
        Article article;
    }

    private static final Map<Integer, ArticleSupply> instance = new HashMap<>();

}
