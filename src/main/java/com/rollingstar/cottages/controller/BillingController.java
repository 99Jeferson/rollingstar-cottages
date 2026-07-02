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
import com.rollingstar.cottages.service.MobileMoneyService; //  Injected your background telecom runner class

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
    private final MobileMoneyService mobileMoneyService; // ✅ Declared service field

    @Autowired 
    public BillingController(BillingRepository tabRepository,
                             InventoryItemRepository itemRepository,
                             TabItemRepository tabItemRepository,
                             PaymentService paymentService,
                             MobileMoneyService mobileMoneyService) { // ✅ Wired into constructor setup
        this.tabRepository = tabRepository;
        this.itemRepository = itemRepository;
        this.tabItemRepository = tabItemRepository;
        this.paymentService = paymentService;
        this.mobileMoneyService = mobileMoneyService;
    }
    
    @GetMapping
    public String showBillingSystem(Model model) {
        List<BillingTab> activeTabs = tabRepository.findByStatus("OPEN");
        // Pulling in both SETTLED and PENDING_PAYMENT items to prevent active trackers from disappearing
        //  Includes PENDING_PAYMENT tabs so they don't vanish from layout panels while waiting for a PIN
        List<BillingTab> pendingTabs = tabRepository.findByStatus("PENDING_PAYMENT");
        List<BillingTab> settledTabs = tabRepository.findByStatus("SETTLED");

        BigDecimal totalSales = settledTabs.stream()
                .map(BillingTab::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long billsSettledCount = settledTabs.size();
        BigDecimal averageSales = billsSettledCount > 0 ? 
                totalSales.divide(BigDecimal.valueOf(billsSettledCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        model.addAttribute("activeTabs", activeTabs);
        model.addAttribute("pendingTabs", pendingTabs); //  Added parameter visibility mapping
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
            String methodNormalized = paymentMethod.toUpperCase();

            // 📱 TARGET INTEGRATION METRICS: LOUNGE MOBILE MONEY ROUTE
            if ("MOBILE_MONEY".equals(methodNormalized)) {
                // 1. Sanitize user input phone strings securely into 256 format strings
                String cleanPhone = sanitizeUgandanPhoneNumber(customerPhone);
                if (cleanPhone == null) {
                    System.err.println(" VALIDATION FAILURE: Aborting Bar STK push invocation. Phone format invalid.");
                    return "redirect:/billing?error=invalid_phone";
                }

                // 2. Put the lounge bar bill into PENDING_PAYMENT status buffer space
                tab.setStatus("PENDING_PAYMENT");
                tabRepository.save(tab);

                // 3. Initialize dynamic accounting audit trail ledger
                try {
                    paymentService.initializePayment(billAmount, "UGX", cleanPhone, methodNormalized);
                } catch (Exception e) {
                    System.err.println(" Warning: Ledger transaction history tracking failed: " + e.getMessage());
                }

                // 4. Fire the remote API payload request to prompt customer's handset device asynchronously
                try {
                    // Custom structured key code matching webhooks: "BAR-TAB-[Id]"
                    String barTxCode = "BAR-TAB-" + tabId;
                    mobileMoneyService.triggerPushPrompt(cleanPhone, billAmount.doubleValue(), barTxCode);
                    System.out.println(" LOUNGE MOMO PROMPT: Dispatched STK Push for Tab ID: " + tabId + " | Phone: " + cleanPhone);
                } catch (Exception e) {
                    System.err.println("❌ BACKEND ERROR: Target connection to telecom outbound framework failed: " + e.getMessage());
                }

                return "redirect:/billing?success=momo_prompted";
            }

            // 💵 STANDARD CASH COLLECTION TERMINATION LAYER
            String cleanCashPhone = (customerPhone != null && !customerPhone.trim().isEmpty()) ? customerPhone.trim() : "0700000000";
            try {
                paymentService.initializePayment(billAmount, "UGX", cleanCashPhone, methodNormalized);
                System.out.println("💳 BILLING INTEGRATION: Created cash entry for Tab ID " + tabId + " | Amount: " + billAmount + " UGX");
            } catch (Exception e) {
                System.err.println("❌ BACKEND ERROR: Tracking cash log allocation failed: " + e.getMessage());
            }

            tab.setStatus("SETTLED");
            tab.setSettledBy(authentication.getName()); 
            tab.setSettledAt(LocalDateTime.now());
            tabRepository.save(tab);
            System.out.println(" CASH COLLECTION: Tab ID " + tabId + " instantly archived as SETTLED.");
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
            
            // INVENTORY PROTECTION GUARDRAIL: Check availability before processing sale
            int currentStock = (menuItem.getStockQuantity() != null) ? menuItem.getStockQuantity() : 0;
            if (currentStock < quantity) {
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

            // STORE SYNCHRONIZATION: Subtract the sold items from the inventory pool
            menuItem.setStockQuantity(currentStock - quantity);
            itemRepository.save(menuItem);

            BigDecimal currentTotal = (tab.getTotalAmount() == null) ? BigDecimal.ZERO : tab.getTotalAmount();
            tab.setTotalAmount(currentTotal.add(addedSubtotal));

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

    /**
     * Parsing utility cleaner checking structural accuracy for Ugandan wallet codes
     */
    private String sanitizeUgandanPhoneNumber(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D+", ""); 
        
        if (digits.startsWith("0") && digits.length() == 10) {
            return "256" + digits.substring(1);
        } else if (digits.startsWith("256") && digits.length() == 12) {
            return digits;
        } else if (digits.length() == 9 && (digits.startsWith("77") || digits.startsWith("78") || digits.startsWith("70") || digits.startsWith("75") || digits.startsWith("74") || digits.startsWith("79") || digits.startsWith("76"))) {
            return "256" + digits;
        }
        return null; 
    }
}