package com.yh.sbps.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.yh.sbps.api.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DeviceServiceWS {

  private static final Logger logger = LoggerFactory.getLogger(DeviceServiceWS.class);
  private final RestTemplate restTemplate;
  private final String deviceServiceUrl;

  public DeviceServiceWS(
      RestTemplate restTemplate, @Value("${device.url}") String deviceServiceUrl) {
    this.restTemplate = restTemplate;
    this.deviceServiceUrl = deviceServiceUrl;
  }

  public void notifyDeviceUpdate(Device device) {
    String url = deviceServiceUrl + "/api/device/internal/subscribe";
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<Device> request = new HttpEntity<>(device, headers);

      restTemplate.postForEntity(url, request, Void.class);
      logger.info("Notified device-service about device update: {}", device.getName());
    } catch (Exception e) {
      logger.error("Failed to notify device-service about device: {}", device.getName(), e);
    }
  }

  public ResponseEntity<String> togglePlug(Long deviceId, boolean on) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/toggle?on=" + on;
    return restTemplate.exchange(url, HttpMethod.POST, null, String.class);
  }

  public ResponseEntity<JsonNode> getStatus(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/status";
    return restTemplate.exchange(url, HttpMethod.POST, null, JsonNode.class);
  }

  public ResponseEntity<Boolean> getOnline(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/online";
    return restTemplate.exchange(url, HttpMethod.POST, null, Boolean.class);
  }

  public ResponseEntity<JsonNode> getEvents(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/events";
    return restTemplate.exchange(url, HttpMethod.POST, null, JsonNode.class);
  }
}
