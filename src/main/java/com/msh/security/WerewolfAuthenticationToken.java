package com.msh.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Created by colorado on 9/03/17.
 */
public class WerewolfAuthenticationToken extends AbstractAuthenticationToken {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8254831403638075928L;

    private String code;

    private String openId;

    private String userName;

    private String password;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public WerewolfAuthenticationToken(
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public WerewolfAuthenticationToken(String openId, Collection<? extends GrantedAuthority> authorities){
        super(authorities);
        this.openId = openId;
    }

    public WerewolfAuthenticationToken(JwtAuthenticationRequest request) {
        super((Collection)null);
        this.code = request.getCode();
        this.password = request.getPassword();
        this.userName = request.getUsername();
    }

    public WerewolfAuthenticationToken(String userName, String password, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userName = userName;
        this.password = password;
    }

    @Override
    public Object getCredentials() {
        return "NOT_REQUIRED";
    }

    @Override
    public Object getPrincipal() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
