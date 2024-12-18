package com.bot.bota.bot;

import cn.hutool.json.JSONUtil;
import com.bot.common.utils.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.Executor;

@Slf4j
public class FzzfExecBot extends TelegramLongPollingBot {
    private final String botUserName;
    private final String token;
    private final FzzfDealBot fzzfDealBot;

    private final Executor taskExecutor;

    private final Executor taskExecutorsigle;

    public FzzfExecBot(DefaultBotOptions options, String botUserName, String token, @Qualifier("taskExecutor") Executor taskExecutor, @Qualifier("taskExecutorsigle") Executor taskExecutorsigle, FzzfDealBot fzzfDealBot) {
        super(options);
        this.taskExecutor = taskExecutor;
        this.taskExecutorsigle = taskExecutorsigle;
        this.botUserName = botUserName;
        this.token = token;
        this.fzzfDealBot = fzzfDealBot;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    /**
     * 监听消息
     *
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        log.info(JSONUtil.toJsonStr(update));
        //判断是否是回调消息
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            if (callbackQuery.getData().equals("null")) {
                Msg.answerCallbackQueryNull(update, this);
                return;
            }
            if (callbackQuery.getMessage().isUserMessage()) {//私聊回调
                taskExecutor.execute(() -> fzzfDealBot.handleUserCallbackQuery(update, this));
                return;
            }
            if (callbackQuery.getMessage().isGroupMessage()) {//普通群回调
                taskExecutor.execute(() -> fzzfDealBot.handleGroupCallbackQuery(update, this));
                return;
            }
            if (callbackQuery.getMessage().isSuperGroupMessage()) {//超级群回调
                taskExecutor.execute(() -> fzzfDealBot.handleSuperGroupCallbackQuery(update, this));
                return;
            }
            return;
        }
        //消息处理
        if ((update.getChannelPost() != null && update.getChannelPost().getChat().isChannelChat()) || (update.hasMessage() && update.getMessage().getChat().isSuperGroupChat()) || (update.hasMessage() && update.getMessage().getChat().isGroupChat()) || (update.hasMessage() && update.getMessage().getChat().isUserChat())) {
            if (update.hasChannelPost() && update.getChannelPost().getChat().isChannelChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleChannelMsg(update, this));//频道
                return;
            }
            if (update.hasMessage() && update.getMessage().getChat().isSuperGroupChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleSuperGroupMsg(update, this));//超级群
                return;
            }
            if (update.hasMessage() && update.getMessage().getChat().isGroupChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleGroupMsg(update, this));//群聊
                return;
            }
            if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleUserMsg(update, this));//私聊
                return;
            }
            return;
        }
        /*
          非机器人成员变更
         */
        if (update.hasChatMember() && (update.getChatMember().getChat().isChannelChat()
                || update.getChatMember().getChat().isGroupChat()
                || update.getChatMember().getChat().isSuperGroupChat()
                || update.getChatMember().getChat().isUserChat())) {
            if (update.getChatMember().getChat().isChannelChat()) {
                taskExecutorsigle.execute(() -> fzzfDealBot.handleJoinChannel(update, this));//频道
                return;
            }
            if (update.getChatMember().getChat().isGroupChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleJoinGroup(update, this));//群
                return;
            }
            if (update.getChatMember().getChat().isSuperGroupChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleJoinSuperGroup(update, this));//超级群
            }
            if (update.getChatMember().getChat().isUserChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handleJoinUser(update, this));//用户
            }
            return;
        }
        //聊天加入请求
        if (update.hasChatJoinRequest()) {
            taskExecutor.execute(() -> fzzfDealBot.handleChatJoinRequest(update, this));
            return;
        }

        //机器人权限变化
        if (update.hasMyChatMember() && (update.getMyChatMember().getChat().isChannelChat()
                || update.getMyChatMember().getChat().isGroupChat()
                || update.getMyChatMember().getChat().isSuperGroupChat()
                || update.getMyChatMember().getChat().isUserChat())) {
            if (update.getMyChatMember().getChat().isChannelChat()) {
                taskExecutorsigle.execute(() -> fzzfDealBot.handlePermissionsFromChannel(update, this));//频道
                return;
            }
            if (update.getMyChatMember().getChat().isGroupChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handlePermissionsFromGroup(update, this));//群
                return;
            }
            if (update.getMyChatMember().getChat().isSuperGroupChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handlePermissionsFromSuperGroup(update, this));//超级群
                return;
            }
            if (update.getMyChatMember().getChat().isUserChat()) {
                taskExecutor.execute(() -> fzzfDealBot.handlePermissionsFromUserChat(update, this));//用户
                return;
            }
            return;
        }
    }

}
