package com.hal9000.warehouse.inventory.domain;

import static java.util.Collections.singletonList;

import com.hal9000.warehouse.inventory.domain.Product.Component;
import java.util.List;

public class ProductExamples {

    public static final String KALIGULA = "Kaligula";
    public static final String NERO = "Nero";
    public static final Component kaligulaComponent1 = new Component(1, 2);
    public static final Component kaligulaComponent2 = new Component(2, 4);

    public static final Product kaligulaTable = new Product(KALIGULA,
        List.of(kaligulaComponent1, kaligulaComponent2));

    public static final Product wrongKaligulaTable1 = new Product(KALIGULA,
        List.of(kaligulaComponent1, new Component(2, -1)));

    public static final Product wrongKaligulaTable2 = new Product(KALIGULA,
        List.of(new Component(1, 0), kaligulaComponent2));

    public static final Component neroComponent1 = new Component(1, 3);
    public static final Component neroComponent2 = new Component(2, 5);

    public static final Product neroTable = new Product(NERO,
        List.of(neroComponent1, neroComponent2));

    public static final List<Product> productList = singletonList(kaligulaTable);

}
