package com.Jobtrackr.jta.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/refresh-token",
                                "/api/users/forgot-password",
                                "/api/users/reset-password",
                                "/api/users/verify-email",
                                "/api/users/request-email-verification",
                                "/api/verification/**"  // Allow all verification endpoints
                        ).permitAll()
                        
                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        
                        // Public job listing
                        .requestMatchers(HttpMethod.GET, "/api/jobs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/{jobId}").permitAll()
                        
                        // User endpoints
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                        
                        // Job endpoints - Recruiter
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/recruiter").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.PATCH, "/api/jobs/*/close").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/all").hasAuthority("ROLE_ADMIN")
                        
                        // Application endpoints - Candidate
                        .requestMatchers(HttpMethod.POST, "/api/applications/jobs/*/apply").hasAuthority("ROLE_CANDIDATE")
                        .requestMatchers(HttpMethod.GET, "/api/applications/me/**").hasAuthority("ROLE_CANDIDATE")
                        
                        // Application endpoints - Recruiter
                        .requestMatchers(HttpMethod.GET, "/api/applications/jobs/**").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.PATCH, "/api/applications/*/status").hasAuthority("ROLE_RECRUITER")
                        
                        // Company endpoints
                        .requestMatchers(HttpMethod.POST, "/api/companies").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.POST, "/api/companies/*/assign-me").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.POST, "/api/companies/*/recruiters/*").hasAuthority("ROLE_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/companies").authenticated()
                        
                        // Resume endpoints - Candidate
                        .requestMatchers("/api/resumes/**").hasAuthority("ROLE_CANDIDATE")
                        
                        // AI endpoints - Authenticated
                        .requestMatchers("/api/ai/**").authenticated()
                        
                        // Analytics endpoints
                        .requestMatchers("/api/analytics/candidate").hasAuthority("ROLE_CANDIDATE")
                        .requestMatchers("/api/analytics/recruiter").hasAuthority("ROLE_RECRUITER")
                        
                        // Notification endpoints - Authenticated
                        .requestMatchers("/api/notifications/**").authenticated()
                        
                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        
                        // Default: require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174", 
                "http://localhost:5175",
                "http://localhost:5176",
                "http://localhost:5177",
                "http://localhost:5179",
                "http://localhost:5180",
                "http://localhost:3000",
                "https://applications-portal.netlify.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition", "X-Rate-Limit-Remaining", "X-Rate-Limit-Retry-After-Seconds"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
