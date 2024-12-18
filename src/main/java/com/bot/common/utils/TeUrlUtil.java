package com.bot.common.utils;

public class TeUrlUtil {
    public static String getUrlExpression(String url, String content) {
        return "<a href='" + url + "'>" + content + "</a>";
    }
}
