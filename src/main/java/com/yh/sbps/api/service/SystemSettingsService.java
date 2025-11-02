package com.yh.sbps.api.service;

import com.yh.sbps.api.dto.SystemSettingsDto;
import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

  // Default values
  private static final Integer DEFAULT_POWER_LIMIT_WATTS = 3500;
  private static final Integer DEFAULT_POWER_ON_MARGIN_WATTS = 500;
  private static final Integer DEFAULT_OVERLOAD_COOLDOWN_SECONDS = 30;
  private final SystemSettingsRepository systemSettingsRepository;

  @Autowired
  public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
    this.systemSettingsRepository = systemSettingsRepository;
  }

  public SystemSettingsDto getSettings(User user) {
    return SystemSettingsDto.fromEntity(
        systemSettingsRepository.findByUser(user).orElseGet(() -> createDefaultSettings(user)));
  }

  public SystemSettingsDto updateSettings(User user, SystemSettingsDto newSettings) {
    SystemSettings existingSettings =
        systemSettingsRepository.findByUser(user).orElseGet(() -> createDefaultSettings(user));
    SystemSettingsDto.toEntity(existingSettings, newSettings);

    SystemSettings save = systemSettingsRepository.save(existingSettings);
    return SystemSettingsDto.fromEntity(save);
  }

  private SystemSettings createDefaultSettings(User user) {
    SystemSettings defaultSettings =
        new SystemSettings(
            DEFAULT_POWER_LIMIT_WATTS,
            DEFAULT_POWER_ON_MARGIN_WATTS,
            DEFAULT_OVERLOAD_COOLDOWN_SECONDS,
            user);
    return systemSettingsRepository.save(defaultSettings);
  }
}
