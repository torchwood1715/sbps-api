package com.yh.sbps.api.dto.mapper;

import com.yh.sbps.api.dto.DeviceRequestDTO;
import com.yh.sbps.api.dto.DeviceResponseDTO;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

  public Device toEntity(DeviceRequestDTO dto, User user) {
    Device device = new Device();
    device.setName(dto.getName());
    device.setMqttPrefix(dto.getMqttPrefix());
    device.setDeviceType(dto.getDeviceType());
    device.setPriority(dto.getPriority());
    device.setWattage(dto.getWattage());
    device.setUser(user);
    return device;
  }

  public void updateEntityFromDto(Device device, DeviceRequestDTO dto) {
    device.setName(dto.getName());
    device.setMqttPrefix(dto.getMqttPrefix());
    device.setDeviceType(dto.getDeviceType());
    device.setPriority(dto.getPriority());
    device.setWattage(dto.getWattage());
  }

  public DeviceResponseDTO toResponseDTO(Device device) {
    DeviceResponseDTO dto = new DeviceResponseDTO();
    dto.setId(device.getId());
    dto.setName(device.getName());
    dto.setMqttPrefix(device.getMqttPrefix());
    dto.setDeviceType(device.getDeviceType());
    dto.setPriority(device.getPriority());
    dto.setWattage(device.getWattage());

    if (device.getUser() != null) {
      dto.setUsername(device.getUser().getUsername());
    }
    return dto;
  }

  public List<DeviceResponseDTO> toResponseDTOList(List<Device> devices) {
    return devices.stream().map(this::toResponseDTO).collect(Collectors.toList());
  }
}
