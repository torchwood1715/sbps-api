package com.yh.sbps.api.dto;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.SystemSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SystemStateDto {
  SystemSettings systemSettings;
  List<Device> devices;
}
