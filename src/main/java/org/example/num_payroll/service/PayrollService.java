package org.example.num_payroll.service;

import org.example.num_payroll.model.Attendance;
import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.model.Payroll;
import org.example.num_payroll.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final AttendanceService attendanceService;

    private static final int WORKING_DAYS_IN_MONTH = 22; // working days of the month
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    public PayrollService(PayrollRepository payrollRepository, AttendanceService attendanceService) {
        this.payrollRepository = payrollRepository;
        this.attendanceService = attendanceService;
    }

    @Transactional
    public Payroll calculateAndSavePayroll(EmployeeProfile employeeProfile, int month, int year) {
        Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeProfileAndMonthAndYear(employeeProfile, month, year);
        if (existingPayroll.isPresent()) {

            return existingPayroll.get();
        }

        List<Attendance> monthlyAttendances = attendanceService.getAttendancesByEmployeeProfileIdAndMonthAndYear(employeeProfile.getId(), month, year);

        long totalDaysWorked = monthlyAttendances.stream()
                .filter(a -> a.getCheckOutTime() != null)
                .count();

//        BigDecimal basicSalary = employeeProfile.getBasicSalary();
//        BigDecimal dailyRate = basicSalary.divide(new BigDecimal(WORKING_DAYS_IN_MONTH), 2, RoundingMode.HALF_UP);
//        BigDecimal grossSalary = dailyRate.multiply(new BigDecimal(totalDaysWorked)).setScale(2, RoundingMode.HALF_UP);
//        BigDecimal taxAmount = grossSalary.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
//        BigDecimal netPay = grossSalary.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);
//
//        Payroll payroll = new Payroll();
//        payroll.setEmployeeProfile(employeeProfile);
//        payroll.setMonth(month);
//        payroll.setYear(year);
//        payroll.setTotalDaysWorked((int) totalDaysWorked);
//        payroll.setBasicSalary(basicSalary);
//        payroll.setTax(taxAmount);
//        payroll.setNetPay(netPay);
//
//        return payrollRepository.save(payroll);


        BigDecimal basicSalary = employeeProfile.getBasicSalary();


        BigDecimal dailyRate = basicSalary.divide(new BigDecimal(WORKING_DAYS_IN_MONTH), 2, RoundingMode.HALF_UP);


        BigDecimal grossSalary = dailyRate.multiply(new BigDecimal(totalDaysWorked)).setScale(2, RoundingMode.HALF_UP);


        BigDecimal taxAmount = grossSalary.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);


        BigDecimal netPay = grossSalary.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);

        Payroll payroll = new Payroll();
        payroll.setEmployeeProfile(employeeProfile);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setTotalDaysWorked((int) totalDaysWorked);
        payroll.setBasicSalary(basicSalary);
        payroll.setGrossSalary(grossSalary);
        payroll.setTax(taxAmount);
        payroll.setTaxRate(TAX_RATE);
        payroll.setNetPay(netPay);

        return payrollRepository.save(payroll);
    }

    public Optional<Payroll> getEmployeePayrollSummary(EmployeeProfile employeeProfile, int month, int year) {
        return payrollRepository.findByEmployeeProfileAndMonthAndYear(employeeProfile, month, year);
    }

    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    public List<Payroll> getPayrollsByEmployeeProfile(EmployeeProfile employeeProfile) {
        return payrollRepository.findByEmployeeProfileOrderByYearDescMonthDesc(employeeProfile);
    }

    // For Admin/HR dashboard
    public BigDecimal getTotalSalaryPaidForMonth(int month, int year) {
        return payrollRepository.findTotalNetPayByMonthAndYear(month, year)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalTaxCollectedForMonth(int month, int year) {
        return payrollRepository.findTotalTaxByMonthAndYear(month, year)
                .orElse(BigDecimal.ZERO);
    }

    public Long getTotalEmployeesPaidForMonth(int month, int year) {
        return payrollRepository.countDistinctEmployeeProfilesByMonthAndYear(month, year);
    }
}