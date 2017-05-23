package com.msh.security.service;

import com.msh.common.mapper.UserMapper;
import com.msh.common.model.security.User;
import com.msh.security.WerewolfAuthenticationToken;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;


/**
 * Created by colorado on 9/03/17.
 */
public class WerewolfAuthProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(WerewolfAuthProvider.class);

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("Provider Manager Executed");
        WerewolfAuthenticationToken token = (WerewolfAuthenticationToken) authentication;
        String code = token.getCode();
        if(StringUtils.isNotEmpty(code)){
            try {
                wxMpService.oauth2getAccessToken(code);
            } catch (WxErrorException e) {
                logger.info("User trying google/login not already a registered user. Register Him !!");
            }
        } else {
            User user = new User();
            user.setUserName(token.getUserName());
            List<User> users = userMapper.select(user);
            if(users.size() == 0){

            }
        }

        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WerewolfAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
