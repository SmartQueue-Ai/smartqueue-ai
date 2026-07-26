package com.smartqueue.auth_service.repository;

import com.smartqueue.auth_service.config.JpaAuditingConfig;
import com.smartqueue.auth_service.domain.Role;
import com.smartqueue.auth_service.domain.RoleName;
import com.smartqueue.auth_service.domain.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should save user with roles and verify persistence")
    void shouldSaveUserWithRoles() {
        Role userRole = roleRepository.save(new Role(RoleName.ROLE_USER, "User Role"));
        Role adminRole = roleRepository.save(new Role(RoleName.ROLE_ADMIN, "Admin Role"));

        User user = User.builder()
                .username("diyasha")
                .email("diyasha@smartqueue.com")
                .passwordHash("hashed_password_123")
                .firstName("Diyasha")
                .lastName("Nag")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        user.addRole(userRole);
        user.addRole(adminRole);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(2);

        Optional<User> foundUser = userRepository.findByUsernameWithRoles("diyasha");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("diyasha@smartqueue.com");
        assertThat(foundUser.get().getRoles()).extracting(Role::getName).contains(RoleName.ROLE_USER, RoleName.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should enforce unique username constraint")
    void shouldEnforceUniqueUsernameConstraint() {
        User user1 = User.builder()
                .username("alex")
                .email("alex1@smartqueue.com")
                .passwordHash("hash")
                .build();
        userRepository.saveAndFlush(user1);

        User user2 = User.builder()
                .username("alex")
                .email("alex2@smartqueue.com")
                .passwordHash("hash")
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void shouldEnforceUniqueEmailConstraint() {
        User user1 = User.builder()
                .username("user1")
                .email("shared@smartqueue.com")
                .passwordHash("hash")
                .build();
        userRepository.saveAndFlush(user1);

        User user2 = User.builder()
                .username("user2")
                .email("shared@smartqueue.com")
                .passwordHash("hash")
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should check existence by username and email")
    void shouldCheckExistenceByUsernameAndEmail() {
        User user = User.builder()
                .username("diptanshu")
                .email("diptanshu@smartqueue.com")
                .passwordHash("hash")
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("diptanshu")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        assertThat(userRepository.existsByEmail("diptanshu@smartqueue.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@smartqueue.com")).isFalse();
    }
}
