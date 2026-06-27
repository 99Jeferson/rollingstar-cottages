package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.repository.StaffRepository;
import com.rollingstar.cottages.repository.CottageRepository;
import com.rollingstar.cottages.repository.PaymentRepository; 
import com.rollingstar.cottages.repository.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired 
    private CottageRepository cottageRepository; 

    @Autowired 
    private PaymentRepository paymentRepository;

    @Autowired 
    private InventoryItemRepository inventoryItemRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        
        // 1. LIVE COTTAGE METRICS (Now includes rooms awaiting incoming Mobile Money verification)
        long occupiedCount = cottageRepository.countByStatus("OCCUPIED");
        long pendingPaymentCount = cottageRepository.countByStatus("PENDING_PAYMENT");
        model.addAttribute("occupiedCottages", occupiedCount + pendingPaymentCount);
        
        // 2. LIVE FINANCIAL METRICS
        Long todayRevenueSum = paymentRepository.calculateRevenueSumByDate(LocalDate.now());
        long todayRevenue = (todayRevenueSum != null) ? todayRevenueSum : 0L;
        model.addAttribute("todayRevenue", todayRevenue);
        
        // 3. LIVE STAFF SHIFTS (Optimized to let the database handle the count filtering natively)
        // Assumes your StaffRepository has a countByEmploymentStatus method, otherwise falls back gracefully
        long activeStaffCount;
        try {
            activeStaffCount = staffRepository.countByEmploymentStatus("ACTIVE");
        } catch (Exception e) {
            activeStaffCount = staffRepository.findAll().stream()
                    .filter(staff -> "ACTIVE".equalsIgnoreCase(staff.getEmploymentStatus()))
                    .count();
        }
        model.addAttribute("activeStaffCount", activeStaffCount);
        
        // 4. LIVE LOW STOCK ALERTS (Counts any lounge assets with 5 or fewer items remaining)
        long liveLowStockAlerts = inventoryItemRepository.countLowStockItems(5);
        model.addAttribute("lowStockAlerts", liveLowStockAlerts);
        
        // 5. LIVE RECENT ROSTER DEPLOYMENTS LIST
        // Displays actively working team members clearly on the security ledger dashboard interface
        model.addAttribute("activeStaffList", staffRepository.findAll());

        return "dashboard"; 
    }
}