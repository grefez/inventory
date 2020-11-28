package com.hal9000.warehouse.inventory.adapter.out;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hal9000.warehouse.inventory.domain.Article;
import com.hal9000.warehouse.inventory.domain.ArticleSupply;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.AddInventoryIn;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.ArticleBatch;
import com.hal9000.warehouse.inventory.port.out.InventoryRepository.TakeFromInventoryIn;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryTest {

    private static final Article leg = new Article(1, "leg");
    private static final Article screw = new Article(2, "screw");
    private static final ArticleSupply legSupply = new ArticleSupply(leg, 2);
    private static final ArticleSupply screwSupply = new ArticleSupply(screw, 4);

    private Inventory inventory = new Inventory();


    @Test
    @DisplayName("When adding or updating article supplies, they should be retrieved")
    public void addingToInventory () {
        inventory.addToInventory(new AddInventoryIn(List.of(legSupply, screwSupply)));

        assertEquals(Optional.of(legSupply), inventory.findArticleSupplyById(leg.getId()));
        assertEquals(Optional.of(screwSupply), inventory.findArticleSupplyById(screw.getId()));

        final ArticleSupply updatedLegSupply = new ArticleSupply(leg, 3);
        final ArticleSupply updatedScrewSupply = new ArticleSupply(screw, 5);

        inventory.addToInventory(new AddInventoryIn(List.of(updatedLegSupply, updatedScrewSupply)));

        assertEquals(Optional.of(updatedLegSupply), inventory.findArticleSupplyById(leg.getId()));
        assertEquals(Optional.of(updatedScrewSupply), inventory.findArticleSupplyById(screw.getId()));

    }

    @Test
    @DisplayName("When trying to retrieve a non existent article, it should return empty")
    public void lookingUpNonExistentArticle () {
        inventory.addToInventory(new AddInventoryIn(List.of(legSupply, screwSupply)));

        assertEquals(empty(), inventory.findArticleSupplyById(8));

    }

    @Test
    @DisplayName("When trying to take a list of article batches that have enough supply, it should return true and update the inventory")
    public void takingFromInventoryWithEnoughSupplies() {
        inventory.addToInventory(new AddInventoryIn(List.of(legSupply, screwSupply)));

        assertTrue(inventory.takeFromInventory(new TakeFromInventoryIn(List.of(
            new ArticleBatch(1, 1),
            new ArticleBatch(2, 2)))));

        assertEquals(Optional.of(new ArticleSupply(leg, 1)), inventory.findArticleSupplyById(1));
        assertEquals(Optional.of(new ArticleSupply(screw, 2)), inventory.findArticleSupplyById(2));

    }


    @Test
    @DisplayName("When trying to take a list of article batches that have not enough supply, it should return false and not update the inventory")
    public void takingFromInventoryWithInsufficientSupplies() {
        inventory.addToInventory(new AddInventoryIn(List.of(legSupply, screwSupply)));

        assertFalse(inventory.takeFromInventory(new TakeFromInventoryIn(List.of(
            new ArticleBatch(1, 5),
            new ArticleBatch(2, 8)))));

        assertEquals(Optional.of(legSupply), inventory.findArticleSupplyById(leg.getId()));
        assertEquals(Optional.of(screwSupply), inventory.findArticleSupplyById(screw.getId()));

    }


}