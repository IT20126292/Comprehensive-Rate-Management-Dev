package com.sampath.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sampath.portal.entity.User;
import com.sampath.portal.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> {
                    log.warn("User not found in CUSSEG schema: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.info("User found: {}", username);
        return user;
    }
}
