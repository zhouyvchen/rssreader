package com.yosuii.rssreader.util;

public class HtmlUtils {
    private HtmlUtils() {
    }

    public static String buildArticleHtml(String title, String body, String link) {
        String safeTitle = title == null ? "" : title;
        String safeBody = body == null || body.isEmpty() ? "暂无正文内容" : body;
        String safeLink = link == null ? "" : link;
        return "<!doctype html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<style>body{font-family:sans-serif;line-height:1.6;padding:16px;color:#202124;}"
                + "img,video,iframe{max-width:100%;height:auto;}a{color:#0b57d0;}</style>"
                + "</head><body><h1>" + safeTitle + "</h1>"
                + safeBody
                + "<p><a href='" + safeLink + "'>打开原文</a></p>"
                + "</body></html>";
    }
}
