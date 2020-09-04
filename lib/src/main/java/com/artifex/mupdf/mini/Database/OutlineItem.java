package com.artifex.mupdf.mini.Database;

/**
 * Created by simha on 8/8/20.
 */

public class OutlineItem {
    public String title;
    public String uri;
    public int page;
    public int found;

    public OutlineItem(
            String title,
            String uri,
            int page,
            int found
    ) {
        this.title = title;
        this.uri = uri;
        this.page = page;
        this.found = found;
    }

    public String toString() {
        return title;
    }
}
