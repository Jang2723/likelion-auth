package com.example.auth.filters;

import com.example.auth.entity.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

// 모든 요청이 인증된 요청으로 취급하는 필터
@Slf4j
public class AllAuthenticatedFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("try all auth filter");
        // TODO 여기부터
        // 헤더에 'x-likelion-all-auth: true'가 포함된 요청은 로그인 한 요청이다.
        String header = request.getHeader("x-likelion-all-auth");
        if (header != null){
            // 사용자의 인증정보를 담고 잇는 객체
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            AbstractAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            CustomUserDetails.builder()
                                    .username(header)
                                    .password("subin")
                                    .email("subin@gmail.com")
                                    .phone("01012345678")
                                    .build(),
                            "아무거나", new ArrayList<>()
                    );
            // SecurityContext에 사용자 정보를 등록해준다.
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
            log.info("set security context with header");
        }else{
            log.info("all auth required header is absent");
        }
        // TODO 여기까지 내가 정의
        // 필터를 실행을 해우저야 한다. 실패하든 말든
        filterChain.doFilter(request, response);
    }
}
