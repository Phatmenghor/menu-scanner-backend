package com.emenu.security;

import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdentifier) throws UsernameNotFoundException {
        log.debug("Attempting to load user with userIdentifier: {}", userIdentifier);
        
        // ✅ UPDATED: Search by userIdentifier
        User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userIdentifier: " + userIdentifier));

        log.debug("User found: {} with userIdentifier: {}", user.getFullName(), user.getUserIdentifier());

        return new org.springframework.security.core.userdetails.User(
                user.getUserIdentifier(), // Use userIdentifier as the principal name
                user.getPassword(),
                user.getAccountStatus().name().equals("ACTIVE"),
                true,
                true,
                !user.getAccountStatus().name().equals("LOCKED"),
                mapRolesToAuthorities(user)
        );
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());
    }
}