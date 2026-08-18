package com.hotelerp.userservice.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

@Component
public class AuditAwareImpl implements AuditorAware<String> {

    private final ObjectProvider<LoginUser> loginUserProvider;

    public AuditAwareImpl(ObjectProvider<LoginUser> loginUserProvider) {
        this.loginUserProvider = loginUserProvider;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            try {
                LoginUser loginUser = loginUserProvider.getIfAvailable();
                if (loginUser != null && loginUser.getUserName() != null) {
                    return Optional.of(loginUser.getUserName());
                }
            } catch (Exception e) {
                // fall back
            }
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }
        return Optional.of("SYSTEM");
    }
}
