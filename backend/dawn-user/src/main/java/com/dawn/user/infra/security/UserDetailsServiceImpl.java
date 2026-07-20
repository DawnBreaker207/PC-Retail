package com.dawn.user.infra.security;

import com.dawn.common.message.Message;
import com.dawn.user.infra.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JpaUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException(Message.format(Message.User.NOT_FOUND, email)));
    }
}
