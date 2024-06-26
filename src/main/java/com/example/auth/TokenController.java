package com.example.auth;

import com.example.auth.jwt.JwtRequestDto;
import com.example.auth.jwt.JwtResponseDto;
import com.example.auth.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("token")
@RequiredArgsConstructor
public class TokenController {
    // JWT를 발급하기 위한 Bean
    private final JwtTokenUtils jwtTokenUtils;
    // 사용자 정보를 회수하기 위한 Bean
    private final UserDetailsManager manager;
    // 사용자가 제공한 아이디 비밀번호를 비교하기 위한 클래스
    private final PasswordEncoder passwordEncoder;

    // POST /token/issue
    @PostMapping("/issue")
    public JwtResponseDto issueJwt(
            @RequestBody JwtRequestDto dto
    ) {
        // 사용자가 제공한 username, passord가 저장된 사용자인지
        if (!manager.userExists(dto.getUsername()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UserDetails userDetails
                = manager.loadUserByUsername(dto.getUsername());

        // 비밀번호 대조
        /*if (userDetails.getPassword()
                .equals(passwordEncoder.encode(dto.getPassword())))*/
        if (!passwordEncoder
                .matches(dto.getPassword(), userDetails.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // JWT 발급
        String jwt = jwtTokenUtils.generateToken(userDetails);
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response; // WebSecurityConfig에 "/token/issue"추가해줌
     }

    @GetMapping("/validate")
//    public Claims validateToken(
//        @RequestParam("token")
//        String token,
//        @RequestParam("code")
//        String code
//    ) {
//        if (!jwtTokenUtils.validate(token))
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
//
//        return jwtTokenUtils.parseClaims(token);
//    }
    public ResponseEntity<String> validateToken(
            @RequestParam("token") String token,
            @RequestParam("code") String code
    ) {
        if (!jwtTokenUtils.validate(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Claims claims = jwtTokenUtils.parseClaims(token);

        String responseMessage = String.format("Token claims: %s\n\n Code: %s", claims, code);

        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

}
