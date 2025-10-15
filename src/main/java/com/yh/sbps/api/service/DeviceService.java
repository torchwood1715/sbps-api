package com.yh.sbps.api.service;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.DeviceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private final DeviceRepository deviceRepository;

  @Autowired
  public DeviceService(DeviceRepository deviceRepository) {
    this.deviceRepository = deviceRepository;
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
    device.setType(deviceDetails.getType());
    device.setPriority(deviceDetails.getPriority());
    device.setWattage(deviceDetails.getWattage());

    return deviceRepository.save(device);
  }

  public void deleteDevice(Long id, User user) {
    Device device = deviceRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

    if (device.getUser() == null || !device.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("You can only delete your own devices");
    }

    deviceRepository.deleteById(id);
  }
}
