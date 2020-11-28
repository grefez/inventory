package com.hal9000.warehouse.inventory.adapter.in;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hal9000.warehouse.inventory.domain.Article;
import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.port.in.InventoryUseCase;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryUseCase inventoryUseCase;

    @Value
    @NoArgsConstructor(force = true, access = PRIVATE)
    @AllArgsConstructor
    static class AddInventoryIn {

        @Value
        @NoArgsConstructor(force = true, access = PRIVATE)
        @AllArgsConstructor
        static class Article {

            @JsonProperty("art_id")
            int articleId;

            String name;
            int stock;

        }

        List<Article> inventory;
    }


    @PostMapping("update")
    public void addInventory(@RequestBody AddInventoryIn addInventoryIn) {
        inventoryUseCase.addToInventory(getAddInventoryIn(addInventoryIn));
    }

    private InventoryUseCase.AddInventoryIn getAddInventoryIn(AddInventoryIn addInventoryIn) {
        return new InventoryUseCase.AddInventoryIn(
            ofNullable(addInventoryIn.inventory).map(
                inventory -> inventory.stream()
                    .map(article -> new ArticleSupply(new Article(article.getArticleId(), article.name), article.stock))
                    .collect(toList()))
            .orElse(Collections.emptyList())
        );
    }

}
