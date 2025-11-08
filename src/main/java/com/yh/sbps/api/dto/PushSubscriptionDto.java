package com.yh.sbps.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PushSubscriptionDto {
  private String endpoint;
  private Keys keys;

  @Setter
  @Getter
  @NoArgsConstructor
  public static class Keys {
    private String p256dh;
    private String auth;
  }
}
