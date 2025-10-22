package com.yh.sbps.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.yh.sbps.api.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DeviceServiceWS {

  private static final Logger logger = LoggerFactory.getLogger(DeviceServiceWS.class);
  private final RestTemplate restTemplate;
  private final WebClient webClient;
  private final String deviceServiceUrl;

  public DeviceServiceWS(
      RestTemplate restTemplate,
      WebClient webClient,
      @Value("${device.url}") String deviceServiceUrl) {
    this.restTemplate = restTemplate;
    this.webClient = webClient;
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
    logger.info("Attempting to call device-service GET online using WebClient at: {}", url);
    try {
      Boolean result = webClient.post().uri(url).retrieve().bodyToMono(Boolean.class).block();

      return ResponseEntity.ok(result);

    } catch (WebClientResponseException e) {
      logger.error(
          "Error calling device-service online for ID {} using WebClient: Status {}, Body {}",
          deviceId,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get online status from device-service", e);
    } catch (Exception e) {
      logger.error(
          "Unexpected error calling device-service online for ID {} using WebClient", deviceId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", e);
    }
  }

  public ResponseEntity<JsonNode> getEvents(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/events";
    return restTemplate.exchange(url, HttpMethod.POST, null, JsonNode.class);
  }
}
