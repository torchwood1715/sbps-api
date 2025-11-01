package com.yh.sbps.api.controller;

import com.yh.sbps.api.dto.SystemSettingsDto;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.service.SystemSettingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingsController {

  private final SystemSettingsService systemSettingsService;

  @Autowired
  public SystemSettingsController(SystemSettingsService systemSettingsService) {
    this.systemSettingsService = systemSettingsService;
  }

  @GetMapping
  public ResponseEntity<SystemSettingsDto> getSettings(@AuthenticationPrincipal User user) {
    SystemSettingsDto settings = systemSettingsService.getSettings(user);
    return ResponseEntity.ok(settings);
  }

  @PutMapping
  public ResponseEntity<SystemSettingsDto> updateSettings(
      @AuthenticationPrincipal User user, @Valid @RequestBody SystemSettingsDto newSettings) {
    SystemSettingsDto updatedSettings = systemSettingsService.updateSettings(user, newSettings);
    return ResponseEntity.ok(updatedSettings);
  }
}
