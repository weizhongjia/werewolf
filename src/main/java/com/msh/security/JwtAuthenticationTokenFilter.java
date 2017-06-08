package com.msh.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.msh.common.model.security.AuthorityName;
import com.msh.common.model.security.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authToken = request.getHeader(this.tokenHeader);
        // authToken.startsWith("Bearer ")
        // String authToken = header.substring(7);
        User user = jwtTokenUtil.getUserFromToken(authToken);

        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            WerewolfAuthenticationToken authentication = createToken(user);
            if (jwtTokenUtil.validateToken(authToken)) {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                logger.info("authenticated user " + user.getUserName() + "or" + user.getOpenid() + ", setting security context");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    private static WerewolfAuthenticationToken createToken (User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (StringUtils.isNotEmpty(user.getUserName())) {
            authorities.add(new SimpleGrantedAuthority(AuthorityName.ROLE_ADMIN.name()));
            return new WerewolfAuthenticationToken(user.getUserName(), authorities);
        }else if (StringUtils.isNotEmpty(user.getOpenid())){
            authorities.add(new SimpleGrantedAuthority(AuthorityName.ROLE_USER.name()));
            return new WerewolfAuthenticationToken(user.getOpenid(), user.getHeadimgurl(), authorities);
        }else {
            return null;
        }

    }
}