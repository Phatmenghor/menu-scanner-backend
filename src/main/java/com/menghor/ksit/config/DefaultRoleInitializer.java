package com.menghor.ksit.config;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
public class DefaultRoleInitializer {

    private final RoleRepository roleRepository;

    public DefaultRoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(RoleEnum.DEVELOPER));
            roleRepository.save(new Role(RoleEnum.ADMIN));
            roleRepository.save(new Role(RoleEnum.STAFF));
            roleRepository.save(new Role(RoleEnum.STUDENT));
        }
    }
}
