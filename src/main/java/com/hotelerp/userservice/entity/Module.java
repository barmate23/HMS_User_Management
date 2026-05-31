package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    
    private String category; // e.g., Operations, Front Office
    
    @Builder.Default
    private boolean active = true;
}
