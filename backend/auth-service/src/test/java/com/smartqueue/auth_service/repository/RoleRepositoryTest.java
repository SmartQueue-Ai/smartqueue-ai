package com.smartqueue.auth_service.repository;

import com.smartqueue.auth_service.config.JpaAuditingConfig;
import com.smartqueue.auth_service.domain.Role;
import com.smartqueue.auth_service.domain.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should save and find role by RoleName")
    void shouldSaveAndFindRoleByName() {
        Role role = Role.builder()
                .name(RoleName.ROLE_USER)
                .description("Standard User Role")
                .build();

        Role savedRole = roleRepository.save(role);

        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getCreatedAt()).isNotNull();
        assertThat(savedRole.getUpdatedAt()).isNotNull();

        Optional<Role> foundRole = roleRepository.findByName(RoleName.ROLE_USER);
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getDescription()).isEqualTo("Standard User Role");
    }

    @Test
    @DisplayName("Should enforce unique constraint on role name")
    void shouldEnforceUniqueConstraintOnRoleName() {
        Role role1 = Role.builder()
                .name(RoleName.ROLE_ADMIN)
                .description("Admin 1")
                .build();
        roleRepository.saveAndFlush(role1);

        Role role2 = Role.builder()
                .name(RoleName.ROLE_ADMIN)
                .description("Admin 2")
                .build();

        assertThatThrownBy(() -> roleRepository.saveAndFlush(role2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
