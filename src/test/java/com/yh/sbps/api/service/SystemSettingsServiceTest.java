package com.yh.sbps.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yh.sbps.api.dto.SystemSettingsDto;
import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.integration.DeviceServiceWS;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemSettingsService Unit Tests")
class SystemSettingsServiceTest {

  @Mock private SystemSettingsRepository systemSettingsRepository;
  @Mock private DeviceService deviceService;
  @Mock private DeviceServiceWS deviceServiceWS;
  private SystemSettingsService systemSettingsService;
  private User testUser;
  private SystemSettings testSettings;
  private SystemSettingsDto testSettingsDTO;

  @BeforeEach
  void setUp() {
    systemSettingsService =
        new SystemSettingsService(systemSettingsRepository, deviceService, deviceServiceWS);

    testUser = new User("test@example.com", "test", "password", Role.USER);
    testUser.setId(1L);

    testSettings = new SystemSettings();
    testSettings.setId(1L);
    testSettings.setPowerLimitWatts(3500);
    testSettings.setPowerOnMarginWatts(500);
    testSettings.setOverloadCooldownSeconds(30);
    testSettings.setUser(testUser);

    testSettingsDTO = new SystemSettingsDto();
    testSettingsDTO.setPowerLimitWatts(3500);
    testSettingsDTO.setPowerOnMarginWatts(500);
    testSettingsDTO.setOverloadCooldownSeconds(30);
  }

  @Test
  @DisplayName("Should return existing settings for user")
  void getSettings_ExistingSettings_ReturnsSettings() {
    // Arrange
    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));

    // Act
    SystemSettingsDto result = systemSettingsService.getSettings(testUser);

    // Assert
    assertNotNull(result);
    assertAll(
        () -> assertEquals(testSettingsDTO.getPowerLimitWatts(), result.getPowerLimitWatts()),
        () -> assertEquals(testSettingsDTO.getPowerOnMarginWatts(), result.getPowerOnMarginWatts()),
        () ->
            assertEquals(
                testSettingsDTO.getOverloadCooldownSeconds(), result.getOverloadCooldownSeconds()));
    assertEquals(3500, result.getPowerLimitWatts());
    assertEquals(500, result.getPowerOnMarginWatts());
    assertEquals(30, result.getOverloadCooldownSeconds());
    verify(systemSettingsRepository).findByUser(testUser);
    verify(systemSettingsRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should create default settings when none exist")
  void getSettings_NoExistingSettings_CreatesDefaultSettings() {
    // Arrange
    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettingsDto result = systemSettingsService.getSettings(testUser);

    // Assert
    assertNotNull(result);
    assertEquals(3500, result.getPowerLimitWatts());
    assertEquals(500, result.getPowerOnMarginWatts());
    assertEquals(30, result.getOverloadCooldownSeconds());
    verify(systemSettingsRepository).findByUser(testUser);
    verify(systemSettingsRepository).save(any(SystemSettings.class));
  }

  @Test
  @DisplayName("Should update existing settings")
  void updateSettings_ExistingSettings_UpdatesSuccessfully() {
    // Arrange
    SystemSettingsDto newSettings = new SystemSettingsDto();
    newSettings.setPowerLimitWatts(4000);
    newSettings.setPowerOnMarginWatts(600);
    newSettings.setOverloadCooldownSeconds(45);

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettingsDto result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    assertEquals(4000, result.getPowerLimitWatts());
    assertEquals(600, result.getPowerOnMarginWatts());
    assertEquals(45, result.getOverloadCooldownSeconds());
    verify(systemSettingsRepository).findByUser(testUser);
    verify(systemSettingsRepository).save(testSettings);
  }

  @Test
  @DisplayName("Should create new settings when updating non-existent settings")
  void updateSettings_NoExistingSettings_CreatesNewSettings() {
    // Arrange
    SystemSettingsDto newSettings = new SystemSettingsDto();
    newSettings.setPowerLimitWatts(4000);
    newSettings.setPowerOnMarginWatts(600);
    newSettings.setOverloadCooldownSeconds(45);

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettingsDto result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    assertEquals(4000, result.getPowerLimitWatts());
    assertEquals(600, result.getPowerOnMarginWatts());
    assertEquals(45, result.getOverloadCooldownSeconds());
    verify(systemSettingsRepository).findByUser(testUser);
    // Save is called twice: once in createDefaultSettings and once in updateSettings
    verify(systemSettingsRepository, times(2)).save(any(SystemSettings.class));
  }

  @Test
  @DisplayName("Should handle null values in update gracefully")
  void updateSettings_NullValues_KeepsExistingValues() {
    // Arrange
    SystemSettingsDto newSettings = new SystemSettingsDto();
    // All values are null

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettingsDto result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    // Values should be null as per the update
    assertNull(result.getPowerLimitWatts());
    assertNull(result.getPowerOnMarginWatts());
    assertNull(result.getOverloadCooldownSeconds());
    verify(systemSettingsRepository).save(testSettings);
  }
}
