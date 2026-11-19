package com.example.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class IconListAdapter extends ArrayAdapter<ListItem> {

    public IconListAdapter(Context context, List<ListItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_layout, parent, false);
        }

        ListItem item = getItem(position);
        ImageView icon = convertView.findViewById(R.id.item_icon);
        TextView text = convertView.findViewById(R.id.item_text);

        if (item != null) {
            icon.setImageResource(item.getIconResId());
            text.setText(item.getText());
        }

        return convertView;
    }
}