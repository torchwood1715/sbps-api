package com.yh.sbps.api.dto;

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
}
