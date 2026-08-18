package com.hotelerp.userservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Long hotelId;
    private String hotelName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
