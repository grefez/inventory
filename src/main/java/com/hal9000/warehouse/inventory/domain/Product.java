package com.hal9000.warehouse.inventory.domain;

import java.util.List;
import lombok.Value;

@Value
public class Product {

    @Value
    public static class Component {
        int articleId;
        int quantity;
    }

    String name;
    List<Component> components;
}
