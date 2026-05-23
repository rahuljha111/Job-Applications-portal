// API service for communicating with the backend

const API_BASE_URL =
    import.meta.env.VITE_API_URL || "http://localhost:8081/api";

// Centralized API routes (prevents mismatch bugs)
const API = {
  USERS: {
    LOGIN: "/users/login",
    REGISTER: "/users/register",
    REFRESH: "/users/refresh-token",
    FORGOT_PASSWORD: "/users/forgot-password",
    VERIFY_EMAIL: "/verification/verify-email",
    RESEND_VERIFICATION: "/verification/resend-verification",
    PROFILE: "/users/me",
    UPDATE_PROFILE: "/users/me/profile",
    CHANGE_PASSWORD: "/users/me/password",
  },
  JOBS: {
    BASE: "/jobs",
    BY_ID: (jobId: string) => `/jobs/${jobId}`,
    RECRUITER_JOBS: "/jobs/recruiter",
    ALL_JOBS: "/jobs/all",
    CLOSE_JOB: (jobId: string) => `/jobs/${jobId}/close`,
  },
  APPLICATIONS: {
    BASE: "/applications",
    APPLY: (jobId: string) => `/applications/jobs/${jobId}/apply`,
    BY_JOB: (jobId: string) => `/applications/jobs/${jobId}`,
    MY_APPLICATIONS: "/applications/me/applications",
    UPDATE_STATUS: (applicationId: string) => `/applications/${applicationId}/status`,
  },
  RESUMES: {
    BASE: "/resumes",
    BY_ID: (resumeId: string) => `/resumes/${resumeId}`,
    DOWNLOAD: (resumeId: string) => `/resumes/${resumeId}/download`,
    SET_DEFAULT: (resumeId: string) => `/resumes/${resumeId}/default`,
    UPDATE_LABEL: (resumeId: string) => `/resumes/${resumeId}/label`,
  },
  COMPANIES: {
    BASE: "/companies",
    BY_ID: (companyId: string) => `/companies/${companyId}`,
    ASSIGN_RECRUITER: (companyId: string, userId: string) => 
      `/companies/${companyId}/recruiters/${userId}`,
    ASSIGN_ME: (companyId: string) => `/companies/${companyId}/assign-me`,
  },
  AI: {
    ANALYZE_RESUME: "/ai/analyze-resume",
    INTERVIEW_PREP: "/ai/interview-prep",
    SKILL_SUGGESTIONS: "/ai/skill-suggestions",
  },
  ANALYTICS: {
    CANDIDATE: "/analytics/candidate",
    RECRUITER: "/analytics/recruiter",
  },
  NOTIFICATIONS: {
    BASE: "/notifications",
    UNREAD: "/notifications/unread",
    UNREAD_COUNT: "/notifications/unread/count",
    MARK_READ: (notificationId: string) => `/notifications/${notificationId}/read`,
    MARK_ALL_READ: "/notifications/read-all",
  },
  ADMIN: {
    STATS: "/admin/stats",
    USERS: "/admin/users",
    UPDATE_ROLE: "/admin/users/role",
    TOGGLE_STATUS: (userId: string) => `/admin/users/${userId}/toggle-status`,
    DELETE_USER: (userId: string) => `/admin/users/${userId}`,
  },
};

class ApiService {
  private baseURL: string;

  constructor() {
    this.baseURL = API_BASE_URL;
  }

  private async request<T>(
      endpoint: string,
      options?: RequestInit
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;

    const config: RequestInit = {
      headers: {
        "Content-Type": "application/json",
        ...options?.headers,
      },
      ...options,
    };

    // Attach token if exists
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers = {
        ...config.headers,
        Authorization: `Bearer ${token}`,
      };
    }

    try {
      console.log("API Request:", {
        method: config.method || "GET",
        url,
        body: config.body,
      });

      const response = await fetch(url, config);

      console.log("API Response:", response.status);

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        console.error("API Error:", errorData);
        throw new Error(
            errorData?.message || `HTTP error ${response.status}`
        );
      }

      const contentType = response.headers.get("content-type");
      if (!contentType || !contentType.includes("application/json")) {
        return {} as T;
      }

      return await response.json();
    } catch (error) {
      console.error("Request Failed:", error);
      throw error;
    }
  }

  // ================= AUTH =================

  async login(email: string, password: string) {
    return this.request(API.USERS.LOGIN, {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  }

  async register(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    role: string;
  }) {
    const payload = {
      name: `${userData.firstName} ${userData.lastName}`,
      email: userData.email,
      password: userData.password,
      role: userData.role,
    };

    return this.request(API.USERS.REGISTER, {
      method: "POST",
      body: JSON.stringify(payload),
    });
  }

  async refreshToken(refreshToken: string) {
    return this.request(API.USERS.REFRESH, {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });
  }

  async forgotPassword(email: string) {
    return this.request(API.USERS.FORGOT_PASSWORD, {
      method: "POST",
      body: JSON.stringify({ email }),
    });
  }

  async resetPassword(token: string, newPassword: string) {
    return this.request("/users/reset-password", {
      method: "POST",
      body: JSON.stringify({ token, newPassword }),
    });
  }

  async requestEmailVerification(email: string) {
    return this.request("/users/request-email-verification", {
      method: "POST",
      body: JSON.stringify({ email }),
    });
  }

  // ================= EMAIL VERIFICATION =================

  async verifyEmail(email: string, otp: string) {
    return this.request(API.USERS.VERIFY_EMAIL, {
      method: "POST",
      body: JSON.stringify({ email, otpCode: otp }),
    });
  }

  async resendVerification(email: string) {
    return this.request(API.USERS.RESEND_VERIFICATION, {
      method: "POST",
      body: JSON.stringify({ email }),
    });
  }

  // ================= JOBS =================

  async getJobs(params?: {
    page?: number;
    size?: number;
    search?: string;
    location?: string;
    type?: string;
  }) {
    const searchParams = new URLSearchParams();

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }

    const query = searchParams.toString();

    return this.request(
        `${API.JOBS.BASE}${query ? `?${query}` : ""}`
    );
  }

  async getJobById(jobId: string) {
    return this.request(API.JOBS.BY_ID(jobId));
  }

  async createJob(jobData: any) {
    return this.request(API.JOBS.BASE, {
      method: "POST",
      body: JSON.stringify(jobData),
    });
  }

  async getRecruiterJobs(params?: { page?: number; size?: number }) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }
    const query = searchParams.toString();
    return this.request(
      `${API.JOBS.RECRUITER_JOBS}${query ? `?${query}` : ""}`
    );
  }

  async getAllJobs(params?: { page?: number; size?: number }) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }
    const query = searchParams.toString();
    return this.request(
      `${API.JOBS.ALL_JOBS}${query ? `?${query}` : ""}`
    );
  }

  async closeJob(jobId: string) {
    return this.request(API.JOBS.CLOSE_JOB(jobId), {
      method: "PATCH",
    });
  }

  // ================= APPLICATIONS =================

  async applyToJob(jobId: string, applicationData: any) {
    return this.request(API.APPLICATIONS.APPLY(jobId), {
      method: "POST",
      body: JSON.stringify(applicationData),
    });
  }

  async getApplicationsByJob(
    jobId: string,
    params?: { page?: number; size?: number }
  ) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }
    const query = searchParams.toString();
    return this.request(
      `${API.APPLICATIONS.BY_JOB(jobId)}${query ? `?${query}` : ""}`
    );
  }

  async getMyApplications(params?: { page?: number; size?: number }) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }
    const query = searchParams.toString();
    return this.request(
      `${API.APPLICATIONS.MY_APPLICATIONS}${query ? `?${query}` : ""}`
    );
  }

  async updateApplicationStatus(
    applicationId: string,
    status: string,
    notes?: string
  ) {
    return this.request(API.APPLICATIONS.UPDATE_STATUS(applicationId), {
      method: "PATCH",
      body: JSON.stringify({ status, notes }),
    });
  }

  // ================= RESUMES =================

  async uploadResume(file: File, label?: string) {
    const formData = new FormData();
    formData.append("file", file);
    if (label) {
      formData.append("label", label);
    }

    const url = `${this.baseURL}${API.RESUMES.BASE}`;
    const token = localStorage.getItem("authToken");

    const response = await fetch(url, {
      method: "POST",
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || `HTTP error ${response.status}`);
    }

    return await response.json();
  }

  async getResumes() {
    return this.request(API.RESUMES.BASE);
  }

  async getResumeById(resumeId: string) {
    return this.request(API.RESUMES.BY_ID(resumeId));
  }

  async downloadResume(resumeId: string) {
    const url = `${this.baseURL}${API.RESUMES.DOWNLOAD(resumeId)}`;
    const token = localStorage.getItem("authToken");

    const response = await fetch(url, {
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error ${response.status}`);
    }

    return response.blob();
  }

  async setDefaultResume(resumeId: string) {
    return this.request(API.RESUMES.SET_DEFAULT(resumeId), {
      method: "PATCH",
    });
  }

  async updateResumeLabel(resumeId: string, label: string) {
    return this.request(API.RESUMES.UPDATE_LABEL(resumeId), {
      method: "PATCH",
      body: JSON.stringify({ label }),
    });
  }

  async deleteResume(resumeId: string) {
    return this.request(API.RESUMES.BY_ID(resumeId), {
      method: "DELETE",
    });
  }

  // ================= COMPANIES =================

  async createCompany(companyData: any) {
    return this.request(API.COMPANIES.BASE, {
      method: "POST",
      body: JSON.stringify(companyData),
    });
  }

  async getCompanies() {
    return this.request(API.COMPANIES.BASE);
  }

  async getCompanyById(companyId: string) {
    return this.request(API.COMPANIES.BY_ID(companyId));
  }

  async assignRecruiter(companyId: string, userId: string) {
    return this.request(API.COMPANIES.ASSIGN_RECRUITER(companyId, userId), {
      method: "POST",
    });
  }

  async assignMeToCompany(companyId: string) {
    return this.request(API.COMPANIES.ASSIGN_ME(companyId), {
      method: "POST",
    });
  }

  // ================= AI SERVICES =================

  async analyzeResume(resumeData: any) {
    return this.request(API.AI.ANALYZE_RESUME, {
      method: "POST",
      body: JSON.stringify(resumeData),
    });
  }

  async getInterviewPrep(jobData: any) {
    return this.request(API.AI.INTERVIEW_PREP, {
      method: "POST",
      body: JSON.stringify(jobData),
    });
  }

  async getSkillSuggestions(roleData: any) {
    return this.request(API.AI.SKILL_SUGGESTIONS, {
      method: "POST",
      body: JSON.stringify(roleData),
    });
  }

  // ================= ANALYTICS =================

  async getCandidateAnalytics() {
    return this.request(API.ANALYTICS.CANDIDATE);
  }

  async getRecruiterAnalytics() {
    return this.request(API.ANALYTICS.RECRUITER);
  }

  // ================= NOTIFICATIONS =================

  async getNotifications(params?: { page?: number; size?: number }) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }
    const query = searchParams.toString();
    return this.request(
      `${API.NOTIFICATIONS.BASE}${query ? `?${query}` : ""}`
    );
  }

  async getUnreadNotifications() {
    return this.request(API.NOTIFICATIONS.UNREAD);
  }

  async getUnreadCount() {
    return this.request(API.NOTIFICATIONS.UNREAD_COUNT);
  }

  async markNotificationAsRead(notificationId: string) {
    return this.request(API.NOTIFICATIONS.MARK_READ(notificationId), {
      method: "PATCH",
    });
  }

  async markAllNotificationsAsRead() {
    return this.request(API.NOTIFICATIONS.MARK_ALL_READ, {
      method: "PATCH",
    });
  }

  // ================= ADMIN =================

  async getAdminStats() {
    return this.request(API.ADMIN.STATS);
  }

  async getAdminUsers(params?: {
    page?: number;
    size?: number;
    search?: string;
    role?: string;
  }) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
    }
    const query = searchParams.toString();
    return this.request(
      `${API.ADMIN.USERS}${query ? `?${query}` : ""}`
    );
  }

  async updateUserRole(userId: string, role: string) {
    return this.request(API.ADMIN.UPDATE_ROLE, {
      method: "PATCH",
      body: JSON.stringify({ userId, role }),
    });
  }

  async toggleUserStatus(userId: string) {
    return this.request(API.ADMIN.TOGGLE_STATUS(userId), {
      method: "PATCH",
    });
  }

  async deleteUser(userId: string) {
    return this.request(API.ADMIN.DELETE_USER(userId), {
      method: "DELETE",
    });
  }

  // ================= USER =================

  async getUserProfile() {
    return this.request(API.USERS.PROFILE);
  }

  async updateUserProfile(profileData: any) {
    return this.request(API.USERS.UPDATE_PROFILE, {
      method: "PATCH",
      body: JSON.stringify(profileData),
    });
  }

  async changePassword(passwordData: {
    currentPassword: string;
    newPassword: string;
  }) {
    return this.request(API.USERS.CHANGE_PASSWORD, {
      method: "PATCH",
      body: JSON.stringify(passwordData),
    });
  }
}

export const apiService = new ApiService();
export default apiService;