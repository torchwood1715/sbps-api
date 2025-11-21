package com.yh.sbps.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlackoutStatsDto {
    private boolean isBlackout;
    private double consumedWattHours;
    private long durationSeconds;
}