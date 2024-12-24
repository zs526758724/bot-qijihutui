package com.bot.common.pojo;

import lombok.Data;

@Data
public class ChannelHtInfo {
    private String chatId;
    private StringBuilder message;
    private String url;
    private String inviteLink;
    private String cdname;
}
