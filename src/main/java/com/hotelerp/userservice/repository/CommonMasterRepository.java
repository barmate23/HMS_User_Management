package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.CommonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommonMasterRepository extends JpaRepository<CommonMaster, Long> {
    List<CommonMaster> findByCategoryAndIsActiveTrue(String category);
    Optional<CommonMaster> findByCategoryAndCodeAndIsActiveTrue(String category, String code);
}
