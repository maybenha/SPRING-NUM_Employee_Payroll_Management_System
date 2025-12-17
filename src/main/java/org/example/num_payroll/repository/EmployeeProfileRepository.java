package org.example.num_payroll.repository;

import org.example.num_payroll.model.EmployeeProfile;
import org.example.num_payroll.model.User;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
    Optional<EmployeeProfile> findByUser(User user);
    Optional<EmployeeProfile> findByEmail(String email);
    long count();

}