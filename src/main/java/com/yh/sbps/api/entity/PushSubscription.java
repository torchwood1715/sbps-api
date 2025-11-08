package com.yh.sbps.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "endpoint", nullable = false, unique = true, length = 1024)
  private String endpoint;

  @Column(name = "p256dh_key", nullable = false)
  private String p256dhKey;

  @Column(name = "auth_key", nullable = false)
  private String authKey;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public PushSubscription(User user, String endpoint, String p256dhKey, String authKey) {
    this.user = user;
    this.endpoint = endpoint;
    this.p256dhKey = p256dhKey;
    this.authKey = authKey;
  }
}
