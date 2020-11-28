package com.hal9000.warehouse.inventory.adapter.out;

import static com.hal9000.warehouse.inventory.domain.ProductExamples.KALIGULA;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.NERO;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.kaligulaComponent2;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.kaligulaTable;
import static com.hal9000.warehouse.inventory.domain.ProductExamples.neroTable;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hal9000.warehouse.inventory.domain.Product;
import com.hal9000.warehouse.inventory.domain.Product.Component;
import com.hal9000.warehouse.inventory.port.out.ProductCatalogueRepository.ProductCatalogueIn;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductCatalogueTest {

    private final ProductCatalogue productCatalogue = new ProductCatalogue();

    @Test
    @DisplayName("When adding or updating products, they should be retrieved")
    void addToCatalogue() {
        productCatalogue.addToCatalogue(new ProductCatalogueIn(List.of(kaligulaTable, neroTable)));
        assertEquals(Optional.of(kaligulaTable), productCatalogue.findProductByName(KALIGULA));
        assertEquals(Optional.of(neroTable), productCatalogue.findProductByName(NERO));

        Component modifiedKaligulaComponent1 = new Component(1, 3);
        Product modifiedKaligulaTable = new Product(KALIGULA, List.of(modifiedKaligulaComponent1, kaligulaComponent2));

        productCatalogue.addToCatalogue(new ProductCatalogueIn(List.of(modifiedKaligulaTable)));
        assertEquals(Optional.of(modifiedKaligulaTable), productCatalogue.findProductByName(KALIGULA));

    }

    @Test
    @DisplayName("When trying to retrieve a non existent product, it should return empty")
    void findProductByName() {
        productCatalogue.addToCatalogue(new ProductCatalogueIn(List.of(kaligulaTable, neroTable)));
        assertEquals(empty(), productCatalogue.findProductByName("none"));

    }

    @Test
    @DisplayName("It should return the list of products in the catalogue")
    void findAllProducts() {
        productCatalogue.addToCatalogue(new ProductCatalogueIn(List.of(kaligulaTable, neroTable)));
        assertEquals(Set.of(neroTable, kaligulaTable), productCatalogue.findAllProducts());
    }
}