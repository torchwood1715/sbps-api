package com.yh.sbps.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.service.DeviceService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("DeviceControlController Integration Tests")
class DeviceControlControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private DeviceService deviceService;
  @Autowired private DeviceServiceWS deviceServiceWS;
  private User testUser;
  private Device testDevice;

  @BeforeEach
  void setUp() {
    testUser = new User("test@example.com", "test", "password", Role.USER);
    testUser.setId(1L);

    testDevice = new Device();
    testDevice.setId(1L);
    testDevice.setName("Test Device");
    testDevice.setMqttPrefix("test/device");
    testDevice.setDeviceType(DeviceType.SWITCHABLE_APPLIANCE);
    testDevice.setPriority(1);
    testDevice.setWattage(1000);
    testDevice.setUser(testUser);
  }

  @Test
  @DisplayName("Should toggle plug successfully when user owns device")
  void togglePlug_UserOwnsDevice_ReturnsSuccess() throws Exception {
    // Arrange
    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.togglePlug(1L, true))
        .thenReturn(ResponseEntity.ok("Device toggled successfully"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/control/plug/1/toggle")
                .param("on", "true")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("Device toggled successfully"));
  }

  @Test
  @DisplayName("Should return 403 when user doesn't own device")
  void togglePlug_UserDoesNotOwnDevice_ReturnsForbidden() throws Exception {
    // Arrange
    User otherUser = new User("other@example.com", "other", "password", Role.USER);
    otherUser.setId(2L);

    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/control/plug/1/toggle")
                .param("on", "true")
                .with(user(otherUser))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Access denied"));
  }

  @Test
  @DisplayName("Should return 403 when device not found")
  void togglePlug_DeviceNotFound_ReturnsForbidden() throws Exception {
    // Arrange
    when(deviceService.getDeviceById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc
        .perform(
            post("/api/control/plug/999/toggle")
                .param("on", "true")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Access denied"));
  }

  @Test
  @DisplayName("Should get device status successfully")
  void getStatus_UserOwnsDevice_ReturnsStatus() throws Exception {
    // Arrange
    JsonNode statusJson =
        objectMapper.readTree("{\"power\": 100, \"voltage\": 220, \"current\": 0.45}");

    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.getStatus(1L)).thenReturn(ResponseEntity.ok(statusJson));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/status").with(user(testUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.power").value(100))
        .andExpect(jsonPath("$.voltage").value(220))
        .andExpect(jsonPath("$.current").value(0.45));
  }

  @Test
  @DisplayName("Should handle device service error for status")
  void getStatus_DeviceServiceError_ReturnsError() throws Exception {
    // Arrange
    JsonNode errorJson = objectMapper.readTree("{\"error\": \"Device offline\"}");

    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.getStatus(1L))
        .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorJson));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/status").with(user(testUser)))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("Device offline"));
  }

  @Test
  @DisplayName("Should get online status successfully")
  void getOnline_UserOwnsDevice_ReturnsOnlineStatus() throws Exception {
    // Arrange
    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.getOnline(1L)).thenReturn(ResponseEntity.ok(true));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/online").with(user(testUser)))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  @DisplayName("Should return 403 for online status when user doesn't own device")
  void getOnline_UserDoesNotOwnDevice_ReturnsForbidden() throws Exception {
    // Arrange
    User otherUser = new User("other@example.com", "other", "password", Role.USER);
    otherUser.setId(2L);

    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/online").with(user(otherUser)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Access denied"));
  }

  @Test
  @DisplayName("Should get events successfully")
  void getEvents_UserOwnsDevice_ReturnsEvents() throws Exception {
    // Arrange
    JsonNode eventsJson =
        objectMapper.readTree(
            "[{\"timestamp\": \"2024-01-01T10:00:00\", \"type\": \"power_on\"}, "
                + "{\"timestamp\": \"2024-01-01T11:00:00\", \"type\": \"power_off\"}]");

    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.getEvents(1L)).thenReturn(ResponseEntity.ok(eventsJson));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/events").with(user(testUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value("power_on"))
        .andExpect(jsonPath("$[1].type").value("power_off"));
  }

  @Test
  @DisplayName("Should return 403 for events when user doesn't own device")
  void getEvents_UserDoesNotOwnDevice_ReturnsForbidden() throws Exception {
    // Arrange
    User otherUser = new User("other@example.com", "other", "password", Role.USER);
    otherUser.setId(2L);

    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/events").with(user(otherUser)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Access denied"));
  }

  @Test
  @DisplayName("Should handle exception in toggle plug")
  void togglePlug_ExceptionThrown_ReturnsInternalServerError() throws Exception {
    // Arrange
    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.togglePlug(anyLong(), any(Boolean.class)))
        .thenThrow(new RuntimeException("Connection failed"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/control/plug/1/toggle")
                .param("on", "true")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Connection failed"));
  }

  @Test
  @DisplayName("Should handle exception in get status")
  void getStatus_ExceptionThrown_ReturnsInternalServerError() throws Exception {
    // Arrange
    when(deviceService.getDeviceById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceServiceWS.getStatus(anyLong()))
        .thenThrow(new RuntimeException("Service unavailable"));

    // Act & Assert
    mockMvc
        .perform(get("/api/control/plug/1/status").with(user(testUser)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Service unavailable"));
  }

  @Test
  @DisplayName("Should require authentication for all endpoints")
  void allEndpoints_NoAuthentication_ReturnsForbidden() throws Exception {
    // Act & Assert - Toggle
    mockMvc
        .perform(
            post("/api/control/plug/1/toggle")
                .param("on", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    // Act & Assert - Status
    mockMvc.perform(get("/api/control/plug/1/status")).andExpect(status().isForbidden());

    // Act & Assert - Online
    mockMvc.perform(get("/api/control/plug/1/online")).andExpect(status().isForbidden());

    // Act & Assert - Events
    mockMvc.perform(get("/api/control/plug/1/events")).andExpect(status().isForbidden());
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public DeviceService deviceService() {
      return Mockito.mock(DeviceService.class);
    }

    @Bean
    @Primary
    public DeviceServiceWS deviceServiceWS() {
      return Mockito.mock(DeviceServiceWS.class);
    }
  }
}
