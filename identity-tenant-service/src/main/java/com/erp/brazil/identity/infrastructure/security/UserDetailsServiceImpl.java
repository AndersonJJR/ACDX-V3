package com.erp.brazil.identity.infrastructure.security;

import com.erp.brazil.common.infrastructure.security.JwtUserDetails;
import com.erp.brazil.identity.domain.model.User;
import com.erp.brazil.identity.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService for JWT authentication.
 * Loads user details from database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        if (!user.isActive()) {
            log.warn("User is not active: {}", username);
            throw new UsernameNotFoundException("User is not active: " + username);
        }

        if (user.isLocked()) {
            log.warn("User is locked: {}", username);
            throw new UsernameNotFoundException("User is locked: " + username);
        }

        Collection<org.springframework.security.core.GrantedAuthority> authorities = 
                user.getUserCompanyRoles().stream()
                        .filter(ucr -> ucr.isCurrentlyValid())
                        .flatMap(ucr -> ucr.getRole().getPermissions().stream())
                        .map(p -> new org.springframework.security.core.authority.SimpleGrantedAuthority(p.getFullPermission()))
                        .collect(Collectors.toSet());

        // Add role authorities
        authorities.addAll(user.getUserCompanyRoles().stream()
                .filter(ucr -> ucr.isCurrentlyValid())
                .map(ucr -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + ucr.getRole().getCode().toUpperCase()))
                .collect(Collectors.toSet()));

        return JwtUserDetails.builder()
                .username(user.getUsername())
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .companyId(user.getCompanyId())
                .password(user.getPassword())
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(!user.isLocked())
                .credentialsNonExpired(true)
                .enabled(user.isActive())
                .build();
    }

    /**
     * Loads user by ID for token validation.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        Collection<org.springframework.security.core.GrantedAuthority> authorities = 
                user.getUserCompanyRoles().stream()
                        .filter(ucr -> ucr.isCurrentlyValid())
                        .flatMap(ucr -> ucr.getRole().getPermissions().stream())
                        .map(p -> new org.springframework.security.core.authority.SimpleGrantedAuthority(p.getFullPermission()))
                        .collect(Collectors.toSet());

        authorities.addAll(user.getUserCompanyRoles().stream()
                .filter(ucr -> ucr.isCurrentlyValid())
                .map(ucr -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + ucr.getRole().getCode().toUpperCase()))
                .collect(Collectors.toSet()));

        return JwtUserDetails.builder()
                .username(user.getUsername())
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .companyId(user.getCompanyId())
                .password(user.getPassword())
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(!user.isLocked())
                .credentialsNonExpired(true)
                .enabled(user.isActive())
                .build();
    }
}