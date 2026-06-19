package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.Staff;
import com.rollingstar.cottages.model.User;
import com.rollingstar.cottages.repository.StaffRepository;
import com.rollingstar.cottages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/staffing")
@PreAuthorize("hasAnyRole('MANAGER', 'BOSS')")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String displayStaffWorkspace(Model model) {
        model.addAttribute("staffList", staffRepository.findAll());
        model.addAttribute("newStaffForm", new Staff());
        return "staffing";
    }

    /**
     * WORKFLOW 1: ONBOARDING WITH PRIVILEGE TIERS
     * Boss can onboard any role. Manager can ONLY onboard NONE or BARTENDER.
     */
    @PostMapping("/onboard")
    public String onboardNewStaffMember(@ModelAttribute("newStaffForm") Staff staff, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isBoss = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BOSS") || a.getAuthority().equals("BOSS"));

        String selectedRole = staff.getSystemUserRole() != null ? staff.getSystemUserRole().toUpperCase() : "NONE";

        // 🔒 BACKEND GUARD: Prevent Managers from onboarding high-privilege roles (MANAGER or BOSS)
        if (!isBoss && (selectedRole.equals("BOSS") || selectedRole.equals("MANAGER"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Security Intercept: Managers lack corporate clearance to onboard higher administrative tiers (Managers/Bosses).");
            return "redirect:/staffing";
        }

        staff.setEmploymentStatus("ACTIVE");
        Staff savedStaff = staffRepository.save(staff);
        
        if (!selectedRole.equals("NONE")) {
            String generatedUsername = staff.getFullName().toLowerCase().trim().replaceAll("\\s+", ".");
            String temporaryPassword = "Star@" + savedStaff.getId() + "2026";
            
            String securelyHashedPassword = passwordEncoder.encode(temporaryPassword);
            
            User newWorkerUser = new User();
            newWorkerUser.setUsername(generatedUsername);
            newWorkerUser.setPasswordHash(securelyHashedPassword);
            newWorkerUser.setRole(selectedRole);
            
            userRepository.save(newWorkerUser);
            
            // 📑 SECURE TERMINAL PRINTING
            System.out.println("\n==================================================================");
            System.out.println("🔐 CRYPTOGRAPHIC PASSWORD TRANSMISSION TRACKING");
            System.out.println("==================================================================");
            System.out.println("   Employee Name:   " + staff.getFullName());
            System.out.println("   Username:        " + generatedUsername);
            System.out.println("   Raw Text Pass:   " + temporaryPassword);
            System.out.println("   Encoded Hash:    " + securelyHashedPassword); 
            System.out.println("==================================================================\n");
            
            redirectAttributes.addFlashAttribute("showCredentialsAlert", true);
            redirectAttributes.addFlashAttribute("generatedEmployeeName", staff.getFullName());
            redirectAttributes.addFlashAttribute("generatedUsername", generatedUsername);
            redirectAttributes.addFlashAttribute("generatedPassword", temporaryPassword);
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "Staff profile created successfully (No system account assigned).");
        }
        
        return "redirect:/staffing";
    }

    /**
     * WORKFLOW 2: RE-REGISTER / SELF SERVICE ACCOUNT OVERRIDES
     * Allows staff members to customize their credential configurations after onboarding.
     */
    @PostMapping("/reregister")
    public String reregisterStaffCredentials(
            @RequestParam("currentUsername") String currentUsername,
            @RequestParam("newUsername") String newUsername,
            @RequestParam("newPassword") String newPassword,
            RedirectAttributes redirectAttributes) {
        
        User existingUser = userRepository.findByUsername(currentUsername.trim());
        
        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Re-registration Aborted: Target baseline username could not be verified.");
            return "redirect:/staffing";
        }
        
        // Check if new username is taken by a different account
        if (!currentUsername.equalsIgnoreCase(newUsername)) {
            User usernameCheck = userRepository.findByUsername(newUsername.trim());
            if (usernameCheck != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: The requested username profile is already allocated.");
                return "redirect:/staffing";
            }
        }
        
        // Apply updates safely with encryption processing strings
        existingUser.setUsername(newUsername.trim().toLowerCase().replaceAll("\\s+", "."));
        existingUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(existingUser);
        
        redirectAttributes.addFlashAttribute("infoMessage", "Account re-registration complete! User profile credentials updated successfully.");
        return "redirect:/staffing";
    }

    /**
     * WORKFLOW 3: OPERATIONAL ADJUSTMENTS
     */
    @PostMapping("/update/{id}")
    public String updateStaffProfileDetails(
            @PathVariable("id") Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam("currentPosition") String currentPosition,
            @RequestParam("departmentAssignment") String departmentAssignment,
            @RequestParam("employmentStatus") String employmentStatus,
            @RequestParam(value = "systemUserRole", required = false, defaultValue = "NONE") String systemUserRole,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isBoss = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BOSS") || a.getAuthority().equals("BOSS"));

        Optional<Staff> employeeData = staffRepository.findById(id);
        
        if (employeeData.isPresent()) {
            Staff worker = employeeData.get();
            String originalStatus = worker.getEmploymentStatus();
            
            boolean isSelectingTermination = employmentStatus.equalsIgnoreCase("FIRED") || 
                                             employmentStatus.equalsIgnoreCase("TERMINATED") || 
                                             employmentStatus.equalsIgnoreCase("INACTIVE") || 
                                             employmentStatus.equalsIgnoreCase("REPLACED");
            
            boolean statusHasChanged = !originalStatus.equalsIgnoreCase(employmentStatus);

            if (isSelectingTermination && statusHasChanged && !isBoss) {
                redirectAttributes.addFlashAttribute("errorMessage", "Security Denied: Only the BOSS profile level has corporate clearance to fire personnel.");
                return "redirect:/staffing";
            }

            String activeUsername = worker.getFullName().toLowerCase().trim().replaceAll("\\s+", ".");
            
            worker.setFullName(fullName);
            worker.setCurrentPosition(currentPosition);
            worker.setDepartmentAssignment(departmentAssignment);
            worker.setEmploymentStatus(employmentStatus);
            worker.setSystemUserRole(systemUserRole);
            staffRepository.save(worker);
            
            if (isSelectingTermination && isBoss) {
                User workerAccount = userRepository.findByUsername(activeUsername);
                if (workerAccount != null) {
                    workerAccount.setRole("DISABLED_ACCOUNT");
                    workerAccount.setPasswordHash("TERMINATED_LOCKOUT_" + System.currentTimeMillis());
                    userRepository.save(workerAccount);
                }
                redirectAttributes.addFlashAttribute("infoMessage", "Staff profile updated. System access permanently revoked for " + fullName);
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "Roster operational modifications saved successfully for " + fullName);
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Target profile configuration not found.");
        }
        
        return "redirect:/staffing";
    }
}