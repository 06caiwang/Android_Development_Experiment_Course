package com.example.daily_new;

/**
 * 新闻实体类：只保存界面展示需要的字段，数据全部为本地死数据。
 */
public class News {

    private final String title;
    private final String source;
    private final String comment;
    private final String time;
    private final String category;
    private final String content;
    private final int imageResId;
    private final boolean video;

    public News(String title, String source, String comment, String time,
                String category, String content, int imageResId, boolean video) {
        this.title = title;
        this.source = source;
        this.comment = comment;
        this.time = time;
        this.category = category;
        this.content = content;
        this.imageResId = imageResId;
        this.video = video;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getComment() {
        return comment;
    }

    public String getTime() {
        return time;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isVideo() {
        return video;
    }

    /** 列表项底部展示的「来源  评论  时间」 */
    public String getMetaText() {
        return source + "   " + comment + "   " + time;
    }
}
