package com.Jobtrackr.jta.admin.controller;

import com.Jobtrackr.jta.admin.dto.PlatformStatsResponse;
import com.Jobtrackr.jta.admin.dto.UpdateUserRoleRequest;
import com.Jobtrackr.jta.admin.dto.UserManagementResponse;
import com.Jobtrackr.jta.admin.service.AdminService;
import com.Jobtrackr.jta.user.entity.Role;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() {
        log.info("Fetching platform stats");
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserManagementResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("Fetching users - search: {}, role: {}", search, role);
        return ResponseEntity.ok(adminService.getUsers(search, role, pageable));
    }

    @PatchMapping("/users/role")
    public ResponseEntity<UserManagementResponse> updateUserRole(
            @Valid @RequestBody UpdateUserRoleRequest request) {
        log.info("Updating user role: {} -> {}", request.getUserId(), request.getRole());
        return ResponseEntity.ok(adminService.updateUserRole(request));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleUserStatus(@PathVariable UUID userId) {
        log.info("Toggling user status: {}", userId);
        adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID userId) {
        log.info("Deleting user: {}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
