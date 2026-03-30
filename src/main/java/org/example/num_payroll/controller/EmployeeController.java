package org.example.num_payroll.controller;

import org.example.num_payroll.model.Attendance;
import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.model.Payroll;
import org.example.num_payroll.model.User;
import org.example.num_payroll.service.AttendanceService;
import org.example.num_payroll.service.EmployeeProfileService;
import org.example.num_payroll.service.PayrollService;
import org.example.num_payroll.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final UserService userService;
    private final EmployeeProfileService employeeProfileService;
    private final AttendanceService attendanceService;
    private final PayrollService payrollService;

    public EmployeeController(UserService userService, EmployeeProfileService employeeProfileService,
                              AttendanceService attendanceService, PayrollService payrollService) {
        this.userService = userService;
        this.employeeProfileService = employeeProfileService;
        this.attendanceService = attendanceService;
        this.payrollService = payrollService;
    }

    private EmployeeProfile getAuthenticatedEmployeeProfile(@AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        return employeeProfileService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for authenticated user."));
    }

    //view profile page
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
        model.addAttribute("employee", employeeProfile);

        // Add avatar URL to model
        String avatarUrl = employeeProfile.getProfileImageUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = employeeProfileService.getDefaultAvatarUrl();
        }
        model.addAttribute("avatarUrl", avatarUrl);

        return "profile";
    }
    //upload image path
    @PostMapping("/profile/upload-image")
    public String uploadProfileImage(@AuthenticationPrincipal UserDetails currentUser,
                                     @RequestParam("profileImage") MultipartFile imageFile,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (imageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select an image to upload.");
                return "redirect:/employee/profile";
            }

            // Validate file type
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only image files are allowed (JPEG, PNG, GIF).");
                return "redirect:/employee/profile";
            }

            // Validate file size (max 5MB)
            if (imageFile.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("errorMessage", "File size must be less than 5MB.");
                return "redirect:/employee/profile";
            }

            EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
            employeeProfileService.updateProfileImage(employeeProfile.getId(), imageFile);

            redirectAttributes.addFlashAttribute("successMessage", "Profile image updated successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile image: " + e.getMessage());
        }

        return "redirect:/employee/profile";
    }
    //attendance page
    @GetMapping("/attendance")
    public String viewAttendance(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
        model.addAttribute("employee", employeeProfile);

        // Add avatar URL
        String avatarUrl = employeeProfile.getProfileImageUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = employeeProfileService.getDefaultAvatarUrl();
        }
        model.addAttribute("avatarUrl", avatarUrl);

        // Attendance logic
        Optional<Attendance> currentDayAttendance = attendanceService.getCurrentDayAttendance(employeeProfile);
        model.addAttribute("hasCheckedIn", currentDayAttendance.isPresent());
        model.addAttribute("hasCheckedOut", currentDayAttendance.isPresent() && currentDayAttendance.get().getCheckOutTime() != null);
        model.addAttribute("currentAttendance", currentDayAttendance.orElse(null));

        List<Attendance> attendanceHistory = attendanceService.getAttendanceHistory(employeeProfile);
        model.addAttribute("attendanceHistory", attendanceHistory);
        return "employee/attendance";
    }
    //employee attendance checkin path
    @PostMapping("/attendance/checkin")
    public String checkIn(@AuthenticationPrincipal UserDetails currentUser, RedirectAttributes redirectAttributes) {
        try {
            EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
            attendanceService.checkIn(employeeProfile);
            redirectAttributes.addFlashAttribute("successMessage", "Checked in successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error checking in: " + e.getMessage());
        }
        return "redirect:/employee/attendance";
    }

    //employee checkout path
    @PostMapping("/attendance/checkout")
    public String checkOut(@AuthenticationPrincipal UserDetails currentUser, RedirectAttributes redirectAttributes) {
        try {
            EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
            attendanceService.checkOut(employeeProfile);
            redirectAttributes.addFlashAttribute("successMessage", "Checked out successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error checking out: " + e.getMessage());
        }
        return "redirect:/employee/attendance";
    }
// employee payroll page
    @GetMapping("/payroll")
    public String viewPayroll(@AuthenticationPrincipal UserDetails currentUser, Model model,
                              @RequestParam(value = "month", required = false) Integer month,
                              @RequestParam(value = "year", required = false) Integer year) {
        EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
        model.addAttribute("employee", employeeProfile);

        // Add avatar URL
        String avatarUrl = employeeProfile.getProfileImageUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = employeeProfileService.getDefaultAvatarUrl();
        }
        model.addAttribute("avatarUrl", avatarUrl);

        if (month == null || year == null) {
            YearMonth currentYearMonth = YearMonth.now();
            month = currentYearMonth.getMonthValue();
            year = currentYearMonth.getYear();
        }

        Optional<Payroll> payrollSummary = payrollService.getEmployeePayrollSummary(employeeProfile, month, year);
        model.addAttribute("payrollSummary", payrollSummary.orElse(null));
        model.addAttribute("currentMonth", Month.of(month).name());
        model.addAttribute("currentYear", year);

        List<Payroll> payrollHistory = payrollService.getPayrollsByEmployeeProfile(employeeProfile);
        model.addAttribute("payrollHistory", payrollHistory);

        return "employee/payroll";
    }

    @GetMapping("/dashboard")
    public String viewDashboard(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        EmployeeProfile employeeProfile = getAuthenticatedEmployeeProfile(currentUser);
        model.addAttribute("employee", employeeProfile);

        // Add avatar URL
        String avatarUrl = employeeProfile.getProfileImageUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = employeeProfileService.getDefaultAvatarUrl();
        }
        model.addAttribute("avatarUrl", avatarUrl);

        // Add any dashboard-specific data here
        return "employee/dashboard";
    }
}