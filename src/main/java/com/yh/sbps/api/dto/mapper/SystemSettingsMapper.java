package com.yh.sbps.api.dto.mapper;

import com.yh.sbps.api.dto.SystemSettingsDTO;
import com.yh.sbps.api.entity.SystemSettings;
import org.springframework.stereotype.Component;

@Component
public class SystemSettingsMapper {

  public void updateEntityFromDto(SystemSettings systemSettings, SystemSettingsDTO dto) {
    systemSettings.setPowerLimitWatts(dto.getPowerLimitWatts());
    systemSettings.setPowerOnMarginWatts(dto.getPowerOnMarginWatts());
    systemSettings.setOverloadCooldownSeconds(dto.getOverloadCooldownSeconds());
  }

  public SystemSettingsDTO toDto(SystemSettings systemSettings) {
    SystemSettingsDTO dto = new SystemSettingsDTO();
    dto.setPowerLimitWatts(systemSettings.getPowerLimitWatts());
    dto.setPowerOnMarginWatts(systemSettings.getPowerOnMarginWatts());
    dto.setOverloadCooldownSeconds(systemSettings.getOverloadCooldownSeconds());
    return dto;
  }
}
