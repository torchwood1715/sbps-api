package com.yh.sbps.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "system_settings")
public class SystemSettings {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "power_limit_watts")
  private Integer powerLimitWatts;

  @Column(name = "power_on_margin_watts")
  private Integer powerOnMarginWatts;

  @Column(name = "overload_cooldown_seconds")
  private Integer overloadCooldownSeconds;

  @Column(name = "power_save_limit_watts")
  private Integer powerSaveLimitWatts;

  @Column(name = "is_vacation_mode_enabled", nullable = false)
  private boolean isVacationModeEnabled = false;

  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  public SystemSettings(
      Integer powerLimitWatts,
      Integer powerOnMarginWatts,
      Integer overloadCooldownSeconds,
      Integer powerSaveLimitWatts,
      User user) {
    this.powerLimitWatts = powerLimitWatts;
    this.powerOnMarginWatts = powerOnMarginWatts;
    this.overloadCooldownSeconds = overloadCooldownSeconds;
    this.powerSaveLimitWatts = powerSaveLimitWatts;
    this.isVacationModeEnabled = false;
    this.user = user;
  }
}
