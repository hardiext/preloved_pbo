package com.preloved_id.preloved.config;

import com.preloved_id.preloved.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

   @Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
) throws ServletException, IOException {

    String token = null;

    System.out.println("========== JWT FILTER ==========");
    System.out.println("PATH : " + request.getRequestURI());

    Cookie[] cookies = request.getCookies();

    if (cookies != null) {

        System.out.println("COOKIE DITEMUKAN:");

        for (Cookie cookie : cookies) {

            System.out.println(
                    cookie.getName() + " = " + cookie.getValue()
            );

            if ("token".equals(cookie.getName())) {
                token = cookie.getValue();
            }
        }

    } else {

        System.out.println("TIDAK ADA COOKIE");
    }

    if (token == null) {

        System.out.println("TOKEN TIDAK ADA");

        filterChain.doFilter(request, response);

        return;
    }

    boolean isValid = jwtService.isTokenValid(token);

    System.out.println("TOKEN VALID : " + isValid);

    if (!isValid) {

        filterChain.doFilter(request, response);

        return;
    }

    try {

        String email = jwtService.extractEmail(token);

        String role =
                jwtService.extractClaims(token)
                        .get("role", String.class);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        )
                );

        authToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authToken);

        System.out.println("AUTH SUCCESS : " + email);

    } catch (Exception e) {

        System.out.println("JWT ERROR : " + e.getMessage());
    }

    filterChain.doFilter(request, response);
}
}