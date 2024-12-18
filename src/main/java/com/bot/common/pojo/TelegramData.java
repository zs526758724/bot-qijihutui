package com.bot.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "telegram")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class TelegramData {
    private String proxyHost;//代理ip
    private int proxyPort;//代理端口
    private int isOpenProxy;//是否开启代理
}
