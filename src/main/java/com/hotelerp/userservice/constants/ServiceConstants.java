package com.hotelerp.userservice.constants;

public class ServiceConstants {

    // ── User ──────────────────────────────────────────────────────────────
    public static final String USER_BASE_URL = "/api/hmsUserService/v1/users";
    public static final String CREATE_USER    = "/createUser";
    public static final String UPDATE_USER    = "/updateUser/{id}";
    public static final String GET_USER_BY_ID = "/getUserById/{id}";
    public static final String GET_ALL_USERS  = "/getAllUsers";
    public static final String DELETE_USER    = "/deleteUser/{id}";
    public static final String CHANGE_STATUS  = "/changeStatus/{id}";

    // ── Audit Logs ────────────────────────────────────────────────────────
    public static final String AUDIT_BASE_URL = "/api/hmsUserService/v1/audit-logs";
    public static final String GET_ALL_AUDIT_LOGS = "/getAllAuditLogs";

    // ── Shifts ────────────────────────────────────────────────────────────
    public static final String SHIFT_BASE_URL = "/api/hmsUserService/v1/shifts";
    public static final String CREATE_SHIFT    = "/createShift";
    public static final String UPDATE_SHIFT    = "/updateShift/{id}";
    public static final String GET_SHIFT_BY_ID = "/getShiftById/{id}";
    public static final String GET_ALL_SHIFTS  = "/getAllShifts";
    public static final String DELETE_SHIFT    = "/deleteShift/{id}";
    public static final String ASSIGN_SHIFT    = "/assignShift/{userId}";
}
