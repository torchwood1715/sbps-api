package com.yh.sbps.api.service;

import com.yh.sbps.api.entity.Device;
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

  public List<Device> getAllDevices() {
    return deviceRepository.findAll();
  }

  public Optional<Device> getDeviceById(Long id) {
    return deviceRepository.findById(id);
  }

  public Device saveDevice(Device device) {
    return deviceRepository.save(device);
  }

  public Device updateDevice(Long id, Device deviceDetails) {
    Device device =
        deviceRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

    device.setName(deviceDetails.getName());
    device.setMqttPrefix(deviceDetails.getMqttPrefix());
    device.setType(deviceDetails.getType());
    device.setPriority(deviceDetails.getPriority());

    return deviceRepository.save(device);
  }

  public void deleteDevice(Long id) {
    if (!deviceRepository.existsById(id)) {
      throw new RuntimeException("Device not found with id: " + id);
    }
    deviceRepository.deleteById(id);
  }
}
