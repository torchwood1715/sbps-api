package com.yh.sbps.api.controller;

import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.service.SystemSettingsService;
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

  @GetMapping("/")
  public ResponseEntity<SystemSettings> getSettings(@AuthenticationPrincipal User user) {
    SystemSettings settings = systemSettingsService.getSettings(user);
    return ResponseEntity.ok(settings);
  }

  @PutMapping("/")
  public ResponseEntity<SystemSettings> updateSettings(
      @AuthenticationPrincipal User user, @RequestBody SystemSettings newSettings) {
    SystemSettings updatedSettings = systemSettingsService.updateSettings(user, newSettings);
    return ResponseEntity.ok(updatedSettings);
  }
}
