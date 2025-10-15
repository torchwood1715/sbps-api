package com.yh.sbps.api.entity;

import jakarta.persistence.*;

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

  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  public SystemSettings() {}

  public SystemSettings(
      Integer powerLimitWatts,
      Integer powerOnMarginWatts,
      Integer overloadCooldownSeconds,
      User user) {
    this.powerLimitWatts = powerLimitWatts;
    this.powerOnMarginWatts = powerOnMarginWatts;
    this.overloadCooldownSeconds = overloadCooldownSeconds;
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getPowerLimitWatts() {
    return powerLimitWatts;
  }

  public void setPowerLimitWatts(Integer powerLimitWatts) {
    this.powerLimitWatts = powerLimitWatts;
  }

  public Integer getPowerOnMarginWatts() {
    return powerOnMarginWatts;
  }

  public void setPowerOnMarginWatts(Integer powerOnMarginWatts) {
    this.powerOnMarginWatts = powerOnMarginWatts;
  }

  public Integer getOverloadCooldownSeconds() {
    return overloadCooldownSeconds;
  }

  public void setOverloadCooldownSeconds(Integer overloadCooldownSeconds) {
    this.overloadCooldownSeconds = overloadCooldownSeconds;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
