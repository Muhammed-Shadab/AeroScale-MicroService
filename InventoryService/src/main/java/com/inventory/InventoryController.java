package com.inventory;

import com.inventory.DTO.Request.ReserveStock;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inv")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/getProductStock/{productId}")
    public int getStockOfProduct(@PathVariable UUID productId) {
        return inventoryService.getStock(productId);
    }

    @PutMapping("/initializeInventory/{productId}")
    public ResponseEntity<?> initializeInventory(@PathVariable UUID productId) {
        inventoryService.initializeInventory(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Inventory Updated Sucesfully Successfully");
    }

    @PostMapping("/getStockMetaData")
    public ResponseEntity<List<List<Integer>>> getTotalStockThatAdded(@RequestBody List<UUID> productIdList) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.getTotalStockThatAdded(productIdList));
    }

    @PutMapping("/addStock/{productId}/{quantity}")
    public ResponseEntity<?> addStock(@PathVariable UUID productId
                                    , @PathVariable int quantity
                                    , @AuthenticationPrincipal(expression = "id") UUID sellerId) {
        inventoryService.addStock(productId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body("stock successfully updated");
    }

    @PostMapping("/updateStock")
    public ResponseEntity<?> updateStock(@RequestBody ReserveStock stocks) {
        inventoryService.reserveStock(stocks);
        return ResponseEntity.status(HttpStatus.CREATED).body("Stocks Updated Succesfully");
    }

    @PutMapping("/releaseStock/{orderId}")
    public ResponseEntity<?> releaseStock(@PathVariable UUID orderId) {
        inventoryService.releaseStock(orderId);
        return ResponseEntity.ok("Stock successfully released for order: " + orderId);
    }


    @PutMapping("/confirmStock/{orderId}")
    public ResponseEntity<?> confirmStock(@PathVariable UUID orderId) {
        inventoryService.confirmStock(orderId);
        return ResponseEntity.ok("Stock confirmed for order: " + orderId);
    }

}
