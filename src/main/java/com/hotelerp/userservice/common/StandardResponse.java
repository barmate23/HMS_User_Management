package com.hotelerp.userservice.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String logId;
    private String requestId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    private ErrorDetails error;
    private ResponseMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String message;
        private String details;
        private String field;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMetadata {
        private Long totalRecords;
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Long executionTimeMs;
        private String operation;
    }

    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .logId(LogContext.getLogId())
                .requestId(LogContext.getRequestId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> StandardResponse<T> success(T data, String message, ResponseMetadata metadata) {
        return StandardResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .logId(LogContext.getLogId())
                .requestId(LogContext.getRequestId())
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }

    public static StandardResponse<Void> success(String message) {
        return StandardResponse.<Void>builder()
                .success(true)
                .message(message)
                .logId(LogContext.getLogId())
                .requestId(LogContext.getRequestId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> StandardResponse<T> error(String message, String errorCode, String errorDetails) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .logId(LogContext.getLogId())
                .requestId(LogContext.getRequestId())
                .timestamp(LocalDateTime.now())
                .error(ErrorDetails.builder()
                        .code(errorCode)
                        .message(message)
                        .details(errorDetails)
                        .build())
                .build();
    }

    public static <T> StandardResponse<T> error(String message, String errorCode, String field, String errorDetails) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .logId(LogContext.getLogId())
                .requestId(LogContext.getRequestId())
                .timestamp(LocalDateTime.now())
                .error(ErrorDetails.builder()
                        .code(errorCode)
                        .message(message)
                        .field(field)
                        .details(errorDetails)
                        .build())
                .build();
    }
}
