package com.Jobtrackr.jta.user.dto;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private AuthUser user;

    public LoginResponse(String token, AuthUser user) {
        this.token = token;
        this.user = user;
    }
    
    public LoginResponse(String token, String refreshToken, AuthUser user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public AuthUser getUser() { return user; }
}
