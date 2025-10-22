package com.yh.sbps.api.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("DeviceServiceWS Unit Tests")
class DeviceServiceWSTest {

  private MockWebServer mockWebServer;
  private DeviceServiceWS deviceServiceWS;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    String baseUrl = mockWebServer.url("/").toString();
    WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

    deviceServiceWS = new DeviceServiceWS(webClient, baseUrl);
    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  @DisplayName("Should notify device update successfully")
  void notifyDeviceUpdate_ValidDevice_SendsRequest() throws InterruptedException {
    // Arrange
    Device device = new Device();
    device.setId(1L);
    device.setName("Test Device");
    device.setMqttPrefix("test/device");
    device.setDeviceType(DeviceType.SWITCHABLE_APPLIANCE);

    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));

    // Act
    deviceServiceWS.notifyDeviceUpdate(device);

    // Wait a bit for async operation
    Thread.sleep(100);

    // Assert
    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getPath().contains("/api/device/internal/subscribe"));
    assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader("Content-Type"));
  }

  @Test
  @DisplayName("Should toggle plug successfully")
  void togglePlug_ValidRequest_ReturnsSuccess() throws InterruptedException {
    // Arrange
    Long deviceId = 1L;
    boolean on = true;
    String expectedResponse = "Device toggled successfully";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(expectedResponse)
            .addHeader("Content-Type", "text/plain"));

    // Act
    ResponseEntity<String> response = deviceServiceWS.togglePlug(deviceId, on);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedResponse, response.getBody());

    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getPath().contains("/api/device/plug/1/toggle?on=true"));
  }

  @Test
  @DisplayName("Should handle toggle plug error")
  void togglePlug_ServerError_ReturnsErrorResponse() {
    // Arrange
    Long deviceId = 1L;
    boolean on = true;
    String errorBody = "Internal server error";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .setBody(errorBody)
            .addHeader("Content-Type", "text/plain"));

    // Act
    ResponseEntity<String> response = deviceServiceWS.togglePlug(deviceId, on);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(errorBody, response.getBody());
  }

  @Test
  @DisplayName("Should get status successfully")
  void getStatus_ValidRequest_ReturnsJsonStatus() throws Exception {
    // Arrange
    Long deviceId = 1L;
    String jsonResponse = "{\"power\": 100, \"voltage\": 220, \"current\": 0.45}";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

    // Act
    ResponseEntity<JsonNode> response = deviceServiceWS.getStatus(deviceId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(100, response.getBody().get("power").asInt());
    assertEquals(220, response.getBody().get("voltage").asInt());

    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("GET", request.getMethod());
    assertTrue(request.getPath().contains("/api/device/plug/1/status"));
  }

  @Test
  @DisplayName("Should handle get status error")
  void getStatus_ServerError_ReturnsErrorResponse() {
    // Arrange
    Long deviceId = 1L;
    String errorBody = "{\"error\": \"Device not found\"}";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .setBody(errorBody)
            .addHeader("Content-Type", "application/json"));

    // Act
    ResponseEntity<JsonNode> response = deviceServiceWS.getStatus(deviceId);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Should get online status successfully")
  void getOnline_ValidRequest_ReturnsBoolean() throws InterruptedException {
    // Arrange
    Long deviceId = 1L;

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody("true")
            .addHeader("Content-Type", "application/json"));

    // Act
    ResponseEntity<Boolean> response = deviceServiceWS.getOnline(deviceId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody());

    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("GET", request.getMethod());
    assertTrue(request.getPath().contains("/api/device/plug/1/online"));
  }

  @Test
  @DisplayName("Should handle get online error")
  void getOnline_ServerError_ReturnsErrorStatus() {
    // Arrange
    Long deviceId = 1L;

    mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal error"));

    // Act
    ResponseEntity<Boolean> response = deviceServiceWS.getOnline(deviceId);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  @DisplayName("Should get events successfully")
  void getEvents_ValidRequest_ReturnsJsonEvents() throws Exception {
    // Arrange
    Long deviceId = 1L;
    String jsonResponse =
        "[{\"timestamp\": \"2024-01-01T10:00:00\", \"type\": \"power_on\"}, "
            + "{\"timestamp\": \"2024-01-01T11:00:00\", \"type\": \"power_off\"}]";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

    // Act
    ResponseEntity<JsonNode> response = deviceServiceWS.getEvents(deviceId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isArray());
    assertEquals(2, response.getBody().size());

    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("GET", request.getMethod());
    assertTrue(request.getPath().contains("/api/device/plug/1/events"));
  }

  @Test
  @DisplayName("Should handle get events error")
  void getEvents_ServerError_ReturnsErrorResponse() {
    // Arrange
    Long deviceId = 1L;
    String errorBody = "{\"error\": \"Failed to retrieve events\"}";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .setBody(errorBody)
            .addHeader("Content-Type", "application/json"));

    // Act
    ResponseEntity<JsonNode> response = deviceServiceWS.getEvents(deviceId);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Should handle non-JSON error response gracefully")
  void getStatus_NonJsonError_ReturnsFormattedError() {
    // Arrange
    Long deviceId = 1L;
    String errorBody = "Plain text error message";

    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(503)
            .setBody(errorBody)
            .addHeader("Content-Type", "text/plain"));

    // Act
    ResponseEntity<JsonNode> response = deviceServiceWS.getStatus(deviceId);

    // Assert
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().has("error"));
    assertTrue(response.getBody().has("status"));
    assertTrue(response.getBody().has("message"));
    assertEquals(503, response.getBody().get("status").asInt());
  }

  @Test
  @DisplayName("Should handle connection timeout")
  void togglePlug_ConnectionTimeout_ThrowsException() throws IOException {
    // Arrange
    mockWebServer.shutdown();

    // Act & Assert
    assertThrows(ResponseStatusException.class, () -> deviceServiceWS.togglePlug(1L, true));
  }
}
