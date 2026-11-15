package com.erp.brazil.common.infrastructure.audit;

import com.erp.brazil.common.domain.model.TenantContext;
import com.erp.brazil.common.infrastructure.security.JwtUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of AuditorAware for JPA auditing.
 * Extracts the current user from the security context for createdBy/updatedBy fields.
 */
@Component
public class AuditAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUserId());
        }

        if (principal instanceof String username) {
            return Optional.of(username);
        }

        // Fallback to tenant context user ID
        String userId = TenantContext.getUserId();
        if (userId != null) {
            return Optional.of(userId);
        }

        return Optional.of("anonymous");
    }
}