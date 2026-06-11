package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.InventoryItem;
import com.rollingstar.cottages.model.InventoryRestockLedger;
import com.rollingstar.cottages.repository.InventoryItemRepository;
import com.rollingstar.cottages.repository.InventoryRestockLedgerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryItemRepository itemRepository;
    private final InventoryRestockLedgerRepository ledgerRepository;

    public InventoryController(InventoryItemRepository itemRepository, InventoryRestockLedgerRepository ledgerRepository) {
        this.itemRepository = itemRepository;
        this.ledgerRepository = ledgerRepository;
    }

    // View Inventory & Restock Ledger Dashboard
    @GetMapping
    public String showInventoryDashboard(Model model) {
        model.addAttribute("inventoryItems", itemRepository.findAll());
        model.addAttribute("restockHistory", ledgerRepository.findAllByOrderByRestockedAtDesc());
        return "inventory"; // Looks for inventory.html template
    }

    // Action: Add New Base Item to Store (Manager/Boss Only)
    @PreAuthorize("hasAnyRole('MANAGER', 'BOSS')")
    @PostMapping("/add-new-item")
    public String addNewItem(@RequestParam String name, 
                             @RequestParam BigDecimal price, 
                             @RequestParam String category) {
        if (name != null && !name.trim().isEmpty() && price != null) {
            InventoryItem item = new InventoryItem();
            item.setName(name);
            item.setPrice(price);
            item.setCategory(category);
            // Default initial code-level stock value to 0; triggers handle loading increments
            // Ensure you add getter/setter for stockQuantity in InventoryItem if tracking quantities
            itemRepository.save(item);
        }
        return "redirect:/inventory";
    }

    // Action: Log Restock Event (Manager/Boss Only)
    @PreAuthorize("hasAnyRole('MANAGER', 'BOSS')")
    @PostMapping("/restock")
    public String restockItem(@RequestParam Long itemId, 
                              @RequestParam Integer quantity, 
                              @RequestParam BigDecimal costPrice,
                              Authentication authentication) {
        
        Optional<InventoryItem> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isPresent() && quantity != null && quantity > 0 && authentication != null) {
            InventoryRestockLedger transaction = new InventoryRestockLedger();
            transaction.setItem(optionalItem.get());
            transaction.setQuantityAdded(quantity);
            transaction.setCostPrice(costPrice);
            // Grabs the username of the active logged-in manager automatically
            transaction.setRestockedBy(authentication.getName()); 

            // Saving logs the entry to the database, firing off the Postgres Trigger instantly!
            ledgerRepository.save(transaction);
        }
        return "redirect:/inventory";
    }

    // Action: Update Selling Price (Manager/Boss Only)
    @PreAuthorize("hasAnyRole('MANAGER', 'BOSS')")
    @PostMapping("/update-price")
    public String updateItemPrice(@RequestParam Long itemId, @RequestParam BigDecimal newPrice) {
        Optional<InventoryItem> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isPresent() && newPrice != null && newPrice.compareTo(BigDecimal.ZERO) >= 0) {
            InventoryItem item = optionalItem.get();
            item.setPrice(newPrice);
            itemRepository.save(item);
        }
        return "redirect:/inventory";
    }
}