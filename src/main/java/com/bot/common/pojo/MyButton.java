package com.bot.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * "url" or "callback"
 */
@Data
@AllArgsConstructor
public class MyButton {
    private String buttonName;
    private String type;// "url" or "callback"
    private String value;
}
