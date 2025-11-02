package com.yh.sbps.api.dto;

import com.yh.sbps.api.entity.SystemSettings;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SystemSettingsDto {
  @PositiveOrZero private Integer powerLimitWatts;
  @PositiveOrZero private Integer powerOnMarginWatts;
  @PositiveOrZero private Integer overloadCooldownSeconds;

  public static SystemSettingsDto fromEntity(SystemSettings systemSettings) {
    SystemSettingsDto dto = new SystemSettingsDto();
    dto.setPowerLimitWatts(systemSettings.getPowerLimitWatts());
    dto.setPowerOnMarginWatts(systemSettings.getPowerOnMarginWatts());
    dto.setOverloadCooldownSeconds(systemSettings.getOverloadCooldownSeconds());
    return dto;
  }

  public static void toEntity(SystemSettings systemSettings, SystemSettingsDto dto) {
    systemSettings.setPowerLimitWatts(dto.getPowerLimitWatts());
    systemSettings.setPowerOnMarginWatts(dto.getPowerOnMarginWatts());
    systemSettings.setOverloadCooldownSeconds(dto.getOverloadCooldownSeconds());
  }
}
