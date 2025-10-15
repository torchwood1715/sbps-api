package com.yh.sbps.api.dto;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.SystemSettings;
import java.util.List;

public class SystemStateDto {
  SystemSettings systemSettings;
  List<Device> devices;

  public SystemStateDto() {}

  public SystemStateDto(SystemSettings systemSettings, List<Device> devices) {
    this.systemSettings = systemSettings;
    this.devices = devices;
  }

  public SystemSettings getSystemSettings() {
    return systemSettings;
  }

  public void setSystemSettings(SystemSettings systemSettings) {
    this.systemSettings = systemSettings;
  }

  public List<Device> getDevices() {
    return devices;
  }

  public void setDevices(List<Device> devices) {
    this.devices = devices;
  }
}
