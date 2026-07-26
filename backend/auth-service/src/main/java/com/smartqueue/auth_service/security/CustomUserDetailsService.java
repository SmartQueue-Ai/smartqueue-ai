package com.smartqueue.auth_service.security;

import com.smartqueue.auth_service.domain.User;
import com.smartqueue.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmailWithRoles(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + usernameOrEmail)));

        return UserPrincipal.create(user);
    }
}
