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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/billing")
public class BillingController {

    @Autowired 
    private BillingRepository tabRepository; 
    
    @Autowired 
    private InventoryItemRepository itemRepository;
    
    @Autowired 
    private TabItemRepository tabItemRepository;

    @GetMapping
    public String showBillingSystem(Model model) {
        List<BillingTab> activeTabs = tabRepository.findByStatus("OPEN");
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
    public String settleTab(@RequestParam Long tabId, Authentication authentication) {
        BillingTab tab = tabRepository.findById(tabId).orElse(null);
        
        if (tab != null && authentication != null) {
            tab.setStatus("SETTLED");
            tab.setSettledBy(authentication.getName()); 
            tab.setSettledAt(LocalDateTime.now());
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
            BigDecimal price = menuItem.getPrice();
            BigDecimal addedSubtotal = price.multiply(BigDecimal.valueOf(quantity));

            TabItem orderLine = tabItemRepository.findByBillingTabAndItemId(tab, itemId);

            if (orderLine != null) {
                int newQuantity = orderLine.getQuantity() + quantity;
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

            BigDecimal currentTotal = (tab.getTotalAmount() == null) ? BigDecimal.ZERO : tab.getTotalAmount();
            tab.setTotalAmount(currentTotal.add(addedSubtotal));
            tabRepository.save(tab); 
        }
        return "redirect:/billing";
    }
}