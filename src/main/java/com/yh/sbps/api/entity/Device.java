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
  @JsonProperty("mqtt_prefix")
  private String mqttPrefix;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "priority")
  private Integer priority = 0;

  public Device() {}

  public Device(String name, String mqttPrefix, String type, Integer priority) {
    this.name = name;
    this.mqttPrefix = mqttPrefix;
    this.type = type;
    this.priority = priority;
  }

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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }
}
