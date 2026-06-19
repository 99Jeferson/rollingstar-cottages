package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.repository.StaffRepository;
import com.rollingstar.cottages.repository.CottageRepository;
import com.rollingstar.cottages.repository.PaymentRepository; 
import com.rollingstar.cottages.repository.InventoryItemRepository; // ✅ Imported your exact repository here
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired 
    private CottageRepository cottageRepository; 

    @Autowired 
    private PaymentRepository paymentRepository;

    @Autowired 
    private InventoryItemRepository inventoryItemRepository; // ✅ Injected your repository cleanly here

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        
        // 1. LIVE COTTAGE METRICS
        long liveOccupiedCottages = cottageRepository.countByStatus("OCCUPIED");
        model.addAttribute("occupiedCottages", liveOccupiedCottages);
        
        // 2. LIVE FINANCIAL METRICS
        Long todayRevenueSum = paymentRepository.calculateRevenueSumByDate(LocalDate.now());
        long todayRevenue = (todayRevenueSum != null) ? todayRevenueSum : 0L;
        model.addAttribute("todayRevenue", todayRevenue);
        
        // 3. LIVE STAFF SHIFTS
        long activeStaffCount = staffRepository.findAll().stream()
                .filter(staff -> "ACTIVE".equalsIgnoreCase(staff.getEmploymentStatus()))
                .count();
        model.addAttribute("activeStaffCount", activeStaffCount);
        
        // 4. LIVE LOW STOCK ALERTS: Uses your repository to count items with 5 or fewer items left
        long liveLowStockAlerts = inventoryItemRepository.countLowStockItems(5);
        model.addAttribute("lowStockAlerts", liveLowStockAlerts);
        
        // 5. LIVE RECENT ROSTER DEPLOYMENTS LIST
        model.addAttribute("activeStaffList", staffRepository.findAll());

        return "dashboard"; 
    }
}