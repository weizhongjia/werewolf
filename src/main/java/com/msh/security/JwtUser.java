package com.msh.security;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by stephan on 20.03.16.
 */
public class JwtUser {

    private final int id;
    private final String openId;
    private final int subscribeTime;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUser(
          int id,
          String openId,
          int subscribeTime, Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.openId = openId;
        this.subscribeTime = subscribeTime;
        this.authorities = authorities;
    }

    @JsonIgnore
    public int getId() {
        return id;
    }

    public String getOpenId() {
        return openId;
    }

    @JsonIgnore
    public int getSubscribeTime() {
        return subscribeTime;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

}
