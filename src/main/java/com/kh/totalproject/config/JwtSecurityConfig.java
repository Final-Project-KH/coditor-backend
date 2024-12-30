package com.kh.totalproject.config;


import com.kh.totalproject.util.JwtFilter;
import com.kh.totalproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private final JwtUtil jwtUtil;

    @Override
    public void configure(HttpSecurity http){
        JwtFilter customFilter = new JwtFilter(jwtUtil);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
