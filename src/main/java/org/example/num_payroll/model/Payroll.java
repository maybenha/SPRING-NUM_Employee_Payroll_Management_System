package org.example.num_payroll.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "payrolls", uniqueConstraints = {@UniqueConstraint(columnNames = {"employee_profile_id", "month", "year"})})
@Data
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int month; // 1-12
    @Column(nullable = false)
    private int year;

    private Integer totalDaysWorked;
    private BigDecimal basicSalary;
    private BigDecimal tax;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public int getMonth() {

        return month;
    }

    public void setMonth(int month) {

        this.month = month;
    }

    public int getYear() {

        return year;
    }

    public void setYear(int year) {

        this.year = year;
    }

    public Integer getTotalDaysWorked() {

        return totalDaysWorked;
    }

    public void setTotalDaysWorked(Integer totalDaysWorked) {

        this.totalDaysWorked = totalDaysWorked;
    }

    public BigDecimal getBasicSalary() {

        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {

        this.basicSalary = basicSalary;
    }

    public BigDecimal getTax() {

        return tax;
    }

    public void setTax(BigDecimal tax) {

        this.tax = tax;
    }

    public BigDecimal getNetPay() {

        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {

        this.netPay = netPay;
    }

    public EmployeeProfile getEmployeeProfile() {

        return employeeProfile;
    }

    public void setEmployeeProfile(EmployeeProfile employeeProfile) {

        this.employeeProfile = employeeProfile;
    }

    private BigDecimal netPay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;
}