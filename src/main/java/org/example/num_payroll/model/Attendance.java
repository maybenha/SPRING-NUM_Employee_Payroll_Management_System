package org.example.num_payroll.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Data
public class Attendance {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {

        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {

        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {

        this.checkOutTime = checkOutTime;
    }

    public LocalDate getDate() {

        return date;
    }

    public void setDate(LocalDate date) {

        this.date = date;
    }

    public EmployeeProfile getEmployeeProfile() {

        return employeeProfile;
    }

    public void setEmployeeProfile(EmployeeProfile employeeProfile) {

        this.employeeProfile = employeeProfile;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;

//    public long getHoursWorked() {
//        if (checkInTime != null && checkOutTime != null) {
//            return java.time.Duration.between(checkInTime, checkOutTime).toHours();
//        }
//        return 0;
//    }

    public String getHoursWorked() {
        if (checkInTime != null && checkOutTime != null) {
            java.time.Duration duration = java.time.Duration.between(checkInTime, checkOutTime);

            long hours = duration.toHours();
            long minutes = duration.minusHours(hours).toMinutes();
            long seconds = duration
                    .minusHours(hours)
                    .minusMinutes(minutes)
                    .getSeconds();

            return hours + " h " + minutes + " m " + seconds + " seconds";
        }
        return "0 h 0 m 0 s";
    }

}