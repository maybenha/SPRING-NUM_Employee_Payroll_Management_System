package org.example.num_payroll.service;

import org.example.num_payroll.model.Attendance;
import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public Attendance checkIn(EmployeeProfile employeeProfile) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeProfileAndDate(employeeProfile, today);

        if (existingAttendance.isPresent() && existingAttendance.get().getCheckOutTime() == null) {
            // Already checked in and not checked out
            throw new IllegalStateException("You are already checked in.");
        }
        if (existingAttendance.isPresent() && existingAttendance.get().getCheckOutTime() != null) {
            // Already checked in and out today
            throw new IllegalStateException("You have already checked in and out today.");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeProfile(employeeProfile);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setDate(today);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance checkOut(EmployeeProfile employeeProfile) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeProfileAndDate(employeeProfile, today)
                .orElseThrow(() -> new IllegalStateException("You are not checked in for today."));

        if (attendance.getCheckOutTime() != null) {
            throw new IllegalStateException("You are already checked out for today.");
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public Optional<Attendance> getCurrentDayAttendance(EmployeeProfile employeeProfile) {
        return attendanceRepository.findByEmployeeProfileAndDate(employeeProfile, LocalDate.now());
    }

    public List<Attendance> getAttendanceHistory(EmployeeProfile employeeProfile) {
        return attendanceRepository.findByEmployeeProfileOrderByDateDesc(employeeProfile);
    }

    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> getAttendancesByEmployeeProfileIdAndMonthAndYear(Long employeeProfileId, int month, int year) {
        return attendanceRepository.findByEmployeeProfileIdAndDateBetween(
                employeeProfileId,
                LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth())
        );
    }
}