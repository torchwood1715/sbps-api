package com.yh.sbps.api.service;

import com.yh.sbps.api.entity.SystemSettings;
import com.yh.sbps.api.entity.User;
import com.yh.sbps.api.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

    // Default values
    private static final Integer DEFAULT_POWER_LIMIT_WATTS = 3500;
    private static final Integer DEFAULT_POWER_ON_MARGIN_WATTS = 500;
    private static final Integer DEFAULT_OVERLOAD_COOLDOWN_SECONDS = 30;
    private final SystemSettingsRepository systemSettingsRepository;

    @Autowired
    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
        this.systemSettingsRepository = systemSettingsRepository;
    }

    public SystemSettings getSettings(User user) {
        return systemSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));
    }

    public SystemSettings updateSettings(User user, SystemSettings newSettings) {
        SystemSettings existingSettings = systemSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        existingSettings.setPowerLimitWatts(newSettings.getPowerLimitWatts());
        existingSettings.setPowerOnMarginWatts(newSettings.getPowerOnMarginWatts());
        existingSettings.setOverloadCooldownSeconds(newSettings.getOverloadCooldownSeconds());

        return systemSettingsRepository.save(existingSettings);
    }

    private SystemSettings createDefaultSettings(User user) {
        SystemSettings defaultSettings = new SystemSettings(
                DEFAULT_POWER_LIMIT_WATTS,
                DEFAULT_POWER_ON_MARGIN_WATTS,
                DEFAULT_OVERLOAD_COOLDOWN_SECONDS,
                user
        );
        return systemSettingsRepository.save(defaultSettings);
    }
}