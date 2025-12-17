package org.example.num_payroll.controller;

import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.service.EmployeeProfileService;
import org.example.num_payroll.service.PayrollService;
import org.example.num_payroll.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EmployeeProfileService employeeProfileService;
    private final PayrollService payrollService;
    private final UserService userService;

    public AdminController(EmployeeProfileService employeeProfileService, PayrollService payrollService, UserService userService) {
        this.employeeProfileService = employeeProfileService;
        this.payrollService = payrollService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model,
                                 @RequestParam(value = "month", required = false) Integer month,
                                 @RequestParam(value = "year", required = false) Integer year) {
        if (month == null || year == null) {
            YearMonth currentYearMonth = YearMonth.now();
            month = currentYearMonth.getMonthValue();
            year = currentYearMonth.getYear();
        }

        model.addAttribute("totalEmployees", employeeProfileService.countAllEmployees());
        model.addAttribute("totalMonthlySalary", payrollService.getTotalSalaryPaidForMonth(month, year));
        model.addAttribute("totalTaxCollected", payrollService.getTotalTaxCollectedForMonth(month, year));
        model.addAttribute("employeesPaid", payrollService.getTotalEmployeesPaidForMonth(month, year));
        model.addAttribute("currentMonth", Month.of(month).name());
        model.addAttribute("currentYear", year);



        return "admin/dashboard";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        List<EmployeeProfile> employees = employeeProfileService.findAllEmployees();
        model.addAttribute("employees", employees);
        return "admin/employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        EmployeeProfile employeeProfile = employeeProfileService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        model.addAttribute("employee", employeeProfile);
        return "admin/edit_employee";
    }

    @PostMapping("/employees/{id}/edit")
    public String updateEmployee(@PathVariable Long id, @ModelAttribute EmployeeProfile employee) {
        EmployeeProfile existingEmployee = employeeProfileService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));

        existingEmployee.setFirstName(employee.getFirstName());
        existingEmployee.setLastName(employee.getLastName());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setPhoneNumber(employee.getPhoneNumber());
        existingEmployee.setDepartment(employee.getDepartment());
        existingEmployee.setPosition(employee.getPosition());
        existingEmployee.setBasicSalary(employee.getBasicSalary());


        employeeProfileService.save(existingEmployee);
        return "redirect:/admin/employees";
    }



    @PostMapping("/payroll/calculate/{employeeProfileId}")
    public String calculatePayrollForEmployee(@PathVariable Long employeeProfileId,
                                              @RequestParam int month, @RequestParam int year) {
        EmployeeProfile employeeProfile = employeeProfileService.findById(employeeProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee profile Id:" + employeeProfileId));
        payrollService.calculateAndSavePayroll(employeeProfile, month, year);
        return "redirect:/admin/employees";
    }

    @PostMapping("/payroll/calculateAll")
    public String calculateAllPayrollsForCurrentMonth() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        List<EmployeeProfile> employees = employeeProfileService.findAllEmployees();
        for (EmployeeProfile employee : employees) {
            payrollService.calculateAndSavePayroll(employee, month, year);
        }
        return "redirect:/admin/dashboard";
    }


}