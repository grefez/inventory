package com.hal9000.warehouse.inventory.application;

import static java.util.Collections.singletonList;

import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.domain.Product.Component;
import java.util.List;

public class ProductExamples {

    static final String KALIGULA = "Kaligula";
    static final String NERO = "Nero";
    static final Component kaligulaComponent1 = new Component(1, 2);
    static final Component kaligulaComponent2 = new Component(2, 4);

    static final Product kaligulaTable = new Product(KALIGULA,
        List.of(kaligulaComponent1, kaligulaComponent2));

    static final Product wrongKaligulaTable1 = new Product(KALIGULA,
        List.of(kaligulaComponent1, new Component(2, -1)));

    static final Product wrongKaligulaTable2 = new Product(KALIGULA,
        List.of(new Component(1, 0), kaligulaComponent2));

    static final Component neroComponent1 = new Component(1, 3);
    static final Component neroComponent2 = new Component(2, 5);

    static final Product neroTable = new Product(NERO,
        List.of(neroComponent1, neroComponent2));

    static final List<Product> productList = singletonList(kaligulaTable);

}
