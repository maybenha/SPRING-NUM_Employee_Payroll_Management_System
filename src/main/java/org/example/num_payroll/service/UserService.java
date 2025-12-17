package org.example.num_payroll.service;

import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.model.Role;
import org.example.num_payroll.model.User;
import org.example.num_payroll.repository.EmployeeProfileRepository;
import org.example.num_payroll.repository.RoleRepository;
import org.example.num_payroll.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       EmployeeProfileRepository employeeProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerNewEmployee(String username, String password, String firstName, String lastName, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (employeeProfileRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found"));

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(employeeRole);
        newUser = userRepository.save(newUser); // Save user first to get ID

        EmployeeProfile newProfile = new EmployeeProfile();
        newProfile.setFirstName(firstName);
        newProfile.setLastName(lastName);
        newProfile.setEmail(email);
        newProfile.setHireDate(LocalDate.now());
        newProfile.setBasicSalary(BigDecimal.ZERO); // Default or prompt for admin to update
        newProfile.setUser(newUser); // Link to the newly created user
        employeeProfileRepository.save(newProfile);

        newUser.setEmployeeProfile(newProfile); // Link profile back to user
        return newUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<EmployeeProfile> findEmployeeProfileByUser(User user) {
        return employeeProfileRepository.findByUser(user);
    }

    // Initialize default roles as admin
    @PostConstruct
    public void init() {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }
        if (roleRepository.findByName("ROLE_HR").isEmpty()) {
            Role hrRole = new Role();
            hrRole.setName("ROLE_HR");
            roleRepository.save(hrRole);
        }
        if (roleRepository.findByName("ROLE_EMPLOYEE").isEmpty()) {
            Role employeeRole = new Role();
            employeeRole.setName("ROLE_EMPLOYEE");
            roleRepository.save(employeeRole);
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin")); //Default Admin User
            adminUser.setRole(adminRole);
            adminUser = userRepository.save(adminUser);

            EmployeeProfile adminProfile = new EmployeeProfile();
            adminProfile.setFirstName("System");
            adminProfile.setLastName("Administrator");
            adminProfile.setEmail("admin@payroll.com");
            adminProfile.setHireDate(LocalDate.now());
            adminProfile.setBasicSalary(new BigDecimal("50000.00"));
            adminProfile.setUser(adminUser);
            employeeProfileRepository.save(adminProfile);
            adminUser.setEmployeeProfile(adminProfile);
            userRepository.save(adminUser);
        }
    }
}