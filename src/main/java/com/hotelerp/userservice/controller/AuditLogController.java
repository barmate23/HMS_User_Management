package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.constants.ServiceConstants;
import com.hotelerp.userservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ServiceConstants.AUDIT_BASE_URL)
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    /**
     * GET /api/v1/audit-logs/getAllAuditLogs
     * Returns paginated list of user activity audit logs.
     */
    @GetMapping(ServiceConstants.GET_ALL_AUDIT_LOGS)
    public ResponseEntity<StandardResponse<?>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getAllAuditLogs(pageable));
    }
}
