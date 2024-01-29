package com.example.auth.jwt;

import com.example.auth.entity.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j // Bean으로 등록하지 않는 이유: 수동으로 webSecurityConfig에서 등록해줘야 하는데 빈으로 등록하면 필터에 두번 등록되기 때문
//@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    // 사용자 정보를 찾기위한 UserDetailsService 또는 Manager
    private final UserDetailsManager manager;

    public JwtTokenFilter(
            JwtTokenUtils jwtTokenUtils,
            UserDetailsManager manager
    ){
        this.jwtTokenUtils = jwtTokenUtils;
        this.manager = manager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.debug("try jwt filter");
        // 1. Authorization 헤더를 회수
        String authHeader
                // = request.getHeader("Authorization");
                = request.getHeader(HttpHeaders.AUTHORIZATION);
        // 2. Authorization 헤더가 존재하는지 + Bearer로 시작하는지
        if(authHeader != null&& authHeader.startsWith("Bearer ")) {
            String token = authHeader.split(" ")[1];
            // 3. Token 이 유효한 토큰인지
            if (jwtTokenUtils.validate(token)){
                // 4. 유효하다면 해당 토큰을 바탕으로 사용자 정보를 SecurityContext에 등록
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                // 사용자 정보 회수
                String username = jwtTokenUtils
                        .parseClaims(token)
                        .getSubject();
                // 인증 정보 생성
                AbstractAuthenticationToken authenticationToken
                        = new UsernamePasswordAuthenticationToken(
//                        CustomUserDetails.builder()
//                                .username(username)
//                                .build(),/
                        // manager에서 실제 사용자 정보 조회
                        manager.loadUserByUsername(username),
                        token, new ArrayList<>()
                );
                // 인증 정보 등록
                context.setAuthentication(authenticationToken);
                SecurityContextHolder.setContext(context);
                log.info("set security context with jwt");
            }
            else {
                log.warn("jwt validation failed");
            }
        }
        // 5. 다음 필터 호출
        // doFilter를 호출하지 않으면 Controller 까지 요청이 도달하지 못한다.
        filterChain.doFilter(request, response);
    }
}