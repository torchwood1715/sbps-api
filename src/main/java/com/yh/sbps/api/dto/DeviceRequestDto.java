package com.yh.sbps.api.dto;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DeviceRequestDto {

  @NotBlank(message = "Device name is required")
  private String name;

  @NotBlank(message = "MQTT prefix is required")
  private String mqttPrefix;

  @NotNull(message = "Device type is required")
  private DeviceType deviceType;

  @PositiveOrZero private Integer priority = 0;

  @PositiveOrZero private Integer wattage;

  private boolean preventDowntime;

  @PositiveOrZero private Integer maxDowntimeMinutes;

  @PositiveOrZero private Integer minUptimeMinutes;

  public static Device toEntity(Device device, DeviceRequestDto dto) {
    if (device == null) {
        device = new Device();
    }
    device.setName(dto.getName());
    device.setMqttPrefix(dto.getMqttPrefix());
    device.setDeviceType(dto.getDeviceType());
    device.setPriority(dto.getPriority());
    device.setWattage(dto.getWattage());
    device.setPreventDowntime(dto.isPreventDowntime());
    device.setMaxDowntimeMinutes(dto.getMaxDowntimeMinutes());
    device.setMinUptimeMinutes(dto.getMinUptimeMinutes());
    return device;
  }
}
