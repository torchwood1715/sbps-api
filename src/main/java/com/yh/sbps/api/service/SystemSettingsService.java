package com.yh.sbps.api.service;

import com.yh.sbps.api.dto.SystemSettingsDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

  // Default values
  private static final Integer DEFAULT_POWER_LIMIT_WATTS = 3500;
  private static final Integer DEFAULT_POWER_ON_MARGIN_WATTS = 500;
  private static final Integer DEFAULT_OVERLOAD_COOLDOWN_SECONDS = 30;
  private static final Integer DEFAULT_POWER_SAVE_LIMIT_WATTS = 1500;
  private static final Logger logger = LoggerFactory.getLogger(SystemSettingsService.class);
  private final SystemSettingsRepository systemSettingsRepository;
  private final DeviceService deviceService;
  private final DeviceServiceWS deviceServiceWS;

  @Autowired
  public SystemSettingsService(
      SystemSettingsRepository systemSettingsRepository,
      DeviceService deviceService,
      DeviceServiceWS deviceServiceWS) {
    this.systemSettingsRepository = systemSettingsRepository;
    this.deviceService = deviceService;
    this.deviceServiceWS = deviceServiceWS;
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
    notifyDeviceServiceOfSettingsUpdate(user);
    return SystemSettingsDto.fromEntity(save);
  }

  private SystemSettings createDefaultSettings(User user) {
    SystemSettings defaultSettings =
        new SystemSettings(
            DEFAULT_POWER_LIMIT_WATTS,
            DEFAULT_POWER_ON_MARGIN_WATTS,
            DEFAULT_OVERLOAD_COOLDOWN_SECONDS,
            DEFAULT_POWER_SAVE_LIMIT_WATTS,
            user);
    return systemSettingsRepository.save(defaultSettings);
  }

  private void notifyDeviceServiceOfSettingsUpdate(User user) {
    try {
      Optional<Device> monitorOpt =
          deviceService.getAllDevices(user).stream()
              .filter(d -> d.getDeviceType() == DeviceType.POWER_MONITOR)
              .findFirst();

      monitorOpt.ifPresent(device -> deviceServiceWS.notifyStateRefresh(device.getMqttPrefix()));
    } catch (Exception e) {
      // don't fail the main operation
      logger.error(
          "Failed to notify device service of settings update for user {}: {}",
          user.getId(),
          e.getMessage());
    }
  }
}
