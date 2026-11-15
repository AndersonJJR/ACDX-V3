package com.erp.brazil.identity.infrastructure.security;

import com.erp.brazil.common.infrastructure.security.JwtUserDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Internal UserDetails implementation for service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements JwtUserDetails {

    private String username;
    private String userId;
    private String tenantId;
    private String companyId;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
}