package com.yh.sbps.api.repository;

import com.yh.sbps.api.entity.PushSubscription;
import com.yh.sbps.api.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
  List<PushSubscription> findAllByUser(User user);

  void deleteByEndpoint(String endpoint);
}
