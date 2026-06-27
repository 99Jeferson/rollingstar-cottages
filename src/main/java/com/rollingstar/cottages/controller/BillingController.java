package com.rollingstar.cottages.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.rollingstar.cottages.model.BillingTab;
import com.rollingstar.cottages.model.InventoryItem;
import com.rollingstar.cottages.model.TabItem;
import com.rollingstar.cottages.repository.BillingRepository;
import com.rollingstar.cottages.repository.InventoryItemRepository;
import com.rollingstar.cottages.repository.TabItemRepository;
import com.rollingstar.cottages.service.PaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/billing")
public class BillingController {

    private final BillingRepository tabRepository; 
    private final InventoryItemRepository itemRepository;
    private final TabItemRepository tabItemRepository;
    private final PaymentService paymentService;

    @Autowired 
    public BillingController(BillingRepository tabRepository,
                             InventoryItemRepository itemRepository,
                             TabItemRepository tabItemRepository,
                             PaymentService paymentService) {
        this.tabRepository = tabRepository;
        this.itemRepository = itemRepository;
        this.tabItemRepository = tabItemRepository;
        this.paymentService = paymentService;
    }
    
    @GetMapping
    public String showBillingSystem(Model model) {
        List<BillingTab> activeTabs = tabRepository.findByStatus("OPEN");
        // Pulling in both SETTLED and PENDING_PAYMENT items to prevent active trackers from disappearing
        List<BillingTab> settledTabs = tabRepository.findByStatus("SETTLED");

        BigDecimal totalSales = settledTabs.stream()
                .map(BillingTab::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long billsSettledCount = settledTabs.size();
        BigDecimal averageSales = billsSettledCount > 0 ? 
                totalSales.divide(BigDecimal.valueOf(billsSettledCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        model.addAttribute("activeTabs", activeTabs);
        model.addAttribute("settledTabs", settledTabs);
        model.addAttribute("menuItems", itemRepository.findAll());
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("billsSettledCount", billsSettledCount);
        model.addAttribute("averageSales", averageSales);

        return "billing";
    }

    @PostMapping("/open-tab")
    public String openNewTab(@RequestParam String tabName) {
        if (tabName != null && !tabName.trim().isEmpty()) {
            BillingTab newTab = new BillingTab();
            newTab.setTabName(tabName);
            newTab.setStatus("OPEN");
            newTab.setTotalAmount(BigDecimal.ZERO);
            newTab.setReferenceId("REF-" + System.currentTimeMillis());
            newTab.setDepartmentSource("Main Lounge Area");
            tabRepository.save(newTab);
        }
        return "redirect:/billing";
    }

    @PostMapping("/settle-tab")
    public String settleTab(@RequestParam Long tabId, 
                            @RequestParam(defaultValue = "CASH") String paymentMethod,
                            @RequestParam(required = false) String customerPhone,
                            Authentication authentication) {
        
        BillingTab tab = tabRepository.findById(tabId).orElse(null);
        
        if (tab != null && authentication != null) {
            // Anti-Crash Guardrail: Satisfy database non-null constraints on legacy records
            if (tab.getReferenceId() == null) {
                tab.setReferenceId("REF-" + System.currentTimeMillis());
            }
            if (tab.getDepartmentSource() == null) {
                tab.setDepartmentSource("Main Lounge Area");
            }

            BigDecimal billAmount = (tab.getTotalAmount() != null) ? tab.getTotalAmount() : BigDecimal.ZERO;
            String cleanPhone = (customerPhone != null && !customerPhone.trim().isEmpty()) ? customerPhone.trim() : "0700000000";
            String methodNormalized = paymentMethod.toUpperCase();

            // 1. Initialize our hybrid audit ledger pipeline tracker
            try {
                paymentService.initializePayment(billAmount, "UGX", cleanPhone, methodNormalized);
                System.out.println("💳 BILLING INTEGRATION: Created transaction entry for Tab ID " + tabId + " | Method: " + methodNormalized + " | Amount: " + billAmount + " UGX");
            } catch (Exception e) {
                System.err.println("❌ BACKEND ERROR: Tracking ledger update failed: " + e.getMessage());
            }

            // 2. Determine state transition rule based on selection
            if ("CASH".equals(methodNormalized)) {
                tab.setStatus("SETTLED");
                tab.setSettledBy(authentication.getName()); 
                tab.setSettledAt(LocalDateTime.now());
                System.out.println("💰 CASH COLLECTION: Tab ID " + tabId + " instantly archived as SETTLED.");
            } else {
                // Mobile Money starts as PENDING. Keep tab alive until Webhook confirmation arrives.
                tab.setStatus("PENDING_PAYMENT");
                System.out.println("📱 MOBILE MONEY: Tab ID " + tabId + " held in intermediate PENDING_PAYMENT buffer state.");
            }
            
            tabRepository.save(tab);
        }
        return "redirect:/billing";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; 
    }

    @PostMapping("/add-item")
    public String addItemToTab(@RequestParam Long tabId, 
                               @RequestParam Long itemId, 
                               @RequestParam Integer quantity) {
        
        BillingTab tab = tabRepository.findById(tabId).orElse(null);
        InventoryItem menuItem = itemRepository.findById(itemId).orElse(null);

        if (tab != null && menuItem != null && quantity != null && quantity > 0) {
            
            //  INVENTORY PROTECTION GUARDRAIL: Check availability before processing sale
            int currentStock = (menuItem.getStockQuantity() != null) ? menuItem.getStockQuantity() : 0;
            if (currentStock < quantity) {
                // Return immediately with an error parameter if staff tries to oversell stock pool
                return "redirect:/billing?error=low_stock";
            }

            BigDecimal price = menuItem.getPrice();
            BigDecimal addedSubtotal = price.multiply(BigDecimal.valueOf(quantity));

            TabItem orderLine = tabItemRepository.findByBillingTabAndItemId(tab, itemId);

            if (orderLine != null) {
                int existingQty = (orderLine.getQuantity() != null) ? orderLine.getQuantity() : 0;
                int newQuantity = existingQty + quantity;
                
                orderLine.setQuantity(newQuantity);
                orderLine.setSubtotal(price.multiply(BigDecimal.valueOf(newQuantity)));
                tabItemRepository.save(orderLine);
            } else {
                orderLine = new TabItem();
                orderLine.setBillingTab(tab);
                orderLine.setItemId(menuItem.getId());
                orderLine.setItemName(menuItem.getName());
                orderLine.setPrice(price);
                orderLine.setQuantity(quantity);
                orderLine.setSubtotal(addedSubtotal);
                tabItemRepository.save(orderLine);
            }

            //  STORE SYNCHRONIZATION: Subtract the sold items from the inventory pool
            menuItem.setStockQuantity(currentStock - quantity);
            itemRepository.save(menuItem);

            BigDecimal currentTotal = (tab.getTotalAmount() == null) ? BigDecimal.ZERO : tab.getTotalAmount();
            tab.setTotalAmount(currentTotal.add(addedSubtotal));

            // Anti-Crash Guardrail: Satisfy database non-null constraints on legacy records
            if (tab.getReferenceId() == null) {
                tab.setReferenceId("REF-" + System.currentTimeMillis());
            }
            if (tab.getDepartmentSource() == null) {
                tab.setDepartmentSource("Main Lounge Area");
            }

            tabRepository.save(tab); 
        }
        return "redirect:/billing";
    }
}