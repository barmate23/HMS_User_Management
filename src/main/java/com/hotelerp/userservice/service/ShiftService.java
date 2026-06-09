package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.ShiftRequest;

public interface ShiftService {
    StandardResponse<?> createShift(ShiftRequest request);
    StandardResponse<?> updateShift(Long id, ShiftRequest request);
    StandardResponse<?> getShiftById(Long id);
    StandardResponse<?> getAllShifts(String status);
    StandardResponse<?> deleteShift(Long id);
    StandardResponse<?> assignShiftToUser(Long userId, Long shiftId);
}
