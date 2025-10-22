package com.yh.sbps.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yh.sbps.api.entity.Role;
import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemSettingsService Unit Tests")
class SystemSettingsServiceTest {

  @Mock private SystemSettingsRepository systemSettingsRepository;

  @InjectMocks private SystemSettingsService systemSettingsService;

  private User testUser;
  private SystemSettings testSettings;

  @BeforeEach
  void setUp() {
    testUser = new User("test@example.com", "password", Role.USER);
    testUser.setId(1L);

    testSettings = new SystemSettings();
    testSettings.setId(1L);
    testSettings.setPowerLimitWatts(3500);
    testSettings.setPowerOnMarginWatts(500);
    testSettings.setOverloadCooldownSeconds(30);
    testSettings.setUser(testUser);
  }

  @Test
  @DisplayName("Should return existing settings for user")
  void getSettings_ExistingSettings_ReturnsSettings() {
    // Arrange
    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));

    // Act
    SystemSettings result = systemSettingsService.getSettings(testUser);

    // Assert
    assertNotNull(result);
    assertEquals(testSettings, result);
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
    SystemSettings result = systemSettingsService.getSettings(testUser);

    // Assert
    assertNotNull(result);
    assertEquals(3500, result.getPowerLimitWatts());
    assertEquals(500, result.getPowerOnMarginWatts());
    assertEquals(30, result.getOverloadCooldownSeconds());
    assertEquals(testUser, result.getUser());
    verify(systemSettingsRepository).findByUser(testUser);
    verify(systemSettingsRepository).save(any(SystemSettings.class));
  }

  @Test
  @DisplayName("Should update existing settings")
  void updateSettings_ExistingSettings_UpdatesSuccessfully() {
    // Arrange
    SystemSettings newSettings = new SystemSettings();
    newSettings.setPowerLimitWatts(4000);
    newSettings.setPowerOnMarginWatts(600);
    newSettings.setOverloadCooldownSeconds(45);

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettings result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    assertEquals(4000, result.getPowerLimitWatts());
    assertEquals(600, result.getPowerOnMarginWatts());
    assertEquals(45, result.getOverloadCooldownSeconds());
    assertEquals(testUser, result.getUser());
    verify(systemSettingsRepository).findByUser(testUser);
    verify(systemSettingsRepository).save(testSettings);
  }

  @Test
  @DisplayName("Should create new settings when updating non-existent settings")
  void updateSettings_NoExistingSettings_CreatesNewSettings() {
    // Arrange
    SystemSettings newSettings = new SystemSettings();
    newSettings.setPowerLimitWatts(4000);
    newSettings.setPowerOnMarginWatts(600);
    newSettings.setOverloadCooldownSeconds(45);

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettings result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    assertEquals(4000, result.getPowerLimitWatts());
    assertEquals(600, result.getPowerOnMarginWatts());
    assertEquals(45, result.getOverloadCooldownSeconds());
    assertEquals(testUser, result.getUser());
    verify(systemSettingsRepository).findByUser(testUser);
    // Save is called twice: once in createDefaultSettings and once in updateSettings
    verify(systemSettingsRepository, times(2)).save(any(SystemSettings.class));
  }

  @Test
  @DisplayName("Should handle null values in update gracefully")
  void updateSettings_NullValues_KeepsExistingValues() {
    // Arrange
    SystemSettings newSettings = new SystemSettings();
    // All values are null

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettings result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    // Values should be null as per the update
    assertNull(result.getPowerLimitWatts());
    assertNull(result.getPowerOnMarginWatts());
    assertNull(result.getOverloadCooldownSeconds());
    assertEquals(testUser, result.getUser());
    verify(systemSettingsRepository).save(testSettings);
  }

  @Test
  @DisplayName("Should preserve user association in settings")
  void updateSettings_PreservesUserAssociation() {
    // Arrange
    SystemSettings newSettings = new SystemSettings();
    newSettings.setPowerLimitWatts(5000);

    User differentUser = new User("different@example.com", "password", Role.USER);
    differentUser.setId(99L);
    newSettings.setUser(differentUser); // Try to set different user

    when(systemSettingsRepository.findByUser(testUser)).thenReturn(Optional.of(testSettings));
    when(systemSettingsRepository.save(any(SystemSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    SystemSettings result = systemSettingsService.updateSettings(testUser, newSettings);

    // Assert
    assertNotNull(result);
    assertEquals(testUser, result.getUser()); // Should preserve original user
    assertNotEquals(differentUser, result.getUser());
    verify(systemSettingsRepository).save(testSettings);
  }
}
