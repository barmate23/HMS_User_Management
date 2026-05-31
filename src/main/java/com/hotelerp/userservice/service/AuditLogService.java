package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.AuditLogResponse;
import com.hotelerp.userservice.entity.AuditLog;
import com.hotelerp.userservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public StandardResponse<?> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        
        List<AuditLogResponse> content = auditLogs.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .totalRecords(auditLogs.getTotalElements())
                .totalPages(auditLogs.getTotalPages())
                .currentPage(auditLogs.getNumber())
                .pageSize(auditLogs.getSize())
                .build();

        return StandardResponse.success(content, "Audit logs retrieved successfully", metadata);
    }

    @Transactional
    public void logActivity(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .fullName(log.getFullName())
                .role(log.getRole())
                .activity(log.getActivity())
                .module(log.getModule())
                .ipAddress(log.getIpAddress())
                .severity(log.getSeverity())
                .timestamp(log.getTimestamp())
                .build();
    }
}
