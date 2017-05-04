package com.msh.security.service;

import com.msh.security.WechatAuthenticationToken;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;


/**
 * Created by colorado on 9/03/17.
 */
public class WechatAuthProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(WechatAuthProvider.class);

    @Autowired
    private WxMpService wxMpService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("Provider Manager Executed");
        WechatAuthenticationToken token = (WechatAuthenticationToken) authentication;
        String code = (String) token.getPrincipal();
        try {
            wxMpService.oauth2getAccessToken(code);
        } catch (WxErrorException e) {
            logger.info("User trying google/login not already a registered user. Register Him !!");
        }
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WechatAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
