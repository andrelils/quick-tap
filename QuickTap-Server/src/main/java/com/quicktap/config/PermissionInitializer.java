package com.quicktap.config;

import com.quicktap.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Permission Initializer
 *
 * Initializes system permissions and roles on application startup
 * Runs after all beans are initialized to ensure database is ready
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionInitializer {

    private final PermissionService permissionService;

    /**
     * Initialize permissions and roles when application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializePermissions() {
        try {
            log.info("Starting permission initialization...");

            // Initialize system permissions
            permissionService.initializeSystemPermissions();
            log.info("System permissions initialized successfully");

            // Initialize system roles with default permissions
            permissionService.initializeSystemRoles();
            log.info("System roles initialized successfully");

            log.info("Permission initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize permissions", e);
        }
    }
}
