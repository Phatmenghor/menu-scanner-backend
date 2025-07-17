package com.emenu.feature.auth.security;

import com.emenu.enumations.Status;
import com.emenu.feature.auth.models.Role;
import com.emenu.feature.auth.models.UserEntity;
import com.emenu.feature.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        log.debug("Found user: {} with status: {}", username, user.getStatus());

        // Validate user status and throw appropriate exceptions
        validateUserStatus(user);

        // Create UserDetails with proper flags
        UserDetails userDetails = new User(
                user.getUsername(),
                user.getPassword(),
                isUserEnabled(user),        // enabled
                true,                       // accountNonExpired
                true,                       // credentialsNonExpired
                isAccountNonLocked(user),   // accountNonLocked
                mapRolesToAuthorities(user.getRoles())
        );

        log.debug("Successfully loaded user details for: {}", username);
        return userDetails;
    }

    /**
     * Validate user status and throw specific exceptions
     */
    private void validateUserStatus(UserEntity user) {
        // Set default status if null
        if (user.getStatus() == null) {
            log.info("User {} has null status, setting to ACTIVE", user.getUsername());
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
            return;
        }

        switch (user.getStatus()) {
            case DELETED:
                log.warn("Account is deleted for user: {}", user.getUsername());
                throw new LockedException("Your account has been permanently deleted and cannot be recovered. Please contact the administrator if you believe this is an error.");

            case INACTIVE:
                log.warn("Account is inactive for user: {}", user.getUsername());
                throw new DisabledException("Your account has been temporarily deactivated. Please contact the administrator to reactivate your account.");

            case ACTIVE:
                log.debug("User {} is active and can login", user.getUsername());
                break;

            default:
                log.warn("User {} has unknown status: {}", user.getUsername(), user.getStatus());
                throw new DisabledException("Your account status is invalid. Please contact the administrator for assistance.");
        }
    }

    /**
     * Check if user account is not locked
     * DELETED users are considered locked (permanent)
     * INACTIVE users are disabled but not locked (can be reactivated)
     */
    private boolean isAccountNonLocked(UserEntity user) {
        if (user.getStatus() == null) {
            return true; // Default to unlocked if status is null
        }

        boolean nonLocked = user.getStatus() != Status.DELETED;
        log.debug("User {} account non-locked status: {}", user.getUsername(), nonLocked);
        return nonLocked;
    }

    /**
     * Check if user account is enabled (active)
     * Only ACTIVE users can login
     */
    private boolean isUserEnabled(UserEntity user) {
        if (user.getStatus() == null) {
            return true; // Default to enabled if status is null
        }

        boolean enabled = user.getStatus() == Status.ACTIVE;
        log.debug("User {} enabled status: {}", user.getUsername(), enabled);
        return enabled;
    }

    /**
     * Maps role entities to Spring Security GrantedAuthorities
     * Each role name becomes an authority
     */
    private Collection<GrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        log.debug("Mapped authorities: {}", authorities);
        return authorities;
    }
}