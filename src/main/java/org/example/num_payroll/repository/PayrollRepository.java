package org.example.num_payroll.repository;

import org.example.num_payroll.model.Payroll;
import org.example.num_payroll.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployeeProfileAndMonthAndYear(EmployeeProfile employeeProfile, int month, int year);
    List<Payroll> findByEmployeeProfileOrderByYearDescMonthDesc(EmployeeProfile employeeProfile);

    @Query("SELECT SUM(p.netPay) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    Optional<BigDecimal> findTotalNetPayByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT SUM(p.tax) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    Optional<BigDecimal> findTotalTaxByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(DISTINCT p.employeeProfile) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    Long countDistinctEmployeeProfilesByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
