package org.example.num_payroll.repository;

import org.example.num_payroll.model.Attendance;
import org.example.num_payroll.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeProfileAndDate(EmployeeProfile employeeProfile, LocalDate date);
    List<Attendance> findByEmployeeProfileOrderByDateDesc(EmployeeProfile employeeProfile);

    @Query("SELECT a FROM Attendance a WHERE a.employeeProfile.id = :employeeProfileId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByEmployeeProfileIdAndDateBetween(
            @Param("employeeProfileId") Long employeeProfileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    // You could add a count for all attendance records if needed for a dashboard
    // long countAllBy();
}