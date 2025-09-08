package com.virinchi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.virinchi.model.UserClass;
import com.virinchi.repository.UserRepository;

@Component
public class AdminBootstrap implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Value("${inkedmind.bootstrap.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${inkedmind.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${inkedmind.bootstrap.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!bootstrapEnabled) return;
        try {
            long adminCount = userRepository.countByRoleIgnoreCase("Admin");
            if (adminCount > 0) return;

            if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                System.err.println("[BOOTSTRAP] Admin bootstrap enabled but email/password not provided");
                return;
            }

            String hashed = DigestUtils.md5DigestAsHex(adminPassword.getBytes());

            UserClass user;
            if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
                user = userRepository.findTopByEmailOrderByIdDesc(adminEmail);
                if (user == null) user = new UserClass();
            } else {
                user = new UserClass();
                user.setEmail(adminEmail);
            }
            if (user.getFirstName() == null || user.getFirstName().isBlank()) user.setFirstName("Admin");
            user.setRole("Admin");
            user.setPassword(hashed);
            userRepository.save(user);

            System.out.println("[BOOTSTRAP] Admin user ensured for email: " + adminEmail);
        } catch (Exception e) {
            System.err.println("[BOOTSTRAP] Failed to bootstrap admin: " + e.getMessage());
        }
    }
}

