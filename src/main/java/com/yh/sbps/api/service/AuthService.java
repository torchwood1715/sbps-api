package com.yh.sbps.api.service;

import com.yh.sbps.api.dto.AuthResponseDto;
import com.yh.sbps.api.dto.LoginRequestDto;
import com.yh.sbps.api.dto.RegisterRequestDto;
import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Autowired
  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  public AuthResponseDto register(RegisterRequestDto request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new RuntimeException("User already exists with email: " + request.getEmail());
    }
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new RuntimeException("Username already taken: " + request.getUsername());
    }
    User user =
        new User(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getUsername(),
            Role.USER);

    userRepository.save(user);

    String token = jwtService.generateToken(user);

    return new AuthResponseDto(token);
  }

  public AuthResponseDto login(LoginRequestDto request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));

    String token = jwtService.generateToken(user);

    return new AuthResponseDto(token);
  }
}
