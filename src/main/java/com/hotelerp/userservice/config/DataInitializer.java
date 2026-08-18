package com.hotelerp.userservice.config;

import com.hotelerp.userservice.entity.Department;
import com.hotelerp.userservice.entity.Module;
import com.hotelerp.userservice.entity.Role;
import com.hotelerp.userservice.entity.RolePermission;
import com.hotelerp.userservice.entity.Shift;
import com.hotelerp.userservice.entity.User;
import com.hotelerp.userservice.repository.DepartmentRepository;
import com.hotelerp.userservice.repository.ModuleRepository;
import com.hotelerp.userservice.repository.RoleRepository;
import com.hotelerp.userservice.repository.ShiftRepository;
import com.hotelerp.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${hms.bootstrap.admin.enabled:true}")
    private boolean adminBootstrapEnabled;

    @Value("${hms.bootstrap.admin.employee-id:EMP-ADMIN}")
    private String adminEmployeeId;

    @Value("${hms.bootstrap.admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${hms.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${hms.bootstrap.admin.email:admin@hmscloud.com}")
    private String adminEmail;

    @Value("${hms.bootstrap.admin.phone:9999999999}")
    private String adminPhone;

    @Value("${hms.bootstrap.admin.password:Hms@1234}")
    private String adminPassword;

    @Value("${hms.bootstrap.admin.department:Administration}")
    private String adminDepartmentName;

    @Value("${hms.bootstrap.admin.role:System Administrator}")
    private String adminRoleName;

    @Value("${hms.bootstrap.admin.shift-name:Morning Shift}")
    private String adminShiftName;

    @Value("${hms.bootstrap.admin.shift-code:MORN}")
    private String adminShiftCode;

    @Override
    @Transactional
    public void run(String... args) {
        seedModules();
        seedBootstrapAdmin();
    }

    private void seedModules() {
        if (moduleRepository.count() == 0) {
            log.info("Seeding system modules...");
            List<Module> modules = Arrays.asList(
                Module.builder().name("Dashboard").category("Operations").build(),
                Module.builder().name("Reservations").category("Front Office").build(),
                Module.builder().name("Arrivals & Departures").category("Front Office").build(),
                Module.builder().name("Guest Profiles").category("Front Office").build(),
                Module.builder().name("Housekeeping Board").category("Housekeeping").build(),
                Module.builder().name("Room Audit SOP").category("Housekeeping").build(),
                Module.builder().name("Lost & Found").category("Housekeeping").build(),
                Module.builder().name("Reports").category("Management").build(),
                Module.builder().name("User Management").category("Management").build()
            );
            moduleRepository.saveAll(modules);
            log.info("Successfully seeded {} modules", modules.size());
        }
    }

    private void seedBootstrapAdmin() {
        if (!adminBootstrapEnabled) {
            log.info("Default admin bootstrap is disabled.");
            return;
        }



        Role role = roleRepository.findByName(adminRoleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(adminRoleName)
                        .accessLevel("ADMIN")
                        .status("ACTIVE")
                        .description("Default admin role with full application access.")
                        .permissions(new ArrayList<>())
                        .build()));

        ensureFullPermissions(role);

        Shift shift = shiftRepository.findByShiftCode(adminShiftCode)
                .orElseGet(() -> shiftRepository.save(Shift.builder()
                        .shiftName(adminShiftName)
                        .shiftCode(adminShiftCode)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .status("ACTIVE")
                        .notes("Default shift created during service bootstrap.")
                        .build()));

        if (userRepository.existsByUsername(adminUsername)
                || userRepository.existsByEmail(adminEmail)
                || userRepository.existsByEmployeeId(adminEmployeeId)) {
            log.info("Default admin user bootstrap skipped because the admin user already exists.");
            return;
        }

        User adminUser = User.builder()
                .employeeId(adminEmployeeId)
                .fullName(adminFullName)
                .username(adminUsername)
                .email(adminEmail)
                .phone(adminPhone)
                .role(role)
                .shift(shift)
                .status("ACTIVE")
                .floorAccess("All Floors")
                .notes("Default admin user created during service bootstrap.")
                .passwordHash(passwordEncoder.encode(adminPassword))
                .build();

        userRepository.save(adminUser);
        log.info("Default admin user created with username '{}'.", adminUsername);
    }

    private void ensureFullPermissions(Role role) {
        List<Module> modules = moduleRepository.findByActiveTrue();
        if (modules.isEmpty()) {
            log.warn("Default admin role permission bootstrap skipped because no active modules exist.");
            return;
        }

        if (role.getPermissions() == null) {
            role.setPermissions(new ArrayList<>());
        }

        Set<Long> permittedModuleIds = role.getPermissions().stream()
                .filter(permission -> permission.getModule() != null && permission.getModule().getId() != null)
                .map(permission -> permission.getModule().getId())
                .collect(Collectors.toCollection(HashSet::new));

        for (Module module : modules) {
            if (!permittedModuleIds.contains(module.getId())) {
                role.getPermissions().add(fullPermission(role, module));
            }
        }

        roleRepository.save(role);
    }

    private RolePermission fullPermission(Role role, Module module) {
        return RolePermission.builder()
                .role(role)
                .module(module)
                .canView(true)
                .canCreate(true)
                .canEdit(true)
                .canDelete(true)
                .canApprove(true)
                .canExport(true)
                .build();
    }
}
