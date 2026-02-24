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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for managing quick settings icon order and visibility
 */
public class QuickSettingsIconManagerActivity extends AppCompatActivity 
        implements QuickSettingsIconAdapter.OnStartDragListener {

    private RecyclerView recyclerView;
    private QuickSettingsIconAdapter adapter;
    private ItemTouchHelper itemTouchHelper;
    private Button resetButton;
    private Button saveButton;

    private List<QuickSettingIcon> availableIcons;
    private List<QuickSettingIcon> currentIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_settings_icon_manager);

        initViews();
        initIcons();
        setupRecyclerView();
        loadCurrentOrder();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_quick_settings);
        resetButton = findViewById(R.id.btn_reset_default);
        saveButton = findViewById(R.id.btn_save);

        resetButton.setOnClickListener(v -> resetToDefault());
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void initIcons() {
        availableIcons = new ArrayList<>();
        
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
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new QuickSettingsIconAdapter(this, currentIcons, this);
        recyclerView.setAdapter(adapter);

        // Setup drag and drop
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadCurrentOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String customOrder = prefs.getString("quick_settings_custom_order", null);
        
        if (customOrder != null && !customOrder.isEmpty()) {
            String[] iconIds = customOrder.split(",");
            currentIcons = new ArrayList<>();
            
            for (String iconId : iconIds) {
                for (QuickSettingIcon icon : availableIcons) {
                    if (icon.getId().equals(iconId)) {
                        currentIcons.add(icon);
                        break;
                    }
                }
            }
        } else {
            // Load default order
            loadDefaultOrder();
        }
        
        adapter.updateIconList(currentIcons);
    }

    private void loadDefaultOrder() {
        String[] defaultOrder = getResources().getStringArray(R.array.config_quick_settings_default_order);
        currentIcons = new ArrayList<>();
        
        for (String iconId : defaultOrder) {
            for (QuickSettingIcon icon : availableIcons) {
                if (icon.getId().equals(iconId)) {
                    currentIcons.add(icon);
                    break;
                }
            }
        }
    }

    private void resetToDefault() {
        loadDefaultOrder();
        adapter.updateIconList(currentIcons);
        saveIconOrder();
    }

    private void saveChanges() {
        saveIconOrder();
        finish();
    }

    private void saveIconOrder() {
        StringBuilder order = new StringBuilder();
        for (int i = 0; i < currentIcons.size(); i++) {
            if (i > 0) order.append(",");
            order.append(currentIcons.get(i).getId());
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("quick_settings_custom_order", order.toString())
                .apply();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    /**
     * Simple callback for handling drag and drop operations
     */
    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
        
        private final QuickSettingsIconAdapter mAdapter;

        public SimpleItemTouchHelperCallback(QuickSettingsIconAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                             @NonNull RecyclerView.ViewHolder target) {
            return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }
}