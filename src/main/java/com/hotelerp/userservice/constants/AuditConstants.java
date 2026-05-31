package com.hotelerp.userservice.constants;

public class AuditConstants {
    // Modules
    public static final String MODULE_AUTHENTICATION = "Authentication";
    public static final String MODULE_USERS = "Users";
    public static final String MODULE_ROLES = "Roles";
    public static final String MODULE_SECURITY = "Security";

    // Activities
    public static final String ACTION_LOGIN_SUCCESS = "User logged in successfully";
    public static final String ACTION_LOGIN_FAILURE = "Failed login attempt";
    public static final String ACTION_PASSWORD_CHANGE = "Password changed";
    public static final String ACTION_USER_LOCKED = "User account locked";
    public static final String ACTION_ROLE_CREATED = "New role created";
    public static final String ACTION_PERMISSION_UPDATED = "Permissions updated";

    // Severity
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_CRITICAL = "CRITICAL";
}
