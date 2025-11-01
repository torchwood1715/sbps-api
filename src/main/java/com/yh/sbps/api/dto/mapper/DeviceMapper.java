package com.yh.sbps.api.dto.mapper;

import com.yh.sbps.api.dto.DeviceRequestDto;
import com.yh.sbps.api.dto.DeviceResponseDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

  public Device toEntity(DeviceRequestDto dto, User user) {
    Device device = new Device();
    device.setName(dto.getName());
    device.setMqttPrefix(dto.getMqttPrefix());
    device.setDeviceType(dto.getDeviceType());
    device.setPriority(dto.getPriority());
    device.setWattage(dto.getWattage());
    device.setUser(user);
    return device;
  }

  public void updateEntityFromDto(Device device, DeviceRequestDto dto) {
    device.setName(dto.getName());
    device.setMqttPrefix(dto.getMqttPrefix());
    device.setDeviceType(dto.getDeviceType());
    device.setPriority(dto.getPriority());
    device.setWattage(dto.getWattage());
  }

  public DeviceResponseDto toResponseDTO(Device device) {
    DeviceResponseDto dto = new DeviceResponseDto();
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

  public List<DeviceResponseDto> toResponseDTOList(List<Device> devices) {
    return devices.stream().map(this::toResponseDTO).collect(Collectors.toList());
  }
}
