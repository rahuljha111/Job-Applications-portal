package com.Jobtrackr.jta.admin.service;

import com.Jobtrackr.jta.Job.entity.JobStatus;
import com.Jobtrackr.jta.Job.repository.JobRepo;
import com.Jobtrackr.jta.admin.dto.PlatformStatsResponse;
import com.Jobtrackr.jta.admin.dto.UpdateUserRoleRequest;
import com.Jobtrackr.jta.admin.dto.UserManagementResponse;
import com.Jobtrackr.jta.application.entity.ApplicationStatus;
import com.Jobtrackr.jta.application.repository.ApplicationRepository;
import com.Jobtrackr.jta.company.repository.CompanyRepo;
import com.Jobtrackr.jta.exception.BadRequestException;
import com.Jobtrackr.jta.exception.NotFoundException;
import com.Jobtrackr.jta.exception.UnauthorizedActionException;
import com.Jobtrackr.jta.user.entity.Role;
import com.Jobtrackr.jta.user.entity.User;
import com.Jobtrackr.jta.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final JobRepo jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepo companyRepository;

    public AdminService(UserRepository userRepository,
                        JobRepo jobRepository,
                        ApplicationRepository applicationRepository,
                        CompanyRepo companyRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
    }

    public PlatformStatsResponse getPlatformStats() {
        validateAdmin();

        PlatformStatsResponse stats = new PlatformStatsResponse();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalCandidates(userRepository.countByRole(Role.CANDIDATE));
        stats.setTotalRecruiters(userRepository.countByRole(Role.RECRUITER));
        stats.setTotalAdmins(userRepository.countByRole(Role.ADMIN));
        stats.setTotalCompanies(companyRepository.count());
        stats.setTotalJobs(jobRepository.count());
        stats.setActiveJobs(jobRepository.countByStatus(JobStatus.OPEN));
        stats.setTotalApplications(applicationRepository.count());

        Map<String, Long> usersByRole = new HashMap<>();
        List<Object[]> roleData = userRepository.countByRoleGrouped();
        for (Object[] row : roleData) {
            usersByRole.put(row[0].toString(), (Long) row[1]);
        }
        stats.setUsersByRole(usersByRole);

        Map<String, Long> jobsByStatus = new HashMap<>();
        for (JobStatus status : JobStatus.values()) {
            jobsByStatus.put(status.name(), jobRepository.countByStatus(status));
        }
        stats.setJobsByStatus(jobsByStatus);

        Map<String, Long> appsByStatus = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            appsByStatus.put(status.name(), applicationRepository.findAll()
                    .stream()
                    .filter(a -> a.getStatus() == status)
                    .count());
        }
        stats.setApplicationsByStatus(appsByStatus);

        log.info("Platform stats generated");
        return stats;
    }

    public Page<UserManagementResponse> getUsers(String search, Role role, Pageable pageable) {
        validateAdmin();

        Page<User> users = userRepository.searchUsers(search, role, pageable);

        return users.map(user -> {
            UserManagementResponse response = new UserManagementResponse();
            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());
            response.setActive(user.isActive());
            response.setEmailVerified(user.isEmailVerified());
            response.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : null);
            response.setCreatedAt(user.getCreatedAt());

            if (user.getRole() == Role.CANDIDATE) {
                response.setApplicationCount(applicationRepository.countByCandidateId(user.getId()));
            } else if (user.getRole() == Role.RECRUITER) {
                response.setJobPostCount(jobRepository.countByRecruiterId(user.getId()));
            }

            return response;
        });
    }

    @Transactional
    public UserManagementResponse updateUserRole(UpdateUserRoleRequest request) {
        validateAdmin();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        User currentAdmin = getCurrentUser();
        if (user.getId().equals(currentAdmin.getId())) {
            throw new BadRequestException("Cannot change your own role");
        }

        user.setRole(request.getRole());
        User saved = userRepository.save(user);

        log.info("User role updated: {} -> {}", saved.getEmail(), request.getRole());

        UserManagementResponse response = new UserManagementResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole());
        response.setActive(saved.isActive());
        response.setEmailVerified(saved.isEmailVerified());
        response.setCompanyName(saved.getCompany() != null ? saved.getCompany().getName() : null);
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    @Transactional
    public void toggleUserStatus(UUID userId) {
        validateAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User currentAdmin = getCurrentUser();
        if (user.getId().equals(currentAdmin.getId())) {
            throw new BadRequestException("Cannot deactivate yourself");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        log.info("User status toggled: {} -> active={}", user.getEmail(), user.isActive());
    }

    @Transactional
    public void deleteUser(UUID userId) {
        validateAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User currentAdmin = getCurrentUser();
        if (user.getId().equals(currentAdmin.getId())) {
            throw new BadRequestException("Cannot delete yourself");
        }

        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }

    private void validateAdmin() {
        User user = getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException("Admin access required");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
