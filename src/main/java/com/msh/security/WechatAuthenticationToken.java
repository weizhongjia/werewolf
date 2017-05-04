package com.msh.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Created by colorado on 9/03/17.
 */
public class WechatAuthenticationToken extends AbstractAuthenticationToken {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8254831403638075928L;

    private String code;

    private String openId;

    public WechatAuthenticationToken(
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public WechatAuthenticationToken(String openId, String code){
        super((Collection)null);
        this.code = code;
        this.openId = openId;
    }

    public WechatAuthenticationToken(String code) {
        super((Collection)null);
        this.code = code;
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
        setDetails(code);
    }
}
