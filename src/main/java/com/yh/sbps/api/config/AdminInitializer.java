package com.yh.sbps.api.config;

import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.email}")
  private String adminEmail;

  @Value("${admin.password}")
  private String adminPassword;

  @Autowired
  public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    if (userRepository.findByEmail(adminEmail).isEmpty()) {
      User adminUser = new User();
      adminUser.setEmail(adminEmail);
      adminUser.setPassword(passwordEncoder.encode(adminPassword));
      adminUser.setRole(Role.ADMIN);

      userRepository.save(adminUser);
      System.out.println("Admin user created with email: " + adminEmail);
    } else {
      System.out.println("Admin user already exists with email: " + adminEmail);
    }
  }
}
