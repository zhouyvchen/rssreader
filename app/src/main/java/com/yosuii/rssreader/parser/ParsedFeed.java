package com.yosuii.rssreader.parser;

import java.util.ArrayList;
import java.util.List;

public class ParsedFeed {
    public String title = "";
    public String siteUrl = "";
    public String description = "";
    public final List<ParsedArticle> articles = new ArrayList<>();
}
