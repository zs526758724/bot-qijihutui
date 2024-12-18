package com.bot.common.utils;

import org.telegram.telegrambots.meta.api.objects.Update;

public class CmdUtil {
    /**
     * 检测私聊信息是否为命令
     *
     * @param update
     * @return
     */
    public static boolean isPrivateCmd(Update update) {
        return update.hasMessage() && update.getMessage().hasText() && update.getMessage().isCommand() && update.getMessage().getChat().getType().equals("private");
    }

    /**
     * 检测私聊命令是否有有参数
     *
     * @param update
     * @return
     */
    public static boolean isCmdWithArgs(Update update) {
        if (isPrivateCmd(update)) {
            return update.getMessage().getText().contains(" ");
        }
        return false;
    }

    /**
     * 获取私聊命令的参数
     *
     * @param update
     * @return
     */
    public static String getCmdArgs(Update update) {
        if (isCmdWithArgs(update)) {
            return update.getMessage().getText().split(" ")[1];
        }
        return "";
    }

    /**
     * 获取私聊命令的名字
     *
     * @param update
     * @return
     */
    public static String getCmdNameWithArgs(Update update) {
        if (isPrivateCmd(update)) {
            return update.getMessage().getText().split(" ")[0].replace("/", "");
        }
        return "";
    }

    /**
     * 获取私聊命令的名字（不带参数）
     *
     * @param update
     * @return
     */
    public static String getCmdNameNoWithArgs(Update update) {
        if (isPrivateCmd(update)) {
            return update.getMessage().getText().replace("/", "");
        }
        return "";
    }

}
