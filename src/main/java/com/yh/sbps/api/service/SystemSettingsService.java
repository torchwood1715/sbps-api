package com.yh.sbps.api.service;

import com.yh.sbps.api.dto.SystemSettingsDTO;
import com.yh.sbps.api.dto.mapper.SystemSettingsMapper;
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
  private final SystemSettingsMapper systemSettingsMapper;
  private final SystemSettingsRepository systemSettingsRepository;

  @Autowired
  public SystemSettingsService(
      SystemSettingsMapper systemSettingsMapper,
      SystemSettingsRepository systemSettingsRepository) {
    this.systemSettingsMapper = systemSettingsMapper;
    this.systemSettingsRepository = systemSettingsRepository;
  }

  public SystemSettingsDTO getSettings(User user) {
    return systemSettingsMapper.toDto(
        systemSettingsRepository.findByUser(user).orElseGet(() -> createDefaultSettings(user)));
  }

  public SystemSettingsDTO updateSettings(User user, SystemSettingsDTO newSettings) {
    SystemSettings existingSettings =
        systemSettingsRepository.findByUser(user).orElseGet(() -> createDefaultSettings(user));
    systemSettingsMapper.updateEntityFromDto(existingSettings, newSettings);

    SystemSettings save = systemSettingsRepository.save(existingSettings);
    return systemSettingsMapper.toDto(save);
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
