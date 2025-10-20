package com.yh.sbps.api.controller;

import com.yh.sbps.api.dto.SystemStateDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.service.DeviceService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
public class DeviceController {

  private final DeviceService deviceService;

  @Autowired
  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @GetMapping
  public ResponseEntity<List<Device>> getAllDevices(@AuthenticationPrincipal User user) {
    List<Device> devices = deviceService.getAllDevices(user);
    return ResponseEntity.ok(devices);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
    Optional<Device> device = deviceService.getDeviceById(id);
    return device.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Device> createDevice(
      @RequestBody Device device, @AuthenticationPrincipal User user) {
    try {
      Device savedDevice = deviceService.saveDevice(device, user);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedDevice);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Device> updateDevice(
      @PathVariable Long id,
      @RequestBody Device deviceDetails,
      @AuthenticationPrincipal User user) {
    try {
      Device updatedDevice = deviceService.updateDevice(id, deviceDetails, user);
      return ResponseEntity.ok(updatedDevice);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDevice(
      @PathVariable Long id, @AuthenticationPrincipal User user) {
    try {
      deviceService.deleteDevice(id, user);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/by-mqtt-prefix/{mqttPrefix}")
  public ResponseEntity<SystemStateDto> getSystemStateByMqttPrefix(
      @PathVariable String mqttPrefix, @AuthenticationPrincipal User user) {
    try {
      SystemStateDto state = deviceService.getSystemStateByMqttPrefix(mqttPrefix);
      return ResponseEntity.ok(state);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (RuntimeException e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}
