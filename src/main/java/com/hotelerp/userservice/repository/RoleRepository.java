package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(String name);
    List<Role> findByDepartmentId(Long departmentId);
}
