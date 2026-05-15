// filter/RateLimitingFilter.java (Tanpa Bucket4j)
package com.preloved_id.preloved.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long TIME_WINDOW_SECONDS = 60;

    // Map: IP -> (timestamp terakhir, count)
    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    @Value("${app.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitingEnabled || !request.getRequestURI().contains("/api/bids")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        RateLimitInfo info = requestCounts.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        synchronized (info) {
            long now = Instant.now().getEpochSecond();

            // Reset counter jika sudah melewati time window
            if (now - info.lastRequestTime > TIME_WINDOW_SECONDS) {
                info.count = 0;
                info.lastRequestTime = now;
            }

            // Cek apakah melebihi batas
            if (info.count >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Terlalu banyak permintaan. Silakan tunggu 1 menit.\"}");
                return;
            }

            // Increment counter
            info.count++;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Inner class untuk menyimpan info rate limit
    private static class RateLimitInfo {
        long lastRequestTime = Instant.now().getEpochSecond();
        int count = 0;
    }
}