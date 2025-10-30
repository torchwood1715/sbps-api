package com.yh.sbps.api.service;

import static com.yh.sbps.api.entity.Role.SERVICE_USER;

import com.yh.sbps.api.dto.DeviceRequestDTO;
import com.yh.sbps.api.dto.DeviceResponseDTO;
import com.yh.sbps.api.dto.SystemSettingsDTO;
import com.yh.sbps.api.dto.SystemStateDto;
import com.yh.sbps.api.dto.mapper.DeviceMapper;
import com.yh.sbps.api.dto.mapper.SystemSettingsMapper;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.repository.DeviceRepository;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeviceService {

  private final DeviceMapper deviceMapper;
  private final SystemSettingsMapper systemSettingsMapper;
  private final DeviceRepository deviceRepository;
  private final SystemSettingsRepository systemSettingsRepository;
  private final DeviceServiceWS deviceServiceWS;

  @Autowired
  public DeviceService(
      DeviceMapper deviceMapper,
      SystemSettingsMapper systemSettingsMapper,
      DeviceRepository deviceRepository,
      SystemSettingsRepository systemSettingsRepository,
      DeviceServiceWS deviceServiceWS) {
    this.deviceMapper = deviceMapper;
    this.systemSettingsMapper = systemSettingsMapper;
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

  public Device saveDevice(DeviceRequestDTO deviceDto, User user) {
    if (deviceDto.getDeviceType() == DeviceType.POWER_MONITOR) {
      boolean monitorExists =
          deviceRepository.existsByUserAndDeviceType(user, DeviceType.POWER_MONITOR);
      if (monitorExists) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Power monitor already exists for this user.");
      }
    }
    Device device = deviceMapper.toEntity(deviceDto, user);
    device.setUser(user);
    Device saved = deviceRepository.save(device);
    deviceServiceWS.notifyDeviceUpdate(saved);
    return saved;
  }

  public Device updateDevice(Long id, DeviceRequestDTO deviceDetailsDto, User user) {
    Device existingDevice =
        deviceRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

    if (!existingDevice.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("User does not own this device");
    }

    deviceMapper.updateEntityFromDto(existingDevice, deviceDetailsDto);
    Device saved = deviceRepository.save(existingDevice);
    deviceServiceWS.notifyDeviceUpdate(saved);
    return saved;
  }

  public void deleteDevice(Long id, User user) {
    Device device =
        deviceRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

    if (device.getUser() == null || !device.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You can only delete your own devices");
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
    SystemSettingsDTO systemSettings =
        systemSettingsMapper.toDto(
            systemSettingsRepository
                .findByUser(user)
                .orElseThrow(
                    () ->
                        new EntityNotFoundException("User settings not found with user: " + user)));
    List<DeviceResponseDTO> allDevices = deviceMapper.toResponseDTOList(deviceRepository.findAllByUser(user));
    return new SystemStateDto(systemSettings, allDevices);
  }
}
