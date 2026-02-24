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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

/**
 * Adapter for managing quick settings icons with drag and drop functionality
 */
public class QuickSettingsIconAdapter extends RecyclerView.Adapter<QuickSettingsIconAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private List<QuickSettingIcon> iconList;
    private Context context;
    private OnStartDragListener dragStartListener;

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public QuickSettingsIconAdapter(Context context, List<QuickSettingIcon> iconList,
                                   OnStartDragListener dragStartListener) {
        this.context = context;
        this.iconList = iconList;
        this.dragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_setting_icon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuickSettingIcon icon = iconList.get(position);
        
        // Set icon image
        holder.iconImage.setImageResource(icon.getIconResId());
        
        // Set icon name
        holder.iconName.setText(icon.getName());
        
        // Set drag handle click listener
        holder.dragHandle.setOnClickListener(v -> {
            if (dragStartListener != null) {
                dragStartListener.onStartDrag(holder);
            }
        });
        
        // Set remove icon click listener
        holder.removeIcon.setOnClickListener(v -> {
            removeItem(position);
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(iconList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(iconList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        saveIconOrder();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        removeItem(position);
    }

    private void removeItem(int position) {
        iconList.remove(position);
        notifyItemRemoved(position);
        saveIconOrder();
    }

    private void saveIconOrder() {
        StringBuilder order = new StringBuilder();
        for (int i = 0; i < iconList.size(); i++) {
            if (i > 0) order.append(",");
            order.append(iconList.get(i).getId());
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putString("quick_settings_custom_order", order.toString())
                .apply();
    }

    public void updateIconList(List<QuickSettingIcon> newIconList) {
        this.iconList = newIconList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView dragHandle;
        ImageView iconImage;
        TextView iconName;
        ImageView statusIcon;
        ImageView removeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dragHandle = itemView.findViewById(R.id.icon_drag_handle);
            iconImage = itemView.findViewById(R.id.icon_image);
            iconName = itemView.findViewById(R.id.icon_name);
            statusIcon = itemView.findViewById(R.id.icon_status);
            removeIcon = itemView.findViewById(R.id.icon_remove);
        }
    }
}