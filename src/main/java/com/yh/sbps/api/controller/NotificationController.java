package com.yh.sbps.api.controller;

import com.yh.sbps.api.dto.PushSubscriptionDto;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.service.PushNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final PushNotificationService pushNotificationService;

  public NotificationController(PushNotificationService pushNotificationService) {
    this.pushNotificationService = pushNotificationService;
  }

  @PostMapping("/subscribe")
  public ResponseEntity<Void> subscribe(
      @RequestBody PushSubscriptionDto subscription, @AuthenticationPrincipal User user) {
    pushNotificationService.subscribe(user, subscription);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/unsubscribe")
  public ResponseEntity<Void> unsubscribe(@RequestBody PushSubscriptionDto subscription) {
    pushNotificationService.unsubscribe(subscription.getEndpoint());
    return ResponseEntity.ok().build();
  }
}
