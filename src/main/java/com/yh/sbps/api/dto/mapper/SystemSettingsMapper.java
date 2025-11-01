package com.yh.sbps.api.dto.mapper;

import com.yh.sbps.api.dto.SystemSettingsDto;
import com.yh.sbps.api.entity.SystemSettings;
import org.springframework.stereotype.Component;

@Component
public class SystemSettingsMapper {

  public void updateEntityFromDto(SystemSettings systemSettings, SystemSettingsDto dto) {
    systemSettings.setPowerLimitWatts(dto.getPowerLimitWatts());
    systemSettings.setPowerOnMarginWatts(dto.getPowerOnMarginWatts());
    systemSettings.setOverloadCooldownSeconds(dto.getOverloadCooldownSeconds());
  }

  public SystemSettingsDto toDto(SystemSettings systemSettings) {
    SystemSettingsDto dto = new SystemSettingsDto();
    dto.setPowerLimitWatts(systemSettings.getPowerLimitWatts());
    dto.setPowerOnMarginWatts(systemSettings.getPowerOnMarginWatts());
    dto.setOverloadCooldownSeconds(systemSettings.getOverloadCooldownSeconds());
    return dto;
  }
}
