package com.msh.security.service;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.msh.common.mapper.UserMapper;
import com.msh.common.model.security.User;
import com.msh.security.WerewolfAuthenticationToken;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
                WxMpOAuth2AccessToken wxMpOAuth2AccessToken =wxMpService.oauth2getAccessToken(code);
                String openId = wxMpOAuth2AccessToken.getOpenId();
                User user = new User();
                user.setOpenid(openId);
                List<User> users = userMapper.select(user);
                if(users.size() == 0){
                    WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
                    BeanUtils.copyProperties(wxMpUser, user);
                    userMapper.insert(user);
                    token.setHeadImgUrl(user.getHeadimgurl());
                }else {
                    token.setHeadImgUrl(users.get(0).getHeadimgurl());
                }
                return token;
            } catch (WxErrorException e) {
                logger.info("User trying google/login not already a registered user. Register Him !!");
                return null;
            }
        } else {
            User user = new User();
            user.setUserName(token.getUserName());
            List<User> users = userMapper.select(user);
            if(users.size() == 0){
                return null;
            }else {
                return token;
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WerewolfAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}
