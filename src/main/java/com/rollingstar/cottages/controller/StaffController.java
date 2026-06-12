package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.Staff;
import com.rollingstar.cottages.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/staffing")
@PreAuthorize("hasAnyRole('MANAGER', 'BOSS')") // Strict Privacy Guardrail
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    /**
     * Renders the master staffing workspace dashboard overview view
     */
    @GetMapping
    public String displayStaffWorkspace(Model model) {
        model.addAttribute("staffList", staffRepository.findAllByOrderByEmploymentStatusAscFullNameAsc());
        model.addAttribute("newStaffForm", new Staff());
        return "staffing"; // Mapped to staffing.html template later
    }

    /**
     * Onboards a brand new employee worker profile to the payroll framework roster
     */
    @PostMapping("/onboard")
    public String onboardNewStaffMember(@ModelAttribute("newStaffForm") Staff staff) {
        staff.setEmploymentStatus("ACTIVE");
        staffRepository.save(staff);
        return "redirect:/staffing";
    }

    /**
     * Handles replacing an old worker who left, changing positions, or reassigning roles
     */
    @PostMapping("/update/{id}")
    public String updateStaffProfileDetails(
            @PathVariable("id") Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam("currentPosition") String currentPosition,
            @RequestParam("departmentAssignment") String departmentAssignment,
            @RequestParam("employmentStatus") String employmentStatus,
            @RequestParam(value = "systemUserRole", required = false, defaultValue = "NONE") String systemUserRole) {
        
        Optional<Staff> employeeData = staffRepository.findById(id);
        
        if (employeeData.isPresent()) {
            Staff worker = employeeData.get();
            
            // Reassign names, operational workspace blocks, or swap personnel parameters
            worker.setFullName(fullName);
            worker.setCurrentPosition(currentPosition);
            worker.setDepartmentAssignment(departmentAssignment);
            worker.setEmploymentStatus(employmentStatus);
            worker.setSystemUserRole(systemUserRole);
            
            staffRepository.save(worker);
        }
        
        return "redirect:/staffing";
    }
}