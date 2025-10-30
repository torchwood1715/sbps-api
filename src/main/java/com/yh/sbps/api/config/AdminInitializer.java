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

  @Value("${service-user.email}")
  private String serviceUserEmail;

  @Value("${service-user.password}")
  private String serviceUserPassword;

  @Autowired
  public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    createAdminUser();
    createServiceUser();
  }

  private void createAdminUser() {
    if (userRepository.findByEmail(adminEmail).isEmpty()) {
      User adminUser = new User();
      adminUser.setEmail(adminEmail);
      adminUser.setPassword(passwordEncoder.encode(adminPassword));
      adminUser.setUsername("admin");
      adminUser.setRole(Role.ADMIN);

      userRepository.save(adminUser);
      System.out.println("Admin user created: " + adminEmail);
    }
  }

  private void createServiceUser() {
    if (userRepository.findByEmail(serviceUserEmail).isEmpty()) {
      User serviceUser = new User();
      serviceUser.setEmail(serviceUserEmail);
      serviceUser.setPassword(passwordEncoder.encode(serviceUserPassword));
      serviceUser.setUsername("service-user");
      serviceUser.setRole(Role.SERVICE_USER);

      userRepository.save(serviceUser);
      System.out.println("Service user created: " + serviceUserEmail);
    }
  }
}
