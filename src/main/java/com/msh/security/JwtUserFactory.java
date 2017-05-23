package com.msh.security;

import com.msh.common.model.security.AuthorityName;
import com.msh.common.model.security.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(User user) {
        List<AuthorityName> roles = new ArrayList<>();
        roles.add(AuthorityName.ROLE_USER);
        return new JwtUser(
                user.getId(),
                user.getOpenid(),
                user.getSubscribeTime(),
                mapToGrantedAuthorities(roles)
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<AuthorityName> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .collect(Collectors.toList());
    }
}
