package com.example.daily_new;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻列表适配器：使用 ViewHolder 复用 Item 布局。
 */
public class NewsAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<News> data = new ArrayList<>();

    public NewsAdapter(Context context, List<News> newsList) {
        this.inflater = LayoutInflater.from(context);
        this.data.addAll(newsList);
    }

    /** 切换频道时更新数据 */
    public void setData(List<News> newsList) {
        data.clear();
        data.addAll(newsList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public News getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_news, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.tv_title);
            holder.meta = convertView.findViewById(R.id.tv_meta);
            holder.thumb = convertView.findViewById(R.id.iv_thumb);
            holder.videoBadge = convertView.findViewById(R.id.iv_video_badge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        News news = getItem(position);
        holder.title.setText(news.getTitle());
        holder.meta.setText(news.getMetaText());
        holder.thumb.setImageResource(news.getImageResId());
        // 视频类新闻在缩略图右下角叠加播放角标
        holder.videoBadge.setVisibility(news.isVideo() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView meta;
        ImageView thumb;
        ImageView videoBadge;
    }
}
