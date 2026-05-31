package com.yosuii.rssreader.parser;

import com.yosuii.rssreader.util.DateUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

public class FeedParser {
    public ParsedFeed parse(String xml) throws Exception {
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(new StringReader(xml));

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("rss".equalsIgnoreCase(tag)) {
                    return parseRss(parser);
                }
                if ("feed".equalsIgnoreCase(tag)) {
                    return parseAtom(parser);
                }
            }
            eventType = parser.next();
        }
        throw new IllegalArgumentException("不支持的 Feed 格式");
    }

    private ParsedFeed parseRss(XmlPullParser parser) throws Exception {
        ParsedFeed feed = new ParsedFeed();
        ParsedArticle currentArticle = null;
        boolean insideItem = false;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("item".equalsIgnoreCase(tag)) {
                    insideItem = true;
                    currentArticle = new ParsedArticle();
                } else if (insideItem && currentArticle != null) {
                    readRssArticleTag(parser, currentArticle, tag);
                } else {
                    readRssFeedTag(parser, feed, tag);
                }
            } else if (eventType == XmlPullParser.END_TAG && "item".equalsIgnoreCase(parser.getName())) {
                insideItem = false;
                if (currentArticle != null && !currentArticle.link.isEmpty()) {
                    feed.articles.add(currentArticle);
                }
                currentArticle = null;
            }
            eventType = parser.next();
        }
        return feed;
    }

    private void readRssFeedTag(XmlPullParser parser, ParsedFeed feed, String tag) throws Exception {
        if ("title".equalsIgnoreCase(tag) && feed.title.isEmpty()) {
            feed.title = readText(parser);
        } else if ("link".equalsIgnoreCase(tag) && feed.siteUrl.isEmpty()) {
            feed.siteUrl = readText(parser);
        } else if ("description".equalsIgnoreCase(tag) && feed.description.isEmpty()) {
            feed.description = readText(parser);
        }
    }

    private void readRssArticleTag(XmlPullParser parser, ParsedArticle article, String tag) throws Exception {
        if ("title".equalsIgnoreCase(tag)) {
            article.title = readText(parser);
        } else if ("link".equalsIgnoreCase(tag)) {
            article.link = readText(parser);
        } else if ("description".equalsIgnoreCase(tag)) {
            article.summary = readText(parser);
        } else if ("encoded".equalsIgnoreCase(tag)) {
            article.content = readText(parser);
        } else if ("creator".equalsIgnoreCase(tag) || "author".equalsIgnoreCase(tag)) {
            article.author = readText(parser);
        } else if ("pubDate".equalsIgnoreCase(tag)) {
            article.publishedAt = DateUtils.parseFeedDate(readText(parser));
        } else if ("guid".equalsIgnoreCase(tag) && article.link.isEmpty()) {
            article.link = readText(parser);
        }
    }

    private ParsedFeed parseAtom(XmlPullParser parser) throws Exception {
        ParsedFeed feed = new ParsedFeed();
        ParsedArticle currentArticle = null;
        boolean insideEntry = false;

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("entry".equalsIgnoreCase(tag)) {
                    insideEntry = true;
                    currentArticle = new ParsedArticle();
                } else if (insideEntry && currentArticle != null) {
                    readAtomArticleTag(parser, currentArticle, tag);
                } else {
                    readAtomFeedTag(parser, feed, tag);
                }
            } else if (eventType == XmlPullParser.END_TAG && "entry".equalsIgnoreCase(parser.getName())) {
                insideEntry = false;
                if (currentArticle != null && !currentArticle.link.isEmpty()) {
                    feed.articles.add(currentArticle);
                }
                currentArticle = null;
            }
            eventType = parser.next();
        }
        return feed;
    }

    private void readAtomFeedTag(XmlPullParser parser, ParsedFeed feed, String tag) throws Exception {
        if ("title".equalsIgnoreCase(tag) && feed.title.isEmpty()) {
            feed.title = readText(parser);
        } else if ("subtitle".equalsIgnoreCase(tag) && feed.description.isEmpty()) {
            feed.description = readText(parser);
        } else if ("link".equalsIgnoreCase(tag) && feed.siteUrl.isEmpty()) {
            feed.siteUrl = readAtomLink(parser);
        }
    }

    private void readAtomArticleTag(XmlPullParser parser, ParsedArticle article, String tag) throws Exception {
        if ("title".equalsIgnoreCase(tag)) {
            article.title = readText(parser);
        } else if ("link".equalsIgnoreCase(tag)) {
            String link = readAtomLink(parser);
            if (!link.isEmpty()) {
                article.link = link;
            }
        } else if ("summary".equalsIgnoreCase(tag)) {
            article.summary = readText(parser);
        } else if ("content".equalsIgnoreCase(tag)) {
            article.content = readText(parser);
        } else if ("name".equalsIgnoreCase(tag)) {
            article.author = readText(parser);
        } else if ("updated".equalsIgnoreCase(tag) || "published".equalsIgnoreCase(tag)) {
            article.publishedAt = DateUtils.parseFeedDate(readText(parser));
        } else if ("id".equalsIgnoreCase(tag) && article.link.isEmpty()) {
            article.link = readText(parser);
        }
    }

    private String readAtomLink(XmlPullParser parser) {
        String href = parser.getAttributeValue(null, "href");
        return href == null ? "" : href;
    }

    private String readText(XmlPullParser parser) throws Exception {
        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            parser.nextTag();
        }
        return text == null ? "" : text.trim();
    }
}
