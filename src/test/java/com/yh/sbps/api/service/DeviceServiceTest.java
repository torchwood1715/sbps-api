package com.yh.sbps.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yh.sbps.api.dto.SystemStateDto;
import com.yh.sbps.api.entity.Device;
import com.yh.sbps.api.entity.DeviceType;
import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.repository.DeviceRepository;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService Unit Tests")
class DeviceServiceTest {

  @Mock private DeviceRepository deviceRepository;

  @Mock private SystemSettingsRepository systemSettingsRepository;

  @Mock private DeviceServiceWS deviceServiceWS;

  @InjectMocks private DeviceService deviceService;

  private User testUser;
  private User serviceUser;
  private Device testDevice;
  private SystemSettings testSettings;

  @BeforeEach
  void setUp() {
    testUser = new User("test@example.com", "password", Role.USER);
    testUser.setId(1L);

    serviceUser = new User("service@example.com", "password", Role.SERVICE_USER);
    serviceUser.setId(2L);

    testDevice = new Device();
    testDevice.setId(1L);
    testDevice.setName("Test Device");
    testDevice.setMqttPrefix("test/device");
    testDevice.setDeviceType(DeviceType.SWITCHABLE_APPLIANCE);
    testDevice.setPriority(1);
    testDevice.setWattage(1000);
    testDevice.setUser(testUser);

    testSettings = new SystemSettings();
    testSettings.setId(1L);
    testSettings.setPowerLimitWatts(3500);
    testSettings.setPowerOnMarginWatts(500);
    testSettings.setOverloadCooldownSeconds(30);
    testSettings.setUser(testUser);
  }

  @Test
  @DisplayName("Should return all devices for SERVICE_USER")
  void getAllDevices_ServiceUser_ReturnsAllDevices() {
    // Arrange
    List<Device> allDevices = Arrays.asList(testDevice, new Device());
    when(deviceRepository.findAll()).thenReturn(allDevices);

    // Act
    List<Device> result = deviceService.getAllDevices(serviceUser);

    // Assert
    assertEquals(2, result.size());
    verify(deviceRepository).findAll();
    verify(deviceRepository, never()).findAllByUser(any());
  }

  @Test
  @DisplayName("Should return user's devices for regular USER")
  void getAllDevices_RegularUser_ReturnsUserDevices() {
    // Arrange
    List<Device> userDevices = Arrays.asList(testDevice);
    when(deviceRepository.findAllByUser(testUser)).thenReturn(userDevices);

    // Act
    List<Device> result = deviceService.getAllDevices(testUser);

    // Assert
    assertEquals(1, result.size());
    assertEquals(testDevice, result.get(0));
    verify(deviceRepository).findAllByUser(testUser);
    verify(deviceRepository, never()).findAll();
  }

  @Test
  @DisplayName("Should return device by ID")
  void getDeviceById_ExistingDevice_ReturnsDevice() {
    // Arrange
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

    // Act
    Optional<Device> result = deviceService.getDeviceById(1L);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(testDevice, result.get());
    verify(deviceRepository).findById(1L);
  }

  @Test
  @DisplayName("Should return empty when device not found")
  void getDeviceById_NonExistingDevice_ReturnsEmpty() {
    // Arrange
    when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

    // Act
    Optional<Device> result = deviceService.getDeviceById(999L);

    // Assert
    assertFalse(result.isPresent());
    verify(deviceRepository).findById(999L);
  }

  @Test
  @DisplayName("Should save device and notify device service")
  void saveDevice_ValidDevice_SavesAndNotifies() {
    // Arrange
    Device newDevice = new Device();
    newDevice.setName("New Device");
    newDevice.setMqttPrefix("new/device");
    newDevice.setDeviceType(DeviceType.POWER_MONITOR);

    when(deviceRepository.save(any(Device.class))).thenReturn(newDevice);
    doNothing().when(deviceServiceWS).notifyDeviceUpdate(any(Device.class));

    // Act
    Device result = deviceService.saveDevice(newDevice, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(testUser, newDevice.getUser());
    verify(deviceRepository).save(newDevice);
    verify(deviceServiceWS).notifyDeviceUpdate(newDevice);
  }

  @Test
  @DisplayName("Should update device and notify device service")
  void updateDevice_ValidDevice_UpdatesAndNotifies() {
    // Arrange
    Device updatedDetails = new Device();
    updatedDetails.setName("Updated Name");
    updatedDetails.setMqttPrefix("updated/prefix");
    updatedDetails.setDeviceType(DeviceType.POWER_MONITOR);
    updatedDetails.setPriority(2);
    updatedDetails.setWattage(2000);

    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
    when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);
    doNothing().when(deviceServiceWS).notifyDeviceUpdate(any(Device.class));

    // Act
    Device result = deviceService.updateDevice(1L, updatedDetails, testUser);

    // Assert
    assertNotNull(result);
    assertEquals("Updated Name", testDevice.getName());
    assertEquals("updated/prefix", testDevice.getMqttPrefix());
    assertEquals(DeviceType.POWER_MONITOR, testDevice.getDeviceType());
    assertEquals(2, testDevice.getPriority());
    assertEquals(2000, testDevice.getWattage());
    verify(deviceRepository).save(testDevice);
    verify(deviceServiceWS).notifyDeviceUpdate(testDevice);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent device")
  void updateDevice_NonExistentDevice_ThrowsException() {
    // Arrange
    when(deviceRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        RuntimeException.class, () -> deviceService.updateDevice(999L, testDevice, testUser));
    verify(deviceRepository, never()).save(any());
    verify(deviceServiceWS, never()).notifyDeviceUpdate(any());
  }

  @Test
  @DisplayName("Should throw exception when updating another user's device")
  void updateDevice_OtherUsersDevice_ThrowsException() {
    // Arrange
    User otherUser = new User("other@example.com", "password", Role.USER);
    otherUser.setId(99L);

    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

    // Act & Assert
    assertThrows(
        RuntimeException.class, () -> deviceService.updateDevice(1L, testDevice, otherUser));
    verify(deviceRepository, never()).save(any());
    verify(deviceServiceWS, never()).notifyDeviceUpdate(any());
  }

  @Test
  @DisplayName("Should delete device successfully")
  void deleteDevice_ValidDevice_DeletesSuccessfully() {
    // Arrange
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
    doNothing().when(deviceRepository).deleteById(1L);

    // Act
    deviceService.deleteDevice(1L, testUser);

    // Assert
    verify(deviceRepository).findById(1L);
    verify(deviceRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw exception when deleting another user's device")
  void deleteDevice_OtherUsersDevice_ThrowsException() {
    // Arrange
    User otherUser = new User("other@example.com", "password", Role.USER);
    otherUser.setId(99L);

    when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> deviceService.deleteDevice(1L, otherUser));
    verify(deviceRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should get system state by MQTT prefix successfully")
  void getSystemStateByMqttPrefix_ValidPrefix_ReturnsSystemState() {
    // Arrange
    List<Device> userDevices = Arrays.asList(testDevice);
    when(deviceRepository.findByMqttPrefix("test/device")).thenReturn(Optional.of(testDevice));
    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));
    when(deviceRepository.findAllByUser(testUser)).thenReturn(userDevices);

    // Act
    SystemStateDto result = deviceService.getSystemStateByMqttPrefix("test/device");

    // Assert
    assertNotNull(result);
    assertEquals(testSettings, result.getSystemSettings());
    assertEquals(1, result.getDevices().size());
    verify(deviceRepository).findByMqttPrefix("test/device");
    verify(systemSettingsRepository).findByUser(testUser);
    verify(deviceRepository).findAllByUser(testUser);
  }

  @Test
  @DisplayName("Should throw exception when device not found by MQTT prefix")
  void getSystemStateByMqttPrefix_InvalidPrefix_ThrowsException() {
    // Arrange
    when(deviceRepository.findByMqttPrefix("invalid/prefix")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> deviceService.getSystemStateByMqttPrefix("invalid/prefix"));
    verify(systemSettingsRepository, never()).findByUser(any());
  }

  @Test
  @DisplayName("Should throw exception when device has no user")
  void getSystemStateByMqttPrefix_DeviceWithoutUser_ThrowsException() {
    // Arrange
    testDevice.setUser(null);
    when(deviceRepository.findByMqttPrefix("test/device")).thenReturn(Optional.of(testDevice));

    // Act & Assert
    assertThrows(
        IllegalStateException.class, () -> deviceService.getSystemStateByMqttPrefix("test/device"));
    verify(systemSettingsRepository, never()).findByUser(any());
  }

  @Test
  @DisplayName("Should throw exception when system settings not found")
  void getSystemStateByMqttPrefix_NoSettings_ThrowsException() {
    // Arrange
    when(deviceRepository.findByMqttPrefix("test/device")).thenReturn(Optional.of(testDevice));
    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> deviceService.getSystemStateByMqttPrefix("test/device"));
    verify(deviceRepository, never()).findAllByUser(any());
  }
}
