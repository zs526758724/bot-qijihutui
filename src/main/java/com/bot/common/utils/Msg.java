package com.bot.common.utils;

import cn.hutool.core.util.StrUtil;
import com.bot.common.pojo.MyButton;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ExportChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class Msg {


    /**
     * 发送消息
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void sendMsg(String msg, TelegramLongPollingBot execBot, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        try {
            execBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
        }
    }

    /**
     * 发送消息并返回消息id
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static Integer sendMsgReturnMessageId(String msg, TelegramLongPollingBot execBot, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        try {
            Message execute = execBot.execute(sendMessage);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
            return 0;
        }
    }

    /**
     * 发送消息
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void sendMsgAndEntities(String msg, TelegramLongPollingBot execBot, Long chatId, List<MessageEntity> entities) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        if (entities != null) {
            sendMessage.setEntities(entities);
        }
        try {
            execBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
        }
    }

    /**
     * 发送消息
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void sendMsgAndEntitiesMd(String msg, TelegramLongPollingBot execBot, Long chatId, List<MessageEntity> entities) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        sendMessage.enableMarkdownV2(true);
        if (entities != null) {
            sendMessage.setEntities(entities);
        }
        try {
            execBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
        }
    }

    /**
     * 发送消息HTML
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void sendMsgHTML(String msg, TelegramLongPollingBot execBot, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.enableHtml(true);
        sendMessage.disableWebPagePreview();
        try {
            execBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
        }
    }

    /**
     * 发送消息Markdown
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void sendMsgMarkdown(String msg, TelegramLongPollingBot execBot, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.enableMarkdownV2(true);
        sendMessage.disableWebPagePreview();
        try {
            execBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
        }
    }

    /**
     * 发送消息强制回复键盘
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void sendMsgForceReplyKeyboard(String msg, TelegramLongPollingBot execBot, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        sendMessage.setReplyMarkup(new ForceReplyKeyboard());
        try {
            execBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
        }
    }

    /**
     * 发送消息并带键盘
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static Integer sendMsgAndKeyboard(String msg, TelegramLongPollingBot execBot, Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup, List<MessageEntity> entities) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        if (inlineKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            Message execute = execBot.execute(sendMessage);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送消息并带键盘
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static Integer sendMsgAndKeyboard(String msg, TelegramLongPollingBot execBot, String format, Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup, List<MessageEntity> entities) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        if (format != null) {
            if (format.equals("html")) {
                sendMessage.enableHtml(true);
            } else {
                sendMessage.enableMarkdown(true);
            }
        }
        if (inlineKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            Message execute = execBot.execute(sendMessage);
            return execute.getMessageId();
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送消息并带键盘 互推转用
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static HashSet<String> sendMsgAndKeyboardHT(String msg, TelegramLongPollingBot execBot, String format, Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        HashSet<String> set = new HashSet<>();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(msg);
        sendMessage.disableWebPagePreview();
        if (format != null) {
            if (format.equals("html")) {
                sendMessage.enableHtml(true);
            } else {
                sendMessage.enableMarkdown(true);
            }
        }
        if (inlineKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            Message execute = execBot.execute(sendMessage);
            set.add(String.valueOf(execute.getMessageId()));
        } catch (TelegramApiException e) {
            log.error("发送消息失败{}", e.getMessage());
            set.add("消息发送失败,频道id：" + chatId + "，原因：" + e.getMessage());
        }
        return set;
    }

    /**
     * 编辑消息并带键盘
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void editMsgAndKeyboard(String msg, TelegramLongPollingBot execBot, Long chatId, Integer messageId, InlineKeyboardMarkup inlineKeyboardMarkup, List<MessageEntity> entities) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(msg);
        editMessageText.disableWebPagePreview();
        editMessageText.setMessageId(messageId);
        if (entities != null) {
            editMessageText.setEntities(entities);
        }
        if (inlineKeyboardMarkup != null) {
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            execBot.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("编辑消息失败{}", e.getMessage());
        }
    }

    /**
     * 编辑消息并带键盘
     *
     * @param msg
     * @param execBot
     * @param chatId
     */
    public static void editMsgAndKeyboard(String msg, TelegramLongPollingBot execBot, String format, Long chatId, Integer messageId, InlineKeyboardMarkup inlineKeyboardMarkup, List<MessageEntity> entities) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(msg);
        editMessageText.setMessageId(messageId);
        editMessageText.disableWebPagePreview();
        if (format != null) {
            if (format.equals("html")) {
                editMessageText.enableHtml(true);
            } else {
                editMessageText.enableMarkdown(true);
            }
        }
        if (entities != null) {
            editMessageText.setEntities(entities);
        }
        if (inlineKeyboardMarkup != null) {
            editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            execBot.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("编辑消息失败{}", e.getMessage());
        }
    }


    /**
     * 发送带回调数据的消息
     *
     * @param update
     * @param msg
     * @param execBot
     */
    public static void answerCallbackQuery(Update update, String msg, TelegramLongPollingBot execBot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
        answerCallbackQuery.setText(msg);
        answerCallbackQuery.setShowAlert(false);
        try {
            execBot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 发送带回调数据的消息
     *
     * @param update
     * @param msg
     * @param execBot
     */
    public static void answerCallbackQueryALert(Update update, String msg, TelegramLongPollingBot execBot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
        answerCallbackQuery.setText(msg);
        answerCallbackQuery.setShowAlert(true);
        try {
            execBot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 回复空消息
     *
     * @param update
     * @param execBot
     */
    public static void answerCallbackQueryNull(Update update, TelegramLongPollingBot execBot) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
        answerCallbackQuery.setText("");
        answerCallbackQuery.setShowAlert(false);
        try {
            execBot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("回复消息失败: {}", e.getMessage());
        }
    }

    /**
     * 删除消息
     *
     * @param channelId 频道ID
     * @param messageId 消息ID
     * @param execBot   执行机器人
     */
    public static void deleteMessage(Long channelId, Integer messageId, TelegramLongPollingBot execBot) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(channelId);
            deleteMessage.setMessageId(messageId);
            execBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("删除消息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取InlineKeyboardMarkup
     * 是否方法
     * List<List<MyButton>> lists = new ArrayList<>();组件列表
     * List<MyButton> list2 = new ArrayList<>();行列表
     * list2.add(new MyButton("按钮文字", "类型", "回调或者链接"));每行按钮
     * lists.add(list2);
     * 类型为 "url" or "callback"
     *
     * @param list 按钮列表
     * @return InlineKeyboardMarkup
     */
    public static InlineKeyboardMarkup getInlineKeyboardMarkup(List<List<MyButton>> list) {
        try {
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            for (List<MyButton> row : list) {
                List<InlineKeyboardButton> buttonsInline = new ArrayList<>();
                for (MyButton button : row) {
                    InlineKeyboardButton buttonInline = new InlineKeyboardButton();
                    if (StrUtil.isBlank(button.getButtonName())) {
                        return null;
                    }
                    buttonInline.setText(button.getButtonName());
                    if (button.getType().equals("url")) {
                        buttonInline.setUrl(button.getValue());
                    } else if (button.getType().equals("callback")) {
                        String value = button.getValue();
                        if (StrUtil.isBlank(value)) {
                            return null;
                        }
                        buttonInline.setCallbackData(value);
                    } else {
                        return null;
                    }
                    buttonsInline.add(buttonInline);
                }
                rowsInline.add(buttonsInline);
            }
            inlineKeyboardMarkup.setKeyboard(rowsInline);
            return inlineKeyboardMarkup;
        } catch (Exception e) {
            log.error("获取InlineKeyboardMarkup失败: {}", e.getMessage());
            return null;
        }
    }

    public static String getInviteLink(long chatId, TelegramLongPollingBot execBot) {
        ExportChatInviteLink exportChatInviteLink = new ExportChatInviteLink();
        exportChatInviteLink.setChatId(chatId);
        try {
            // 获取邀请链接
            return execBot.execute(exportChatInviteLink);
        } catch (TelegramApiException e) {
            log.error("获取邀请链接失败{}", e.getMessage());
            return "";
        }
    }

    /**
     * 退出群聊
     *
     * @param chatId
     * @param execBot
     */
    public static void leaveChat(Long chatId, TelegramLongPollingBot execBot) {
        LeaveChat leaveChat = new LeaveChat();
        leaveChat.setChatId(chatId);
        try {
            execBot.execute(leaveChat);
        } catch (TelegramApiException e) {
            log.error("退出群聊失败{}", e.getMessage());
        }
    }

    /**
     * 获取频道成员数量
     *
     * @param chatId
     * @param bot
     * @return
     */
    public static int getChatMembersCount(Long chatId, TelegramLongPollingBot bot) {
        GetChatMemberCount getChatMemberCount = new GetChatMemberCount();
        getChatMemberCount.setChatId(chatId);
        try {
            return bot.execute(getChatMemberCount);
        } catch (TelegramApiException e) {
            log.error("获取频道成员数量失败：{}", e.getMessage());
            return 0;
        }
    }

    //字符串转InlineKeyboardMarkup
    public static InlineKeyboardMarkup getInlineKeyboardMarkupString(String string) {
        try {
            String[] split = string.split("\n");
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();//按钮组
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            for (String s : split) {
                List<InlineKeyboardButton> rowInline = new ArrayList<>();//每行的按钮
                String[] split1 = s.split("&&");
                for (String s1 : split1) {
                    InlineKeyboardButton buttonInline = new InlineKeyboardButton();
                    String[] split2 = s1.split("-");
                    buttonInline.setText(split2[0]);
                    if (split2[1].contains("https://")) {
                        buttonInline.setUrl(split2[1]);
                    } else {
                        buttonInline.setUrl("https://" + split2[1]);
                    }
                    rowInline.add(buttonInline);
                }
                rowsInline.add(rowInline);
            }
            markupInline.setKeyboard(rowsInline);
            return markupInline;
        } catch (Exception e) {
            log.error("字符串转InlineKeyboardMarkup失败: {}", e.getMessage());
            return null;
        }
    }
}
