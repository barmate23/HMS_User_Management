package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    Optional<Shift> findByShiftCode(String shiftCode);

    /**
     * Returns active shifts for the same hotel whose time window overlaps [startTime, endTime).
     * Overlap condition: existingStart < newEnd  AND  existingEnd > newStart
     *
     * @param hotelId   hotel to scope the check (null = global scope when hotel is not set)
     * @param startTime start of the new/updated shift
     * @param endTime   end   of the new/updated shift
     * @param excludeId ID of the shift being updated (pass null for create)
     */
    @Query("""
            SELECT s FROM Shift s
            WHERE s.status = 'ACTIVE'
              AND (:hotelId IS NULL OR s.property.id = :hotelId)
              AND s.startTime < :endTime
              AND s.endTime   > :startTime
              AND (:excludeId IS NULL OR s.id <> :excludeId)
            """)
    List<Shift> findOverlappingShifts(
            @Param("hotelId")   Long hotelId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime")   LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
