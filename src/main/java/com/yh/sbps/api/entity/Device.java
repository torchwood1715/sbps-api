package com.yh.sbps.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
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

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  public Device() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMqttPrefix() {
    return mqttPrefix;
  }

  public void setMqttPrefix(String mqttPrefix) {
    this.mqttPrefix = mqttPrefix;
  }

  public DeviceType getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(DeviceType deviceType) {
    this.deviceType = deviceType;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getWattage() {
    return wattage;
  }

  public void setWattage(Integer wattage) {
    this.wattage = wattage;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
