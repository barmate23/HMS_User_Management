package com.hotelerp.userservice.config;

import com.hotelerp.userservice.entity.Module;
import com.hotelerp.userservice.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ModuleRepository moduleRepository;

    @Override
    public void run(String... args) {
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
}
