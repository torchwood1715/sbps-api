package com.yh.sbps.api.dto;

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
  private String username;
}
