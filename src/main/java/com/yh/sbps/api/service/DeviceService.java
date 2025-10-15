package com.yh.sbps.api.service;

import com.yh.sbps.api.dto.SystemStateDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.DeviceRepository;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final SystemSettingsRepository systemSettingsRepository;

  @Autowired
  public DeviceService(
      DeviceRepository deviceRepository, SystemSettingsRepository systemSettingsRepository) {
    this.deviceRepository = deviceRepository;
    this.systemSettingsRepository = systemSettingsRepository;
  }

  public List<Device> getAllDevices(User user) {
    return deviceRepository.findAllByUser(user);
  }

  public Optional<Device> getDeviceById(Long id) {
    return deviceRepository.findById(id);
  }

  public Device saveDevice(Device device, User user) {
    device.setUser(user);
    return deviceRepository.save(device);
  }

  public Device updateDevice(Long id, Device deviceDetails, User user) {
    Device device =
        deviceRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

    if (device.getUser() == null || !device.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You can only update your own devices");
    }

    device.setName(deviceDetails.getName());
    device.setMqttPrefix(deviceDetails.getMqttPrefix());
    device.setDeviceType(deviceDetails.getDeviceType());
    device.setPriority(deviceDetails.getPriority());
    device.setWattage(deviceDetails.getWattage());

    return deviceRepository.save(device);
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
    SystemSettings systemSettings =
        systemSettingsRepository
            .findByUser(user)
            .orElseThrow(
                () -> new EntityNotFoundException("User settings not found with user: " + user));
    List<Device> allDevices = deviceRepository.findAllByUser(user);
    return new SystemStateDto(systemSettings, allDevices);
  }
}
