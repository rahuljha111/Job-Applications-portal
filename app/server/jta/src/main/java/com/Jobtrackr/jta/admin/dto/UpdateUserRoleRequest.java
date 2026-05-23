package com.Jobtrackr.jta.admin.dto;

import com.Jobtrackr.jta.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class UpdateUserRoleRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Role is required")
    private Role role;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
