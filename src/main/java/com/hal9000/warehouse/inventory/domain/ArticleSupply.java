package com.hal9000.warehouse.inventory.domain;

import lombok.Value;

@Value
public class ArticleSupply {
    Article article;
    int quantity;

}
