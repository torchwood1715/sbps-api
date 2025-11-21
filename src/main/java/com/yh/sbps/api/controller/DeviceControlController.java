package com.yh.sbps.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yh.sbps.api.dto.BalancerActionDto;
import com.yh.sbps.api.dto.BlackoutStatsDto;
import com.yh.sbps.api.dto.DeviceStatusDto;
import com.yh.sbps.api.dto.DeviceStatusUpdateDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.service.DeviceService;
import com.yh.sbps.api.service.PushNotificationService;
import com.yh.sbps.api.service.WebSocketService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control")
public class DeviceControlController {

  private final DeviceService deviceService;
  private final DeviceServiceWS deviceServiceWS;
  private final WebSocketService webSocketService;
  private final PushNotificationService pushNotificationService;
  private final ObjectMapper objectMapper;

  public DeviceControlController(
      DeviceService deviceService,
      DeviceServiceWS deviceServiceWS,
      WebSocketService webSocketService,
      PushNotificationService pushNotificationService,
      ObjectMapper objectMapper) {
    this.deviceService = deviceService;
    this.deviceServiceWS = deviceServiceWS;
    this.webSocketService = webSocketService;
    this.pushNotificationService = pushNotificationService;
    this.objectMapper = objectMapper;
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

  @GetMapping("/plug/{deviceId}/status")
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

  @GetMapping("/plug/{deviceId}/online")
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

  @GetMapping("/plug/{deviceId}/events")
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

  @GetMapping("/all-statuses")
  public ResponseEntity<?> getAllDeviceStatuses(@AuthenticationPrincipal User user) {
    try {
      List<Long> deviceIds = deviceService.getAllDevices(user).stream().map(Device::getId).toList();

      if (deviceIds.isEmpty()) {
        return ResponseEntity.ok(Map.of());
      }

      Map<Long, DeviceStatusDto> deviceStatuses = deviceServiceWS.getAllStatuses(deviceIds);
      Map<Long, Object> responseMap =
          deviceStatuses.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey,
                      entry -> {
                        DeviceStatusDto status = entry.getValue();
                        if (status.isOnline() && status.getStatusJson() != null) {
                          return status.getStatusJson();
                        } else {
                          ObjectNode offlineNode = objectMapper.createObjectNode();
                          offlineNode.put("online", false);
                          return offlineNode;
                        }
                      }));

      return ResponseEntity.ok(responseMap);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PostMapping("/internal/device-update")
  public ResponseEntity<Void> receiveDeviceUpdate(
      @RequestBody DeviceStatusUpdateDto deviceStatusUpdate,
      @AuthenticationPrincipal User serviceUser) {

    if (serviceUser == null || serviceUser.getRole() != Role.SERVICE_USER) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    if (deviceStatusUpdate.getUsername() != null) {
      webSocketService.broadcastDeviceUpdate(deviceStatusUpdate.getUsername(), deviceStatusUpdate);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().build();
  }

  @PostMapping("/internal/balancer-action")
  public ResponseEntity<Void> receiveBalancerAction(
      @RequestBody BalancerActionDto actionDto, @AuthenticationPrincipal User serviceUser) {
    pushNotificationService.notifyUserOfBalancerAction(actionDto);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/system-stats")
  public ResponseEntity<BlackoutStatsDto> getSystemStats(@AuthenticationPrincipal User user) {
    String monitorPrefix = deviceService.getMonitorPrefixForUser(user);
    if (monitorPrefix == null) {
      return ResponseEntity.ok(new BlackoutStatsDto(false, 0, 0));
    }
    return ResponseEntity.ok(deviceServiceWS.getBlackoutStats(monitorPrefix));
  }
}
