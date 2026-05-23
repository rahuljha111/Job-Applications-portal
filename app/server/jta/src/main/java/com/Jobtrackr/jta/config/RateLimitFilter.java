package com.Jobtrackr.jta.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> rateLimitCache;
    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientKey = getClientKey(request);
        String path = request.getRequestURI();
        
        Bucket bucket = resolveBucket(clientKey, path);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitTimeSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitTimeSeconds));
            response.setContentType("application/json");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"retryAfterSeconds\":" + waitTimeSeconds + "}");
            
            log.warn("Rate limit exceeded for client: {} on path: {}", clientKey, path);
        }
    }

    private String getClientKey(HttpServletRequest request) {
        // Use X-Forwarded-For if behind proxy, otherwise use remote addr
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        
        // If authenticated, use user principal for more granular limiting
        if (request.getUserPrincipal() != null) {
            return "user:" + request.getUserPrincipal().getName();
        }
        
        return request.getRemoteAddr();
    }

    private Bucket resolveBucket(String clientKey, String path) {
        String bucketKey = clientKey + ":" + getBucketType(path);
        
        return rateLimitCache.get(bucketKey, key -> {
            if (path.startsWith("/api/ai/")) {
                return rateLimitConfig.createAIBucket();
            } else if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
                return rateLimitConfig.createLoginBucket();
            }
            return rateLimitConfig.createStandardBucket();
        });
    }

    private String getBucketType(String path) {
        if (path.startsWith("/api/ai/")) {
            return "ai";
        } else if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            return "auth";
        }
        return "standard";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip rate limiting for health checks and static resources
        return path.startsWith("/actuator/") || 
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/v3/api-docs");
    }
}
