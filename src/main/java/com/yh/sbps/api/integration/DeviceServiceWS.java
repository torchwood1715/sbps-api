package com.yh.sbps.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yh.sbps.api.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DeviceServiceWS {

  private static final Logger logger = LoggerFactory.getLogger(DeviceServiceWS.class);
  private final WebClient webClient;
  private final String deviceServiceUrl;
  private final ObjectMapper objectMapper;

  public DeviceServiceWS(WebClient webClient, @Value("${device.url}") String deviceServiceUrl) {
    this.webClient = webClient;
    this.deviceServiceUrl = deviceServiceUrl;
    this.objectMapper = new ObjectMapper();
  }

  public void notifyDeviceUpdate(Device device) {
    String url = deviceServiceUrl + "/api/device/internal/subscribe";
    logger.info("Notifying device-service about update for device: {}", device.getName());
    webClient
        .post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(device)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(
            error ->
                logger.error(
                    "Failed to notify device-service about device: {}", device.getName(), error))
        .subscribe();
  }

  public ResponseEntity<String> togglePlug(Long deviceId, boolean on) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/toggle?on=" + on;
    logger.info("Attempting to call device-service POST toggle at: {}", url);
    try {
      String result = webClient.post().uri(url).retrieve().bodyToMono(String.class).block();
      return ResponseEntity.ok(result);
    } catch (WebClientResponseException e) {
      logger.error(
          "Error calling device-service toggle for ID {}: Status {}, Body {}",
          deviceId,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    } catch (Exception e) {
      logger.error("Unexpected error calling device-service toggle for ID {}", deviceId, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error calling device service", e);
    }
  }

  public ResponseEntity<JsonNode> getStatus(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/status";
    logger.info("Attempting to call device-service GET status at: {}", url);
    try {
      JsonNode result = webClient.get().uri(url).retrieve().bodyToMono(JsonNode.class).block();
      return ResponseEntity.ok(result);
    } catch (WebClientResponseException e) {
      logger.error(
          "Error calling device-service status for ID {}: Status {}, Body {}",
          deviceId,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      return ResponseEntity.status(e.getStatusCode()).body(convertErrorBodyToJsonNode(e));
    } catch (Exception e) {
      logger.error("Unexpected error calling device-service status for ID {}", deviceId, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error calling device service", e);
    }
  }

  public ResponseEntity<Boolean> getOnline(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/online";
    logger.info("Attempting to call device-service GET online at: {}", url);
    try {
      Boolean result = webClient.get().uri(url).retrieve().bodyToMono(Boolean.class).block();
      return ResponseEntity.ok(result);
    } catch (WebClientResponseException e) {
      logger.error(
          "Error calling device-service online for ID {}: Status {}, Body {}",
          deviceId,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      return ResponseEntity.status(e.getStatusCode()).build();
    } catch (Exception e) {
      logger.error("Unexpected error calling device-service online for ID {}", deviceId, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error calling device service", e);
    }
  }

  public ResponseEntity<JsonNode> getEvents(Long deviceId) {
    String url = deviceServiceUrl + "/api/device/plug/" + deviceId + "/events";
    logger.info("Attempting to call device-service GET events at: {}", url);
    try {
      JsonNode result = webClient.get().uri(url).retrieve().bodyToMono(JsonNode.class).block();
      return ResponseEntity.ok(result);
    } catch (WebClientResponseException e) {
      logger.error(
          "Error calling device-service events for ID {}: Status {}, Body {}",
          deviceId,
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      return ResponseEntity.status(e.getStatusCode()).body(convertErrorBodyToJsonNode(e));
    } catch (Exception e) {
      logger.error("Unexpected error calling device-service events for ID {}", deviceId, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error calling device service", e);
    }
  }

  private JsonNode convertErrorBodyToJsonNode(WebClientResponseException e) {
    try {
      return objectMapper.readTree(e.getResponseBodyAsString());
    } catch (Exception parseException) {
      logger.warn("Could not parse error response body as JSON: {}", e.getResponseBodyAsString());
      ObjectNode errorNode = objectMapper.createObjectNode();
      errorNode.put("error", "Failed to retrieve data from device service");
      errorNode.put("status", e.getStatusCode().value());
      errorNode.put("message", e.getResponseBodyAsString());
      return errorNode;
    }
  }
}
