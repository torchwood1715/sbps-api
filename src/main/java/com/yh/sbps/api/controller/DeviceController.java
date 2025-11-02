package com.yh.sbps.api.controller;

import com.yh.sbps.api.dto.DeviceRequestDto;
import com.yh.sbps.api.dto.DeviceResponseDto;
import com.yh.sbps.api.dto.SystemStateDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.service.DeviceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

  private final DeviceService deviceService;

  @Autowired
  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @GetMapping
  public ResponseEntity<List<DeviceResponseDto>> getAllDevices(@AuthenticationPrincipal User user) {
    List<Device> devices = deviceService.getAllDevices(user);
    return ResponseEntity.ok(devices.stream().map(DeviceResponseDto::from).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<DeviceResponseDto> getDeviceById(@PathVariable Long id) {
    Optional<Device> device = deviceService.getDeviceById(id);
    return device
        .map(d -> ResponseEntity.ok(DeviceResponseDto.from(d)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<DeviceResponseDto> createDevice(
      @Valid @RequestBody DeviceRequestDto device, @AuthenticationPrincipal User user) {
    Device savedDevice = deviceService.saveDevice(device, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(DeviceResponseDto.from(savedDevice));
  }

  @PutMapping("/{id}")
  public ResponseEntity<DeviceResponseDto> updateDevice(
      @PathVariable Long id,
      @Valid @RequestBody DeviceRequestDto deviceDetails,
      @AuthenticationPrincipal User user) {
    Device updatedDevice = deviceService.updateDevice(id, deviceDetails, user);
    return ResponseEntity.ok(DeviceResponseDto.from(updatedDevice));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDevice(
      @PathVariable Long id, @AuthenticationPrincipal User user) {
    deviceService.deleteDevice(id, user);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/by-mqtt-prefix/{mqttPrefix}")
  public ResponseEntity<SystemStateDto> getSystemStateByMqttPrefix(
      @PathVariable String mqttPrefix) {
    SystemStateDto state = deviceService.getSystemStateByMqttPrefix(mqttPrefix);
    return ResponseEntity.ok(state);
  }
}
