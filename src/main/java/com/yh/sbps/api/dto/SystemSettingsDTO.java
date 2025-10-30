package com.yh.sbps.api.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SystemSettingsDTO {
  @PositiveOrZero private Integer powerLimitWatts;
  @PositiveOrZero private Integer powerOnMarginWatts;
  @PositiveOrZero private Integer overloadCooldownSeconds;
}
