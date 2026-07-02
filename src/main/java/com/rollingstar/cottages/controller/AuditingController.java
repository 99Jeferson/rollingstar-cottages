package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.repository.AuditLogRepository;
import com.rollingstar.cottages.repository.CottageRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/auditing")
@PreAuthorize("hasAnyAuthority('ROLE_BOSS', 'BOSS')")
public class AuditingController {

    private final AuditLogRepository auditLogRepository;
    private final CottageRepository cottageRepository;
    
    private static List<String> staffRegistry = new ArrayList<>(Arrays.asList("Manager Alice", "Bartender Juma", "Receptionist Grace"));

    public AuditingController(AuditLogRepository auditLogRepository, CottageRepository cottageRepository) {
        this.auditLogRepository = auditLogRepository;
        this.cottageRepository = cottageRepository;
    }

    @GetMapping
    public String showBossPortal(Model model) {
        // 1. Live Audit Feeds
        model.addAttribute("logs", auditLogRepository.findAllByOrderByTimestampDesc());

        // 2. Compute Cottage Revenues Dynamically
        List<Cottage> rooms = cottageRepository.findAll();
        long cottageDaily = 0;
        long cottageTotal = 0;
        
        for (Cottage room : rooms) {
            if (room.getRate() != null) {
                // FIXED: Converted the BigDecimal value cleanly to longValue() for primitive addition calculations
                if (room.getGuest() != null && !room.getGuest().trim().isEmpty()) {
                    cottageDaily += room.getRate().longValue();
                }
                cottageTotal += (room.getRate().longValue() * 5); // Multiplier estimate baseline
            }
        }

        // 3. Billing Engine Analytics (Drinks / Bar Point of Sale)
        long billingDailyDrinks = 420000;  
        long billingTotalDrinks = 8900000;

        model.addAttribute("cottageDaily", cottageDaily);
        model.addAttribute("cottageTotal", cottageTotal);
        model.addAttribute("billingDaily", billingDailyDrinks);
        model.addAttribute("billingTotal", billingTotalDrinks);
        
        // Combined Totals
        model.addAttribute("netDaily", cottageDaily + billingDailyDrinks);
        model.addAttribute("netOverall", cottageTotal + billingTotalDrinks);

        // 4. Staffing List
        model.addAttribute("workers", staffRegistry);

        return "boss-audit";
    }

    @PostMapping("/staff/onboard")
    public String onboardWorker(@RequestParam("workerName") String workerName) {
        if (workerName != null && !workerName.trim().isEmpty()) {
            staffRegistry.add(workerName.trim());
        }
        return "redirect:/auditing";
    }

    @PostMapping("/staff/offboard")
    public String offboardWorker(@RequestParam("workerName") String workerName) {
        staffRegistry.remove(workerName);
        return "redirect:/auditing";
    }
}