package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.InventoryItem;
import com.rollingstar.cottages.repository.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/inventory")
@PreAuthorize("hasAnyRole('MANAGER', 'BOSS')")
public class InventoryController {

    @Autowired
    private InventoryItemRepository itemRepository;

    /**
     * Renders the master inventory workspace page
     */
    @GetMapping
    public String viewInventoryDashboard(Model model) {
        model.addAttribute("inventoryItems", itemRepository.findAll());
        return "inventory"; 
    }

    /**
     * Handles restocking stock numbers
     */
    @PostMapping("/restock")
    public String processItemRestock(
            @RequestParam("itemId") Long itemId,
            @RequestParam("quantity") Integer quantity) {
        
        Optional<InventoryItem> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            InventoryItem item = itemOptional.get();
            int currentStock = item.getStockQuantity() != null ? item.getStockQuantity() : 0;
            item.setStockQuantity(currentStock + quantity);
            itemRepository.save(item);
        }
        return "redirect:/inventory";
    }

    /**
     * Updates the retail selling menu price configuration using BigDecimal
     */
    @PostMapping("/update-price")
    public String updateRetailSellingPrice(
            @RequestParam("itemId") Long itemId,
            @RequestParam("newPrice") BigDecimal newPrice) {
        
        Optional<InventoryItem> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            InventoryItem item = itemOptional.get();
            item.setPrice(newPrice);
            itemRepository.save(item);
        }
        return "redirect:/inventory";
    }
}