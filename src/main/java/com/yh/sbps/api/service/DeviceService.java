package com.yh.sbps.api.service;

import static com.yh.sbps.api.entity.Role.SERVICE_USER;

import com.yh.sbps.api.dto.DeviceRequestDto;
import com.yh.sbps.api.dto.DeviceResponseDto;
import com.yh.sbps.api.dto.SystemSettingsDto;
import com.yh.sbps.api.dto.SystemStateDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.repository.DeviceRepository;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeviceService {

  private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
  private final DeviceRepository deviceRepository;
  private final SystemSettingsRepository systemSettingsRepository;
  private final DeviceServiceWS deviceServiceWS;

  @Autowired
  public DeviceService(
      DeviceRepository deviceRepository,
      SystemSettingsRepository systemSettingsRepository,
      DeviceServiceWS deviceServiceWS) {
    this.deviceRepository = deviceRepository;
    this.systemSettingsRepository = systemSettingsRepository;
    this.deviceServiceWS = deviceServiceWS;
  }

  public List<Device> getAllDevices(User user) {
    if (user.getRole() == SERVICE_USER) {
      return deviceRepository.findAll();
    }
    return deviceRepository.findAllByUserOrderByDeviceTypeAscNameAsc(user);
  }

  public Optional<Device> getDeviceById(Long id) {
    return deviceRepository.findById(id);
  }

  public Device saveDevice(DeviceRequestDto deviceDto, User user) {
    if (deviceDto.getDeviceType() == DeviceType.POWER_MONITOR
        || deviceDto.getDeviceType() == DeviceType.GRID_MONITOR) {

      boolean monitorExists =
          deviceRepository.existsByUserAndDeviceType(user, deviceDto.getDeviceType());

      if (monitorExists) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Monitor type '" + deviceDto.getDeviceType() + "' is already exists for user.");
      }
    }
    Device device = DeviceRequestDto.toEntity(null, deviceDto);
    device.setUser(user);
    Device saved = deviceRepository.save(device);
    deviceServiceWS.notifyDeviceUpdate(saved);
    if (saved.getDeviceType() == DeviceType.POWER_MONITOR) {
      deviceServiceWS.notifyStateRefresh(saved.getMqttPrefix());
    }
    return saved;
  }

  public Device updateDevice(Long id, DeviceRequestDto deviceDetailsDto, User user) {
    Device existingDevice =
        deviceRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + id));

    if (!existingDevice.getUser().getId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not own this device");
    }

    DeviceType newType = deviceDetailsDto.getDeviceType();
    DeviceType oldType = existingDevice.getDeviceType();

    if (newType != oldType
        && (newType == DeviceType.POWER_MONITOR || newType == DeviceType.GRID_MONITOR)) {
      boolean monitorExists = deviceRepository.existsByUserAndDeviceType(user, newType);
      if (monitorExists) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Монітор типу '" + newType + "' вже існує. Ви не можете змінити тип цього пристрою.");
      }
    }

    String oldMqttPrefix = existingDevice.getMqttPrefix();

    DeviceRequestDto.toEntity(existingDevice, deviceDetailsDto);
    Device saved = deviceRepository.save(existingDevice);

    String newMqttPrefix = saved.getMqttPrefix();
    String monitorPrefixForRefresh = null;
    if (oldMqttPrefix != null && !oldMqttPrefix.equals(newMqttPrefix)) {
      logger.info("MQTT prefix changed for device {}. Re-subscribing.", saved.getName());
      deviceServiceWS.notifyDeviceDelete(oldMqttPrefix);
      deviceServiceWS.notifyDeviceUpdate(saved);
    }

    if (saved.getDeviceType() == DeviceType.POWER_MONITOR) {
      monitorPrefixForRefresh = newMqttPrefix;
    } else if (oldType == DeviceType.POWER_MONITOR && newType != DeviceType.POWER_MONITOR) {
      deviceServiceWS.notifyDeviceDelete(oldMqttPrefix);
    } else {
      monitorPrefixForRefresh = findMonitorPrefixForUser(user);
    }

    if (monitorPrefixForRefresh != null) {
      deviceServiceWS.notifyStateRefresh(monitorPrefixForRefresh);
    }

    return saved;
  }

  public void deleteDevice(Long id, User user) {
    Device device =
        deviceRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + id));

    if (device.getUser() == null || !device.getUser().getId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User can only delete own devices");
    }

    String monitorPrefix = findMonitorPrefixForUser(user);
    if (device.getMqttPrefix() != null) {
      deviceServiceWS.notifyDeviceDelete(device.getMqttPrefix());
    }
    if (monitorPrefix != null) {
      deviceServiceWS.notifyStateRefresh(monitorPrefix);
    }

    deviceRepository.deleteById(id);
  }

  public SystemStateDto getSystemStateByMqttPrefix(String mqttPrefix) {
    Device device =
        deviceRepository
            .findByMqttPrefix(mqttPrefix)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Device with MQTT prefix not found: " + mqttPrefix));
    User user = device.getUser();
    if (user == null) {
      throw new IllegalStateException("Device is not associated with any user");
    }
    SystemSettingsDto systemSettings =
        SystemSettingsDto.fromEntity(
            systemSettingsRepository
                .findByUser(user)
                .orElseThrow(
                    () ->
                        new EntityNotFoundException("User settings not found with user: " + user)));
    List<DeviceResponseDto> allDevices =
        deviceRepository.findAllByUser(user).stream().map(DeviceResponseDto::from).toList();
    return new SystemStateDto(systemSettings, allDevices);
  }

  public String getMonitorPrefixForUser(User user) {
    return deviceRepository.findAllByUser(user).stream()
        .filter(d -> d.getDeviceType() == DeviceType.POWER_MONITOR)
        .map(Device::getMqttPrefix)
        .findFirst()
        .orElse(null);
  }

  private String findMonitorPrefixForUser(User user) {
    return deviceRepository.findAllByUser(user).stream()
        .filter(d -> d.getDeviceType() == DeviceType.POWER_MONITOR)
        .map(Device::getMqttPrefix)
        .findFirst()
        .orElse(null);
  }
}
