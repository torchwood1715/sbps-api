package com.yh.sbps.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yh.sbps.api.dto.BalancerActionDto;
import com.yh.sbps.api.dto.PushSubscriptionDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.PushSubscription;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.DeviceRepository;
import com.yh.sbps.api.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushNotificationService {

  private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
  private final PushSubscriptionRepository subscriptionRepository;
  private final DeviceRepository deviceRepository;
  private final ObjectMapper objectMapper;

  @Value("${webpush.vapid.public-key}")
  private String vapidPublicKey;

  @Value("${webpush.vapid.private-key}")
  private String vapidPrivateKey;

  @Value("${webpush.vapid.subject}")
  private String vapidSubject;

  private PushService pushService;

  @Autowired
  public PushNotificationService(
      PushSubscriptionRepository subscriptionRepository,
      DeviceRepository deviceRepository,
      ObjectMapper objectMapper) {
    this.subscriptionRepository = subscriptionRepository;
    this.deviceRepository = deviceRepository;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  private void init() {
    Security.addProvider(new BouncyCastleProvider());
    try {
      pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
      logger.info("PushService initialized successfully.");
    } catch (Exception e) {
      logger.error("Failed to initialize PushService. VAPID keys might be missing.", e);
    }
  }

  @Transactional
  public void subscribe(User user, PushSubscriptionDto subDto) {
    logger.info("User {} subscribing to push notifications.", user.getUsername());
    subscriptionRepository.deleteByEndpoint(subDto.getEndpoint());

    PushSubscription subscription =
        new PushSubscription(
            user, subDto.getEndpoint(), subDto.getKeys().getP256dh(), subDto.getKeys().getAuth());
    subscriptionRepository.save(subscription);
  }

  @Transactional
  public void unsubscribe(String endpoint) {
    logger.info("Unsubscribing endpoint: {}", endpoint);
    subscriptionRepository.deleteByEndpoint(endpoint);
  }

  @Transactional(readOnly = true)
  public void notifyUserOfBalancerAction(BalancerActionDto actionDto) {
    Optional<Device> deviceOpt = deviceRepository.findById(actionDto.getDeviceId());
    if (deviceOpt.isEmpty() || deviceOpt.get().getUser() == null) {
      logger.warn(
          "Cannot send push notification. Device {} or its user not found.",
          actionDto.getDeviceId());
      return;
    }

    User user = deviceOpt.get().getUser();
    String title = "Smart Power Balancer";
    String body;
    String url = "/dashboard";

    if ("DISABLED_BY_BALANCER".equals(actionDto.getAction())) {
      body =
          String.format("Overload detected! Balancer turned off '%s'.", actionDto.getDeviceName());
    } else if ("ENABLED_BY_BALANCER".equals(actionDto.getAction())) {
      body = String.format("Power restored. Balancer turned on '%s'.", actionDto.getDeviceName());
    } else {
      logger.warn("Unknown balancer action: {}", actionDto.getAction());
      return;
    }

    try {
      Map<String, String> payloadMap =
          Map.of(
              "title", title,
              "body", body,
              "url", url);
      String payload = objectMapper.writeValueAsString(payloadMap);

      sendNotificationToUser(user, payload);
    } catch (Exception e) {
      logger.error("Failed to serialize push payload", e);
    }
  }

  @Transactional(readOnly = true)
  public void sendNotificationToUser(User user, String payload) {
    if (pushService == null) {
      logger.warn("PushService not initialized. Cannot send notification.");
      return;
    }

    List<PushSubscription> subscriptions = subscriptionRepository.findAllByUser(user);
    if (subscriptions.isEmpty()) {
      logger.debug("User {} has no push subscriptions.", user.getUsername());
      return;
    }

    logger.info(
        "Sending push notification to {} subscriptions for user {}",
        subscriptions.size(),
        user.getUsername());

    for (PushSubscription sub : subscriptions) {
      try {
        Notification notification =
            new Notification(sub.getEndpoint(), sub.getP256dhKey(), sub.getAuthKey(), payload);
        pushService.send(notification);
      } catch (Exception e) {
        logger.error(
            "Failed to send push notification to {}: {}", sub.getEndpoint(), e.getMessage());
        // TODO
        // if (e instanceof OrgApacheHttpclientStatusLineException &&
        // ((OrgApacheHttpclientStatusLineException) e).getStatusCode() == 410) {
        //   subscriptionRepository.delete(sub);
        // }
      }
    }
  }
}
