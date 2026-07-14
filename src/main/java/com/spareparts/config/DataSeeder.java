package com.spareparts.config;

import com.spareparts.entity.Role;
import com.spareparts.entity.User;
import com.spareparts.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        upsertUser("admin",   "admin@spareparts.com",   "admin123", Role.ADMIN);
        upsertUser("manager", "manager@spareparts.com", "admin123", Role.MANAGER);
        upsertUser("user",    "user@spareparts.com",    "admin123", Role.USER);
    }

    private void upsertUser(String username, String email, String password, Role role) {
        userRepository.findByEmail(email).ifPresent(u -> userRepository.delete(u));
        userRepository.flush();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);

        boolean valid = passwordEncoder.matches(password, user.getPassword());
        log.info("Seeded {} | role: {} | password verify: {}", email, role, valid);
    }
}
