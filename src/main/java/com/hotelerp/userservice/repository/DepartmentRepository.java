package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    @Query("SELECT d FROM Department d WHERE (:hotelId IS NULL OR d.property.id = :hotelId)")
    List<Department> findAllByPropertyId(@Param("hotelId") Long hotelId);

    @Query("SELECT d FROM Department d WHERE d.isActive = true AND (:hotelId IS NULL OR d.property.id = :hotelId)")
    List<Department> findActiveByPropertyId(@Param("hotelId") Long hotelId);

    @Query("SELECT d FROM Department d WHERE d.name = :name AND (:hotelId IS NULL OR d.property.id = :hotelId)")
    Optional<Department> findByNameAndPropertyId(@Param("name") String name, @Param("hotelId") Long hotelId);

}
