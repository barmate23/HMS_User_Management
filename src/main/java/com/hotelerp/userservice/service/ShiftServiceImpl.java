package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.ShiftRequest;
import com.hotelerp.userservice.dto.ShiftResponse;
import com.hotelerp.userservice.entity.Hotel;
import com.hotelerp.userservice.entity.Shift;
import com.hotelerp.userservice.entity.User;
import com.hotelerp.userservice.repository.HotelRepository;
import com.hotelerp.userservice.repository.ShiftRepository;
import com.hotelerp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository repository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> createShift(ShiftRequest request) {
        log.info("Creating new shift: {}", request.getShiftName());
        try {
            if (repository.findByShiftCode(request.getShiftCode()).isPresent()) {
                return StandardResponse.error("Shift code already exists", "DUPLICATE_CODE",
                        "shiftCode", request.getShiftCode());
            }

            Hotel hotel = null;
            if (request.getHotelId() != null) {
                Optional<Hotel> hotelOpt = hotelRepository.findById(request.getHotelId());
                if (hotelOpt.isEmpty()) {
                    return StandardResponse.error("Hotel not found", "NOT_FOUND", "hotelId", null);
                }
                hotel = hotelOpt.get();
            }

            Shift shift = Shift.builder()
                    .shiftName(request.getShiftName())
                    .shiftCode(request.getShiftCode())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .property(hotel)
                    .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE")
                    .notes(request.getNotes())
                    .build();

            Shift saved = repository.save(shift);
            return StandardResponse.success(null, "Shift created successfully");
        } catch (Exception e) {
            log.error("Error creating shift: ", e);
            return StandardResponse.error("Failed to create shift", "CREATE_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> updateShift(Long id, ShiftRequest request) {
        log.info("Updating shift ID: {}", id);
        try {
            Optional<Shift> opt = repository.findById(id);
            if (opt.isEmpty()) {
                return StandardResponse.error("Shift not found", "NOT_FOUND", "id", null);
            }
            Shift shift = opt.get();

            if (!shift.getShiftCode().equals(request.getShiftCode()) &&
                    repository.findByShiftCode(request.getShiftCode()).isPresent()) {
                return StandardResponse.error("Shift code already exists", "DUPLICATE_CODE",
                        "shiftCode", request.getShiftCode());
            }

            shift.setShiftName(request.getShiftName());
            shift.setShiftCode(request.getShiftCode());
            shift.setStartTime(request.getStartTime());
            shift.setEndTime(request.getEndTime());

            if (request.getHotelId() != null) {
                Optional<Hotel> hotelOpt = hotelRepository.findById(request.getHotelId());
                if (hotelOpt.isEmpty()) {
                    return StandardResponse.error("Hotel not found", "NOT_FOUND", "hotelId", null);
                }
                shift.setProperty(hotelOpt.get());
            }

            if (StringUtils.hasText(request.getStatus())) {
                shift.setStatus(request.getStatus());
            }
            shift.setNotes(request.getNotes());

            Shift updated = repository.save(shift);
            return StandardResponse.success(null, "Shift updated successfully");
        } catch (Exception e) {
            log.error("Error updating shift: ", e);
            return StandardResponse.error("Failed to update shift", "UPDATE_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getShiftById(Long id) {
        return repository.findById(id)
                .map(shift -> StandardResponse.success(mapToResponse(shift), "Shift fetched successfully"))
                .orElse(StandardResponse.error("Shift not found", "NOT_FOUND", "id", null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL (optional status filter)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getAllShifts(String status) {
        try {
            List<Shift> shifts;
            if (StringUtils.hasText(status)) {
                shifts = repository.findAll().stream()
                        .filter(s -> s.getStatus().equalsIgnoreCase(status))
                        .collect(Collectors.toList());
            } else {
                shifts = repository.findAll();
            }
            List<ShiftResponse> responses = shifts.stream().map(this::mapToResponse).collect(Collectors.toList());
            return StandardResponse.success(responses, "Shifts fetched successfully");
        } catch (Exception e) {
            log.error("Error fetching shifts: ", e);
            return StandardResponse.error("Failed to fetch shifts", "FETCH_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE (soft delete → INACTIVE)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> deleteShift(Long id) {
        try {
            Optional<Shift> opt = repository.findById(id);
            if (opt.isEmpty()) {
                return StandardResponse.error("Shift not found", "NOT_FOUND", "id", null);
            }
            Shift shift = opt.get();
            shift.setStatus("INACTIVE");
            repository.save(shift);
            return StandardResponse.success("Shift deactivated successfully");
        } catch (Exception e) {
            log.error("Error deleting shift: ", e);
            return StandardResponse.error("Failed to delete shift", "DELETE_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASSIGN SHIFT TO USER
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> assignShiftToUser(Long userId, Long shiftId) {
        log.info("Assigning shift ID: {} to user ID: {}", shiftId, userId);
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return StandardResponse.error("User not found", "NOT_FOUND", "userId", null);
            }

            Shift shift = null;
            if (shiftId != null) {
                Optional<Shift> shiftOpt = repository.findById(shiftId);
                if (shiftOpt.isEmpty()) {
                    return StandardResponse.error("Shift not found", "NOT_FOUND", "shiftId", null);
                }
                shift = shiftOpt.get();
            }

            User user = userOpt.get();
            user.setShift(shift);
            userRepository.save(user);

            String message = shift == null
                    ? "Shift unassigned from user successfully"
                    : "Shift assigned to user successfully";
            return StandardResponse.success(null, message);
        } catch (Exception e) {
            log.error("Error assigning shift to user: ", e);
            return StandardResponse.error("Failed to assign shift", "ASSIGN_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapper helpers
    // ─────────────────────────────────────────────────────────────────────────
    private ShiftResponse mapToResponse(Shift shift) {
        Hotel hotel = shift.getProperty();
        return ShiftResponse.builder()
                .id(shift.getId())
                .shiftName(shift.getShiftName())
                .shiftCode(shift.getShiftCode())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .hotelId(hotel != null ? hotel.getId() : null)
                .hotelName(hotel != null ? hotel.getName() : null)
                .status(shift.getStatus())
                .notes(shift.getNotes())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }
}
