/*
 * Copyright (C) 2021-2024 The LineageOS Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing quick settings configuration and icon order
 */
public class QuickSettingsConfigService extends Service {

    private static final String PREF_KEY_CUSTOM_ORDER = "quick_settings_custom_order";
    private static final String PREF_KEY_ICON_ENABLED = "quick_settings_icon_enabled_";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "ACTION_LOAD_ICON_ORDER":
                        loadIconOrder();
                        break;
                    case "ACTION_SAVE_ICON_ORDER":
                        saveIconOrder(intent.getStringArrayListExtra("icon_order"));
                        break;
                    case "ACTION_RESET_TO_DEFAULT":
                        resetToDefault();
                        break;
                    case "ACTION_GET_AVAILABLE_ICONS":
                        getAvailableIcons();
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void loadIconOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String customOrder = prefs.getString(PREF_KEY_CUSTOM_ORDER, null);
        
        if (customOrder != null && !customOrder.isEmpty()) {
            // Use custom order
            String[] iconIds = customOrder.split(",");
            // Notify system that icon order has changed
            sendBroadcast(new Intent("com.android.systemui.action.QUICK_SETTINGS_ORDER_CHANGED")
                    .putExtra("icon_order", Arrays.asList(iconIds)));
        } else {
            // Use default order
            String[] defaultOrder = getResources().getStringArray(R.array.config_quick_settings_default_order);
            sendBroadcast(new Intent("com.android.systemui.action.QUICK_SETTINGS_ORDER_CHANGED")
                    .putExtra("icon_order", Arrays.asList(defaultOrder)));
        }
    }

    private void saveIconOrder(List<String> iconOrder) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        StringBuilder order = new StringBuilder();
        
        for (int i = 0; i < iconOrder.size(); i++) {
            if (i > 0) order.append(",");
            order.append(iconOrder.get(i));
        }
        
        prefs.edit()
                .putString(PREF_KEY_CUSTOM_ORDER, order.toString())
                .apply();
        
        // Notify system that icon order has changed
        sendBroadcast(new Intent("com.android.systemui.action.QUICK_SETTINGS_ORDER_CHANGED")
                .putExtra("icon_order", iconOrder));
    }

    private void resetToDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .remove(PREF_KEY_CUSTOM_ORDER)
                .apply();
        
        // Load and broadcast default order
        String[] defaultOrder = getResources().getStringArray(R.array.config_quick_settings_default_order);
        sendBroadcast(new Intent("com.android.systemui.action.QUICK_SETTINGS_ORDER_CHANGED")
                .putExtra("icon_order", Arrays.asList(defaultOrder)));
    }

    private void getAvailableIcons() {
        List<QuickSettingIcon> availableIcons = new ArrayList<>();
        
        // Add all available quick settings icons
        availableIcons.add(new QuickSettingIcon("wifi", "Wi-Fi", R.drawable.ic_wifi));
        availableIcons.add(new QuickSettingIcon("bluetooth", "Bluetooth", R.drawable.ic_bluetooth));
        availableIcons.add(new QuickSettingIcon("data", "Mobile Data", R.drawable.ic_data));
        availableIcons.add(new QuickSettingIcon("flashlight", "Flashlight", R.drawable.ic_flashlight));
        availableIcons.add(new QuickSettingIcon("airplane", "Airplane Mode", R.drawable.ic_airplane));
        availableIcons.add(new QuickSettingIcon("battery", "Battery Saver", R.drawable.ic_battery));
        availableIcons.add(new QuickSettingIcon("rotation", "Auto Rotate", R.drawable.ic_rotation));
        availableIcons.add(new QuickSettingIcon("do_not_disturb", "Do Not Disturb", R.drawable.ic_do_not_disturb));
        availableIcons.add(new QuickSettingIcon("screen_record", "Screen Record", R.drawable.ic_screen_record));
        availableIcons.add(new QuickSettingIcon("screenshot", "Screenshot", R.drawable.ic_screenshot));
        availableIcons.add(new QuickSettingIcon("location", "Location", R.drawable.ic_location));
        availableIcons.add(new QuickSettingIcon("cast", "Cast", R.drawable.ic_cast));
        availableIcons.add(new QuickSettingIcon("hotspot", "Hotspot", R.drawable.ic_hotspot));
        
        // Broadcast available icons
        sendBroadcast(new Intent("com.android.systemui.action.QUICK_SETTINGS_AVAILABLE_ICONS")
                .putExtra("available_icons", availableIcons.toArray(new QuickSettingIcon[0])));
    }

    /**
     * Check if an icon is enabled
     */
    public boolean isIconEnabled(String iconId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(PREF_KEY_ICON_ENABLED + iconId, true);
    }

    /**
     * Enable or disable an icon
     */
    public void setIconEnabled(String iconId, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putBoolean(PREF_KEY_ICON_ENABLED + iconId, enabled)
                .apply();
        
        // Notify system that icon visibility has changed
        sendBroadcast(new Intent("com.android.systemui.action.QUICK_SETTINGS_ICON_VISIBILITY_CHANGED")
                .putExtra("icon_id", iconId)
                .putExtra("enabled", enabled));
    }
}