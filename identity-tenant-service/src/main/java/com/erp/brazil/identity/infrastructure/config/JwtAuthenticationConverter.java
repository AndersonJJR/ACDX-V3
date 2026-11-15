package com.erp.brazil.identity.infrastructure.config;

import com.erp.brazil.common.infrastructure.security.JwtUserDetails;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts JWT to Spring Security Authentication with authorities.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        
        JwtUserDetails userDetails = JwtUserDetails.builder()
                .username(jwt.getSubject())
                .userId(jwt.getClaimAsString("userId"))
                .tenantId(jwt.getClaimAsString("tenantId"))
                .companyId(jwt.getClaimAsString("companyId"))
                .authorities(authorities)
                .build();

        return new JwtAuthenticationToken(jwt, authorities, userDetails);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        String authoritiesClaim = jwt.getClaimAsString("authorities");
        if (authoritiesClaim == null || authoritiesClaim.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(authoritiesClaim.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}