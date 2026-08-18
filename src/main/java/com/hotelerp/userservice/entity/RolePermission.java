package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "role_permissions")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class RolePermission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "can_view")
    private boolean canView;
    
    @Column(name = "can_create")
    private boolean canCreate;
    
    @Column(name = "can_edit")
    private boolean canEdit;
    
    @Column(name = "can_delete")
    private boolean canDelete;
    
    @Column(name = "can_approve")
    private boolean canApprove;
    
    @Column(name = "can_export")
    private boolean canExport;
}
