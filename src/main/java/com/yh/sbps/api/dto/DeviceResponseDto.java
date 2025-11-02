package com.yh.sbps.api.dto;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DeviceResponseDto {
  private Long id;
  private String name;
  private String mqttPrefix;
  private DeviceType deviceType;
  private Integer priority;
  private Integer wattage;
  private boolean preventDowntime;
  private Integer maxDowntimeMinutes;
  private Integer minUptimeMinutes;
  private String username;

  public static DeviceResponseDto from(Device device) {
    DeviceResponseDto dto = new DeviceResponseDto();
    dto.setId(device.getId());
    dto.setName(device.getName());
    dto.setMqttPrefix(device.getMqttPrefix());
    dto.setDeviceType(device.getDeviceType());
    dto.setPriority(device.getPriority());
    dto.setWattage(device.getWattage());
    dto.setPreventDowntime(device.isPreventDowntime());
    dto.setMaxDowntimeMinutes(device.getMaxDowntimeMinutes());
    dto.setMinUptimeMinutes(device.getMinUptimeMinutes());

    if (device.getUser() != null) {
      dto.setUsername(device.getUser().getUsername());
    }
    return dto;
  }
}
