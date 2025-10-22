package com.yh.sbps.api.controller;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.service.DeviceService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
public class DeviceControlController {

  private final DeviceService deviceService;
  private final DeviceServiceWS deviceServiceWS;

  public DeviceControlController(DeviceService deviceService, DeviceServiceWS deviceServiceWS) {
    this.deviceService = deviceService;
    this.deviceServiceWS = deviceServiceWS;
  }

  @PostMapping("/plug/{deviceId}/toggle")
  public ResponseEntity<?> togglePlug(
      @AuthenticationPrincipal User user, @PathVariable Long deviceId, @RequestParam boolean on) {
    if (!canAccessDevice(user, deviceId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    try {
      return deviceServiceWS.togglePlug(deviceId, on);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/plug/{deviceId}/status")
  public ResponseEntity<?> getStatus(
      @AuthenticationPrincipal User user, @PathVariable Long deviceId) {
    if (!canAccessDevice(user, deviceId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    try {
      return deviceServiceWS.getStatus(deviceId);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/plug/{deviceId}/online")
  public ResponseEntity<?> getOnline(
      @AuthenticationPrincipal User user, @PathVariable Long deviceId) {
    if (!canAccessDevice(user, deviceId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    try {
      return deviceServiceWS.getOnline(deviceId);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/plug/{deviceId}/events")
  public ResponseEntity<?> getEvents(
      @AuthenticationPrincipal User user, @PathVariable Long deviceId) {
    if (!canAccessDevice(user, deviceId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    try {
      return deviceServiceWS.getEvents(deviceId);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  private boolean canAccessDevice(User user, Long deviceId) {
    Optional<Device> deviceOpt = deviceService.getDeviceById(deviceId);
    if (deviceOpt.isEmpty()) {
      return false;
    }
    Device device = deviceOpt.get();
    return device.getUser() != null && device.getUser().getId().equals(user.getId());
  }
}
