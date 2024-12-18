package com.bot.common.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SpidersUtils {
    @Resource
    private OkHttpClient client;

    /**
     * 获取html
     *
     * @param url
     * @return
     */
    public String getHtml(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            log.error("获取html body失败", e);
            return null;
        }
    }

    /**
     * 将链接解析成频道信息
     *
     * @param url
     * @return
     */
    //https://t.me/tfwzclz  群组
    //https://t.me/DNSPODT 频道
    public int getChannelCount(String url) {
        String html = getHtml(url);
        if (StrUtil.isEmpty(html)) {
            return 0;
        }
        if (html.contains("you can contact")) {
            return 0;
        }
        try {
            Document document = Jsoup.parse(html);
            String title = document.getElementsByClass("tgme_page_title").text();
            if (StrUtil.isEmpty(title)) {
                return 0;
            }
            String extraInfo = document.getElementsByClass("tgme_page_extra").text();
            if (extraInfo.contains("subscribers")) {
                try {
                    String countStr = extraInfo.split("subscribers")[0].replace(" ", "");
                    return Integer.parseInt(countStr);
                } catch (Exception e) {
                    log.error("解析频道成员数失败", e);
                    return 0;
                }
            } else if (extraInfo.contains("subscriber")) {
                try {
                    String countStr = extraInfo.split("subscriber")[0].replace(" ", "");
                    return Integer.parseInt(countStr);
                } catch (Exception e) {
                    log.error("解析频道成员数失败", e);
                    return 0;
                }
            }
        } catch (Exception e) {
            log.error("解析html失败", e);
            return 0;
        }
        return 0;
    }
}
