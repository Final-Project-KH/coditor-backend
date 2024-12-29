package com.kh.totalproject.service;

import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException{
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found : " + userId));

        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getId(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getUserStatus().toString())  )
        );
    }
}
