package com.yh.sbps.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SystemStateDto {
  SystemSettingsDto systemSettings;
  List<DeviceResponseDto> devices;
}
