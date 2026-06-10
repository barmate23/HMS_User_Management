package com.hotelerp.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.entity.AuthSession;
import com.hotelerp.userservice.entity.User;
import com.hotelerp.userservice.repository.AuthSessionRepository;
import com.hotelerp.userservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveBearerToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);
            String tokenId = claims.getId();

            AuthSession session = authSessionRepository.findByAccessTokenId(tokenId)
                    .filter(AuthSession::isActive)
                    .filter(item -> item.getAccessExpiresAt().isAfter(LocalDateTime.now()))
                    .orElseThrow(() -> new IllegalArgumentException("Session is invalid or expired"));

            if (!jwtService.isAccessToken(claims) || !String.valueOf(session.getUser().getId()).equals(claims.getSubject())) {
                throw new IllegalArgumentException("Invalid access token");
            }

            User user = userRepository.findById(Long.valueOf(claims.getSubject()))
                    .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                    .orElseThrow(() -> new IllegalArgumentException("User is not active"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    String.valueOf(user.getId()),
                    null,
                    toAuthorities(claims)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, ex.getMessage());
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> toAuthorities(Claims claims) {
        Object rawAuthorities = claims.get("authorities");
        List<String> authorities = rawAuthorities instanceof List<?> values
                ? values.stream().map(String::valueOf).toList()
                : List.of("ROLE_USER");

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return grantedAuthorities;
    }

    private void writeUnauthorized(HttpServletResponse response, String details) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                StandardResponse.error("Unauthorized", "AUTH_UNAUTHORIZED", details)
        );
    }
}
