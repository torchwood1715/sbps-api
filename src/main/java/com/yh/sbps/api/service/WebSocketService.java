package com.yh.sbps.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  @Autowired
  public WebSocketService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void broadcastDeviceUpdate(String username, Object deviceStatus) {
    if (username == null) return;
    String destination = "/topic/status/" + username;
    messagingTemplate.convertAndSend(destination, deviceStatus);
  }
}
