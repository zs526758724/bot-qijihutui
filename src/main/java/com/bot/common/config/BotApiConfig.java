package com.bot.common.config;

import com.bot.common.pojo.TelegramData;
import jakarta.annotation.Resource;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.ExponentialBackOff;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configurable
@Component
public class BotApiConfig {
    @Resource
    private TelegramData telegramData;

    @Bean(name = "botOptions")
    public DefaultBotOptions getBotOptions() {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        if (telegramData.getIsOpenProxy() == 1) {
            botOptions.setProxyHost(telegramData.getProxyHost());
            botOptions.setProxyPort(telegramData.getProxyPort());
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            ExponentialBackOff backOff = new ExponentialBackOff.Builder()
                    .setInitialIntervalMillis(500)
                    .setMaxIntervalMillis(1000)
                    .setMaxElapsedTimeMillis(5000)
                    .build();
            botOptions.setBackOff(backOff);
        }
        //"message",
        //        "edited_message",
        //        "channel_post",
        //        "edited_channel_post",
        //        "message_reaction",
        //        "message_reaction_count",
        //        "inline_query",
        //        "chosen_inline_result",
        //        "callback_query",
        //        "shipping_query",
        //        "pre_checkout_query",
        //        "poll",
        //        "poll_answer",
        //        "my_chat_member",
        //        "chat_member",
        //        "chat_join_request",
        //        "chat_boost",
        //        "removed_chat_boost",
        String[] allowedUpdatesString = {
                "message",
                "edited_message",
                "channel_post",
                "edited_channel_post",
                "inline_query",
                "chosen_inline_result",
                "callback_query",
                "shipping_query",
                "pre_checkout_query",
                "poll",
                "video_note",
                "chat_member",
                "message_reaction",
                "message_reaction_count",
                "poll_answer",
                "my_chat_member",
                "chat_join_request",
                "chat_boost",
                "removed_chat_boost"
        };
        botOptions.setAllowedUpdates(Arrays.asList(allowedUpdatesString));
        return botOptions;
    }


    @Bean
    public DefaultBotSession getDefaultBotSession(DefaultBotOptions botOptions) {
        DefaultBotSession defaultBotSession = new DefaultBotSession();
        defaultBotSession.setOptions(botOptions);
        return defaultBotSession;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        if (telegramData.getIsOpenProxy() == 1) {
            return new OkHttpClient.Builder()
                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(telegramData.getProxyHost(), telegramData.getProxyPort())))
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
        } else {
            return new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
    }


}
