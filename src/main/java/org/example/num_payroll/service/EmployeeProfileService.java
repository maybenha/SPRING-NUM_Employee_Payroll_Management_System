package org.example.num_payroll.service;

import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.model.User;
import org.example.num_payroll.repository.EmployeeProfileRepository;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeProfileService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final FileStorageService fileStorageService;

    public EmployeeProfileService(EmployeeProfileRepository employeeProfileRepository,
                                  FileStorageService fileStorageService) {
        this.employeeProfileRepository = employeeProfileRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<EmployeeProfile> findAllEmployees() {
        return employeeProfileRepository.findAll();
    }

    public Optional<EmployeeProfile> findById(Long id) {
        return employeeProfileRepository.findById(id);
    }

    public Optional<EmployeeProfile> findByUser(User user) {
        return employeeProfileRepository.findByUser(user);
    }

    public long countAllEmployees() {
        return employeeProfileRepository.count();
    }

    @Transactional
    public EmployeeProfile save(EmployeeProfile employeeProfile) {
        return employeeProfileRepository.save(employeeProfile);
    }

    @Transactional
    public EmployeeProfile saveWithImage(EmployeeProfile employeeProfile, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (employeeProfile.getProfileImageUrl() != null) {
                try {
                    fileStorageService.deleteFile(employeeProfile.getProfileImageUrl());
                } catch (IOException e) {
                    // Log error but continue with save
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }

            // Save new image
            String imageUrl = fileStorageService.storeFile(imageFile);
            employeeProfile.setProfileImageUrl(imageUrl);
        }

        return employeeProfileRepository.save(employeeProfile);
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<EmployeeProfile> employee = employeeProfileRepository.findById(id);
        employee.ifPresent(emp -> {
            // Delete profile image if exists
            if (emp.getProfileImageUrl() != null) {
                try {
                    fileStorageService.deleteFile(emp.getProfileImageUrl());
                } catch (IOException e) {
                    System.err.println("Failed to delete image: " + e.getMessage());
                }
            }
            employeeProfileRepository.deleteById(id);
        });
    }

    @Transactional
    public void updateProfileImage(Long employeeId, MultipartFile imageFile) throws IOException {
        Optional<EmployeeProfile> employeeOpt = employeeProfileRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            EmployeeProfile employee = employeeOpt.get();

            // Delete old image if exists
            if (employee.getProfileImageUrl() != null) {
                fileStorageService.deleteFile(employee.getProfileImageUrl());
            }

            // Save new image
            String imageUrl = fileStorageService.storeFile(imageFile);
            employee.setProfileImageUrl(imageUrl);
            employeeProfileRepository.save(employee);
        }
    }

    public String getDefaultAvatarUrl() {
        return "/images/default-avatar.png";
    }
}