package com.yh.sbps.api.repository;

import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
  List<Device> findAllByUser(User user);

  Optional<Device> findByMqttPrefix(String mqttPrefix);
}
