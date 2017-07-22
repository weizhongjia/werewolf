package com.msh.security.controller;

import com.msh.common.mapper.UserMapper;
import com.msh.security.JwtAuthenticationRequest;
import com.msh.security.JwtTokenUtil;
import com.msh.security.WerewolfAuthenticationToken;
import com.msh.security.service.JwtAuthenticationResponse;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthenticationRestController {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${security.oauth2.client.url}")
    private String url;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WxMpService wxMpService;

    @CrossOrigin
    @RequestMapping(value = "/werewolf/auth", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, Device device) throws Exception {

        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
            new WerewolfAuthenticationToken(authenticationRequest)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final String token = jwtTokenUtil.generateToken((WerewolfAuthenticationToken) authentication, device);

        // Return the token
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

//    @RequestMapping(value = "/werewolf/refresh", method = RequestMethod.GET)
//    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
//        String token = request.getHeader(tokenHeader);
//        String username = jwtTokenUtil.getUsernameFromToken(token);
//        JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
//
//        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
//            String refreshedToken = jwtTokenUtil.refreshToken(token);
//            return ResponseEntity.ok(new JwtAuthenticationResponse(refreshedToken));
//        } else {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }

    @CrossOrigin
    @RequestMapping(value = "/werewolf/auth/web", method = RequestMethod.GET)
    public void wechatWebOauthLogin(Device device, HttpServletResponse response, @RequestParam String code) throws Exception {
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest();
        authenticationRequest.setCode(code);
        final Authentication authentication = authenticationManager.authenticate(
                new WerewolfAuthenticationToken(authenticationRequest)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final String token = jwtTokenUtil.generateToken((WerewolfAuthenticationToken) authentication, device);
        response.sendRedirect(url+"?token="+token);
    }

}
