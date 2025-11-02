package com.yh.sbps.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "devices")
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "mqtt_prefix", nullable = false)
  private String mqttPrefix;

  @Enumerated(EnumType.STRING)
  @Column(name = "device_type", nullable = false)
  private DeviceType deviceType;

  @Column(name = "priority")
  private Integer priority = 0;

  @Column(name = "wattage")
  private Integer wattage;

  @Column(name = "prevent_downtime", nullable = false)
  private boolean preventDowntime = false;

  @Column(name = "max_downtime_minutes")
  private Integer maxDowntimeMinutes;

  @Column(name = "min_uptime_minutes")
  private Integer minUptimeMinutes;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
}
