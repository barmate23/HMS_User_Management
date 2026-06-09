package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.constants.ServiceConstants;
import com.hotelerp.userservice.dto.ShiftRequest;
import com.hotelerp.userservice.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ServiceConstants.SHIFT_BASE_URL)
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService service;

    @PostMapping(ServiceConstants.CREATE_SHIFT)
    public ResponseEntity<StandardResponse<?>> createShift(@Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(service.createShift(request));
    }

    @PutMapping(ServiceConstants.UPDATE_SHIFT)
    public ResponseEntity<StandardResponse<?>> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(service.updateShift(id, request));
    }

    @GetMapping(ServiceConstants.GET_SHIFT_BY_ID)
    public ResponseEntity<StandardResponse<?>> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getShiftById(id));
    }

    @GetMapping(ServiceConstants.GET_ALL_SHIFTS)
    public ResponseEntity<StandardResponse<?>> getAllShifts(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.getAllShifts(status));
    }

    @DeleteMapping(ServiceConstants.DELETE_SHIFT)
    public ResponseEntity<StandardResponse<?>> deleteShift(@PathVariable Long id) {
        return ResponseEntity.ok(service.deleteShift(id));
    }

    @PostMapping(ServiceConstants.ASSIGN_SHIFT)
    public ResponseEntity<StandardResponse<?>> assignShift(
            @PathVariable Long userId,
            @RequestParam(required = false) Long shiftId) {
        return ResponseEntity.ok(service.assignShiftToUser(userId, shiftId));
    }
}
