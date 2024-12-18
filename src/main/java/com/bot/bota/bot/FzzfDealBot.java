package com.bot.bota.bot;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bota.entity.*;
import com.bot.bota.pojo.Permissions;
import com.bot.bota.service.*;
import com.bot.bota.pojo.ChannelHtInfo;
import com.bot.common.utils.Msg;
import com.bot.common.pojo.MyButton;
import com.bot.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.bot.bota.enums.TeFzzfStr.*;

@Component
/**
 * 处理信息
 */
@Slf4j
public class FzzfDealBot {

    @Autowired
    private SpidersUtils spidersUtils;
    @Autowired
    private FzzfadService fzzfadService;
    @Autowired
    private FzzfcdService fzzfcdService;
    @Autowired
    private FzzfchannelhtService fzzfchannelhtService;
    @Autowired
    private FzzffansrecordsinService fzzffansrecordsinService;
    @Autowired
    private FzzfchannelmsgService fzzfchannelmsgService;
    @Autowired
    private FzzfteadminService fzzfteadminService;
    @Autowired
    private FzzfpermissionsService fzzfpermissionsService;
    @Autowired
    private FzzfmydataService fzzfmydataService;
    @Autowired
    private FzzfcopywriterService fzzfcopywriterService;
    @Autowired
    private FzzfvisitorsService fzzfvisitorsService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    // 定时任务调度器
    private ScheduledExecutorService htscheduler;


    public void handleUserCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        if (update.getCallbackQuery().getData().startsWith("gl")) {
            Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getCallbackQuery().getFrom().getId());
            if (fzzfteadmin == null) {
                return;
            }
            handlestartsWithglCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (update.getCallbackQuery().getData().startsWith("cdstart")) {
            handlestartsWithcdstartCallbackQuery(update, fzzfExecBot);
            return;
        }
    }

    private void handlestartsWithcdstartCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (data.contains("#")) {
            String[] split = data.split("#");
            switch (split[0]) {
                case "cdstartsqprev", "cdstartsqnext" -> {
                    sendfzsq(chatId, fzzfExecBot, update.getCallbackQuery().getMessage().getMessageId(), Integer.parseInt(split[1]));
                }
                case "cdstartsqselectfz" -> {
                    Long cdid = Long.valueOf(split[1]);
                    Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    sendcdstartsqselectfz(chatId, cdid, fzzfExecBot, update.getCallbackQuery().getMessage().getMessageId());
                }
                case "cdstartsqadd" -> {
                    Long cdid = Long.valueOf(split[1]);
                    Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    long count = fzzfchannelhtService.countByCdidYTG(Math.toIntExact(cdid));
                    long l = Long.parseLong(fzzfmydataService.getValueByMyKey("cdrl"));
                    if (count >= l) {
                        Msg.answerCallbackQueryALert(update, "当前车队成员已满", fzzfExecBot);
                        return;
                    }
                    //判断是否有满足的频道
                    List<Fzzfpermissions> listByInviteuserid = fzzfpermissionsService.findListByInviteuserid(chatId);
                    if (listByInviteuserid == null || listByInviteuserid.isEmpty()) {
                        Msg.answerCallbackQueryALert(update, str10, fzzfExecBot);
                        return;
                    }
                    boolean flag = false;
                    for (Fzzfpermissions fzzfpermissions : listByInviteuserid) {
                        if (fzzfpermissions.getCount() > fzzfcd.getMinisubscription()) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        Msg.answerCallbackQueryALert(update, str31, fzzfExecBot);
                        return;
                    }
                    List<Fzzfchannelht> listBySubmitterid = fzzfchannelhtService.getListBySubmitterid(chatId);
                    //将listByInviteuserid中chatid与listBySubmitterid中cdid相同的元素剔除
                    if (listBySubmitterid == null || listBySubmitterid.isEmpty()) {
                        sendcdstartsqadd(chatId, cdid, fzzfExecBot, update.getCallbackQuery().getMessage().getMessageId());
                    } else {
                        HashSet<String> chatids = new HashSet<>();
                        for (Fzzfpermissions fzzfpermissions : listByInviteuserid) {
                            chatids.add(fzzfpermissions.getChatid());
                        }
                        for (Fzzfchannelht fzzfchannelht : listBySubmitterid) {
                            chatids.remove(fzzfchannelht.getChatid());
                        }
                        if (chatids.isEmpty()) {
                            Msg.answerCallbackQueryALert(update, str10, fzzfExecBot);
                        } else {
                            sendcdstartsqadd(chatId, cdid, fzzfExecBot, update.getCallbackQuery().getMessage().getMessageId());
                        }
                    }
                }
                case "cdstartsqaddpd" -> {
                    Long cdid = Long.valueOf(split[1]);
                    String chatid = split[2];
                    Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getByChatid(chatid);
                    if (fzzfchannelht != null) {
                        Msg.answerCallbackQuery(update, "频道已存在", fzzfExecBot);
                        return;
                    }
                    long count = fzzfchannelhtService.countByCdidYTG(Math.toIntExact(cdid));
                    long l = Long.parseLong(fzzfmydataService.getValueByMyKey("cdrl"));
                    if (count >= l) {
                        Msg.answerCallbackQuery(update, "当前车队成员已满", fzzfExecBot);
                        return;
                    }
                    Fzzfpermissions oneByChatId = fzzfpermissionsService.getOneByChatId(Long.valueOf(chatid));
                    if (oneByChatId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    fzzfchannelht = new Fzzfchannelht();
                    fzzfchannelht.setTitle(oneByChatId.getTitle());
                    fzzfchannelht.setCdid(fzzfcd.getId());
                    fzzfchannelht.setChatid(chatid);
                    fzzfchannelht.setSubmitterid(oneByChatId.getInviteuserid());
                    int channelCount = spidersUtils.getChannelCount(oneByChatId.getUrl());
                    if (channelCount == 0) {
                        int channelCount1 = spidersUtils.getChannelCount(oneByChatId.getInviteurl());
                        if (channelCount1 == 0) {
                            int chatMembersCount = Msg.getChatMembersCount(chatId, fzzfExecBot);
                            fzzfchannelht.setCount(chatMembersCount);
                        } else {
                            fzzfchannelht.setCount(channelCount1);
                        }
                    } else {
                        fzzfchannelht.setCount(channelCount);
                    }
                    fzzfchannelht.setCreatetime(TeTimeUtil.getNowTime());
                    fzzfchannelht.setUrl(oneByChatId.getUrl());
                    fzzfchannelht.setAudit("0");
                    fzzfchannelht.setInvitelink(oneByChatId.getInviteurl());
                    fzzfchannelhtService.save(fzzfchannelht);
                    Msg.answerCallbackQuery(update, "提交成功", fzzfExecBot);
                    //15秒后再次同步邀请链接
                    scheduler.schedule(() -> {
                        Fzzfchannelht byChatid = fzzfchannelhtService.getByChatid(chatid);
                        Fzzfpermissions oneByChatId1 = fzzfpermissionsService.getOneByChatId(chatId);
                        if (byChatid != null && oneByChatId1 != null) {
                            byChatid.setInvitelink(oneByChatId1.getInviteurl());
                            fzzfchannelhtService.updateById(byChatid);
                        }
                    }, 15, TimeUnit.SECONDS);
                    sendcdstartsqadd(chatId, cdid, fzzfExecBot, update.getCallbackQuery().getMessage().getMessageId());
                    //发送频道审核通知
                    sendChannelAuditNotice(fzzfExecBot, fzzfchannelht.getId(), "sendnew", null);
                    Msg.sendMsgHTML("您提交的频道" + TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()) + "已提交审核，请耐心等待审核结果。", fzzfExecBot, chatId);
                }
                case "cdstartglmypddetail" -> {
                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                    Long id = Long.parseLong(split[1]);
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getById(id);
                    if (fzzfchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    Fzzfpermissions oneByChatId = fzzfpermissionsService.getOneByChatId(Long.valueOf(fzzfchannelht.getChatid()));
                    Fzzfcd byId = fzzfcdService.getById(fzzfchannelht.getCdid());
                    StringBuilder msgText = new StringBuilder("频道详情");
                    msgText.append("\n频道标题: ").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()));
                    msgText.append("\n频道ID: ").append(fzzfchannelht.getChatid());
                    msgText.append("\n提交者ID: ").append(fzzfchannelht.getSubmitterid());
                    msgText.append("\n车队: ").append(byId.getTitle());
                    msgText.append("\n人数: ").append(fzzfchannelht.getCount());
                    msgText.append("\n审核状态: ").append(fzzfchannelht.getAudit().equals("1") ? "通过" : "审核中");
                    msgText.append("\n管理权限: ").append(oneByChatId.getMsgandinvqx().equals("1") ? "有" : "无");
                    msgText.append("\n5日进粉统计: ");
                    List<String> beforeDays = TeTimeUtil.getBeforeDays(5);
                    List<Fzzffansrecordsin> listByChatId = fzzffansrecordsinService.findListByChatId(fzzfchannelht.getChatid());
                    for (String beforeDay : beforeDays) {
                        int count = 0;
                        for (Fzzffansrecordsin fzzffansrecordsin : listByChatId) {
                            if (beforeDay.equals(fzzffansrecordsin.getDate())) {
                                count = fzzffansrecordsin.getFanscount();
                                break;
                            }
                        }
                        msgText.append("\n").append(beforeDay).append("：").append(count).append("人");
                    }
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str17, "callback", "cdstartglmypdexit#" + id));
                    list1.add(new MyButton(str30, "callback", "cdstartglmypdedittitle#" + id));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton(str0713, "callback", "cdstartreturnmenu"));
                    list2.add(new MyButton(str08, "callback", "cdstartglmypd"));
                    lists.add(list2);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText.toString(), fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                }
                case "cdstartglmypdedittitle" -> {
                    String id = split[1];
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getById(id);
                    if (fzzfchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    if (!chatId.toString().equals(fzzfchannelht.getSubmitterid())) {
                        Msg.answerCallbackQuery(update, "无权限操作", fzzfExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    Msg.sendMsgForceReplyKeyboard(fzzfchannelht.getChatid() + "\n请输入新的频道标题", fzzfExecBot, chatId);
                }
                case "cdstartglmypdexit" -> {
                    String id = split[1];
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getById(id);
                    if (fzzfchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    if (!chatId.toString().equals(fzzfchannelht.getSubmitterid())) {
                        Msg.answerCallbackQuery(update, "无权限操作", fzzfExecBot);
                        return;
                    }
                    fzzfchannelhtService.removeById(id);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(fzzfchannelht.getChatid());
                    fzzfchannelmsgService.removeByChatids(strings);
                    fzzffansrecordsinService.removeByChatids(strings);
                    Msg.leaveChat(Long.valueOf(fzzfchannelht.getChatid()), fzzfExecBot);
                    Msg.answerCallbackQuery(update, "退出频道成功", fzzfExecBot);
                    sendcdstartglmypd(fzzfExecBot, update, 1);
                }
                case "cdstartglmypdprev", "cdstartglmypdnext" -> {
                    String current = split[1];
                    sendcdstartglmypd(fzzfExecBot, update, Integer.parseInt(current));
                }

            }
        } else {
            switch (data) {
                case "cdstartvive" -> {

                }
                case "cdstartsq" -> {
                    sendfzsq(update.getCallbackQuery().getMessage().getChatId(), fzzfExecBot, update.getCallbackQuery().getMessage().getMessageId(), 1);
                }
                case "cdstartreturnmenu" -> {
                    sendStartMain(update.getCallbackQuery().getMessage().getChatId(), fzzfExecBot, "edit", update.getCallbackQuery().getMessage().getMessageId());
                }
                case "cdstartglmypd" -> {
                    sendcdstartglmypd(fzzfExecBot, update, 1);
                }
            }
        }
    }

    //todo 我的频道管理
    private void sendcdstartglmypd(FzzfExecBot fzzfExecBot, Update update, int current) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        List<Fzzfchannelht> listBySubmitterId = fzzfchannelhtService.findListBySubmitterId(chatId);
        if (listBySubmitterId == null || listBySubmitterId.isEmpty()) {
            Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
            return;
        } else {
            Page<Fzzfchannelht> pagesBySubmitterId = fzzfchannelhtService.getPagesBySubmitterId(chatId, current);
            if (pagesBySubmitterId.getRecords() == null || pagesBySubmitterId.getRecords().isEmpty()) {
                Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                return;
            } else {
                List<Fzzfchannelht> records = pagesBySubmitterId.getRecords();
                StringBuilder message = new StringBuilder();
                message.append("我的车队频道列表");
                int i = 1;
                List<Fzzfpermissions> fzzfpermissionsList = fzzfpermissionsService.list();
                List<Fzzfcd> list = fzzfcdService.list();
                for (Fzzfchannelht fzzfchannelht : records) {
                    message.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()));
                    message.append(" - ").append(TeNumberUtil.formatNumber(fzzfchannelht.getCount()));
                    for (Fzzfcd fzzfcd : list) {
                        if (fzzfchannelht.getCdid().equals(fzzfcd.getId())) {
                            message.append(" - ").append(fzzfcd.getTitle());
                            break;
                        }
                    }
                    if (fzzfchannelht.getAudit().equals("1")) {
                        message.append("\n").append("✅审核通过");
                    } else {
                        message.append("\n").append("\uD83D\uDEAB审核中");
                    }
                    i++;
                    for (Fzzfpermissions fzzfpermissions : fzzfpermissionsList) {
                        if (fzzfpermissions.getChatid().equals(fzzfchannelht.getChatid())) {
                            if (fzzfpermissions.getMsgandinvqx().equals("1")) {
                                message.append("✅有管理权限");
                            } else {
                                message.append("\uD83D\uDEAB无管理权限");
                            }
                            break;
                        }
                    }
                }
                List<List<MyButton>> lists = new ArrayList<>();
                //每行三个
                int size = records.size() / 3 + (records.size() % 3 > 0 ? 1 : 0);
                for (int j = 0; j < size; j++) {
                    List<MyButton> list2 = new ArrayList<>();
                    for (int k = 0; k < 3; k++) {
                        if (j * 3 + k < records.size()) {
                            Fzzfchannelht fzzfchannelht = records.get(j * 3 + k);
                            list2.add(new MyButton((j * 3 + k + 1) + "", "callback", "cdstartglmypddetail#" + fzzfchannelht.getId()));
                        } else {
                            list2.add(new MyButton("-", "callback", "null"));
                        }
                    }
                    lists.add(list2);
                }
                List<MyButton> list3 = new ArrayList<>();
                if (current != 1) {
                    list3.add(new MyButton("⬅\uFE0F上一页", "callback", "cdstartglmypdprev" + "#" + (current - 1)));
                } else {
                    list3.add(new MyButton("-", "callback", "null"));
                }
                list3.add(new MyButton(current + "/" + pagesBySubmitterId.getPages() + "页", "callback", "null"));
                if (pagesBySubmitterId.getPages() > current) {//存在下一页
                    list3.add(new MyButton("➡\uFE0F下一页", "callback", "cdstartglmypdnext" + "#" + (current + 1)));
                } else {
                    list3.add(new MyButton("-", "callback", "null"));
                }
                lists.add(list3);
                List<MyButton> list99 = new ArrayList<>();
                list99.add(new MyButton(str0713, "callback", "cdstartreturnmenu"));
                lists.add(list99);
                InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                Msg.editMsgAndKeyboard(message.toString(), fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
            }
        }
    }

    /**
     * 发送审核
     *
     * @param fzzfExecBot
     * @param id
     * @param sendMode
     * @param messageId
     */
    private void sendChannelAuditNotice(FzzfExecBot fzzfExecBot, Integer id, String sendMode, Integer messageId) {
        Fzzfchannelht byId = fzzfchannelhtService.getById(id);
        if (byId == null) {
            return;
        }
        Integer cdid = byId.getCdid();
        Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
        String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
        msgText += "\n频道ID: " + byId.getChatid();
        msgText += "\n提交者ID: " + byId.getSubmitterid();
        msgText += "\n车队: " + fzzfcd.getTitle();
        msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
        msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(fzzfcd.getMiniread());
        msgText += "\n人数: " + byId.getCount();
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(str11, "callback", "shtg#" + id));
        list1.add(new MyButton(str12, "callback", "shjj#" + id));
        list1.add(new MyButton(str13, "callback", "shjh#" + id));
        lists.add(list1);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        String shqid = fzzfmydataService.getValueByMyKey("shqid");
        if (sendMode.equals("sendnew") && messageId == null) {
            Msg.sendMsgAndKeyboard(msgText, fzzfExecBot, "html", Long.valueOf(shqid), inlineKeyboardMarkup, null);
        } else {
            Msg.editMsgAndKeyboard(msgText, fzzfExecBot, "html", Long.valueOf(shqid), messageId, inlineKeyboardMarkup, null);
        }

    }

    private void sendcdstartsqadd(Long chatId, Long cdid, FzzfExecBot fzzfExecBot, Integer messageId) {
        Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
        StringBuilder msgText = new StringBuilder();
        msgText.append("请选择频道");
        msgText.append("\n频道标题: ").append(fzzfcd.getTitle());
        msgText.append("\n频道介绍: ").append(fzzfcd.getCddesc());
        msgText.append("\n车队类型:").append(fzzfcd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(fzzfcd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(fzzfcd.getMiniread()));
        msgText.append("\n请确认是否已经将机器人拉入频道并授予管理员权限");
        msgText.append("\n请选择一个提交");
        List<List<MyButton>> lists = new ArrayList<>();
        List<Fzzfpermissions> listByInviteuserid = fzzfpermissionsService.findListByInviteuserid(chatId);
        List<Fzzfchannelht> listBySubmitterid = fzzfchannelhtService.getListBySubmitterid(chatId);
        if (listBySubmitterid == null || listBySubmitterid.isEmpty()) {
            for (Fzzfpermissions fzzfpermissions : listByInviteuserid) {
                List<MyButton> list1 = new ArrayList<>();
                list1.add(new MyButton(fzzfpermissions.getTitle(), "callback", "cdstartsqaddpd#" + cdid + "#" + fzzfpermissions.getChatid()));
                lists.add(list1);
            }
        } else {
            HashSet<String> chatids = new HashSet<>();
            for (Fzzfpermissions fzzfpermissions : listByInviteuserid) {
                if (fzzfpermissions.getCount() > fzzfcd.getMinisubscription()) {
                    chatids.add(fzzfpermissions.getChatid());
                }
            }
            for (Fzzfchannelht fzzfchannelht : listBySubmitterid) {
                chatids.remove(fzzfchannelht.getChatid());
            }
            for (String chatid : chatids) {
                List<MyButton> list1 = new ArrayList<>();
                String title = "";
                for (Fzzfpermissions fzzfpermissions : listByInviteuserid) {
                    if (fzzfpermissions.getChatid().equals(chatid)) {
                        title = fzzfpermissions.getTitle();
                        break;
                    }
                }
                list1.add(new MyButton(title, "callback", "cdstartsqaddpd#" + cdid + "#" + chatid));
                lists.add(list1);
            }
        }
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "cdstartreturnmenu"));
        list99.add(new MyButton(str08, "callback", "cdstartsq"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), fzzfExecBot, null, chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendcdstartsqselectfz(Long chatId, Long fzzfcdid, FzzfExecBot fzzfExecBot, Integer messageId) {
        StringBuilder msgText = new StringBuilder();
        Fzzfcd fzzfcd = fzzfcdService.getById(fzzfcdid);
        Long count = fzzfchannelhtService.lambdaQuery().eq(Fzzfchannelht::getCdid, fzzfcdid).count();
        long l = Long.parseLong(fzzfmydataService.getValueByMyKey("cdrl"));
        msgText.append("车队标题:").append(fzzfcd.getTitle());
        msgText.append("\n车队介绍:").append(fzzfcd.getCddesc());
        msgText.append("\n当前/最大(成员):").append(count).append("/").append(l);
        msgText.append("\n车队类型:").append(fzzfcd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(fzzfcd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(fzzfcd.getMiniread()));
        msgText.append("\n成员列表:");
        List<Fzzfchannelht> fzzfchannelhtslist = fzzfchannelhtService.findListByCdid(fzzfcdid);
        if (fzzfchannelhtslist == null || fzzfchannelhtslist.isEmpty()) {
            msgText.append("\n当前没有互推成员");
        } else {
            int i = 1;
            for (Fzzfchannelht fzzfchannelht : fzzfchannelhtslist) {
                msgText.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getInvitelink(), fzzfchannelht.getTitle()));
                msgText.append(" - ").append(TeNumberUtil.formatNumber(fzzfchannelht.getCount()));
                i++;
            }
        }
        if (count == l) {
            msgText.append("\n\n\uD83D\uDD34当前车队已满员，无法申请加入");
        } else {
            msgText.append("\n\n✅车队还未满员,你可以申请!");
        }
        List<List<MyButton>> lists = new ArrayList<>();
        if (count < l) {
            List<MyButton> list1 = new ArrayList<>();
            list1.add(new MyButton(str09, "callback", "cdstartsqadd#" + fzzfcdid));
            lists.add(list1);
        }

        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "cdstartreturnmenu"));
        list99.add(new MyButton(str08, "callback", "cdstartsq"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    /**
     * 分组申请选择界面
     *
     * @param chatId
     * @param fzzfExecBot
     * @param messageId
     * @param current
     */
    private void sendfzsq(Long chatId, FzzfExecBot fzzfExecBot, Integer messageId, int current) {
        long count = fzzfcdService.count();
        long pdcount = fzzfchannelhtService.count();
        String msgText = "频道-车队大厅";
        msgText += "\n车队数量:" + count;
        msgText += "\n频道数量:" + pdcount;
        msgText += "\n图标介绍:";
        msgText += "\n\uD83D\uDFE2未满/\uD83D\uDD34已满|队内成员数-最大成员数|最小订阅需求";
        msgText += "\n选择下方车队进入指定车队，然后选择你要申请的频道进行上车提交。";
        Page<Fzzfcd> page = fzzfcdService.pageList(current);
        List<List<MyButton>> lists = new ArrayList<>();
        int cdrl = Integer.parseInt(fzzfmydataService.getValueByMyKey("cdrl"));
//        for (Fzzfcd fzzfcd : page.getRecords()) {
//            List<MyButton> list1 = new ArrayList<>();
//            Long count1 = fzzfchannelhtService.lambdaQuery().eq(Fzzfchannelht::getCdid, fzzfcd.getId()).count();
//            String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + fzzfcd.getTitle() + "|"
//                    + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
//            list1.add(new MyButton(buttonName, "callback", "cdstartsqselectfz#" + fzzfcd.getId()));
//            lists.add(list1);
//        }
        int size = page.getRecords().size() / 2 + (page.getRecords().size() % 2 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                if (j * 2 + k < page.getRecords().size()) {
                    Fzzfcd fzzfcd = page.getRecords().get(j * 2 + k);
                    Long count1 = fzzfchannelhtService.lambdaQuery().eq(Fzzfchannelht::getCdid, fzzfcd.getId()).count();
                    String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + fzzfcd.getTitle() + "|"
                            + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
                    list2.add(new MyButton(buttonName, "callback", "cdstartsqselectfz#" + fzzfcd.getId()));
                } else {
                    list2.add(new MyButton("-", "callback", "null"));
                }
            }
            lists.add(list2);
        }

        List<MyButton> list2 = new ArrayList<>();
        if (current != 1) {
            list2.add(new MyButton("⬅\uFE0F上一页", "callback", "cdstartsqprev" + "#" + (current - 1)));
        } else {
            list2.add(new MyButton("-", "callback", "null"));
        }
        list2.add(new MyButton(current + "/" + page.getPages() + "页", "callback", "null"));
        if (page.getPages() > current) {//存在下一页
            list2.add(new MyButton("➡\uFE0F下一页", "callback", "cdstartsqnext" + "#" + (current + 1)));
        } else {
            list2.add(new MyButton("-", "callback", "null"));
        }
        lists.add(list2);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "cdstartreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, fzzfExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    //todo gl开头回调分支
    private void handlestartsWithglCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        String data = update.getCallbackQuery().getData();
        if (data.equals("glreturnmenu")) {
            myGlMenu(update.getCallbackQuery().getMessage().getChatId(), fzzfExecBot, "edit", update.getCallbackQuery().getMessage().getMessageId());
        }
        if (data.startsWith("glfz")) {
            handlestartsWithglfzCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glht")) {
            handlestartsWithglhtCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glpd")) {
            handlestartsWithglpdCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glad")) {
            handlestartsWithgladCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glsendfrequency")) {
            handlestartsWithglsendfrequencyCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glrefreshcount")) {
            handlestartsWithglrefreshcountCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glcheckpd")) {
            handlestartsWithglcheckpdCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("gluserused")) {
            handlestartsWithgluserusedCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("gltgbutton")) {
            handlestartsWithgltgbuttonCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glwa")) {
            handlestartsWithglwaCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glbb")) {
            handlestartsWithglbbCallbackQuery(update, fzzfExecBot);
            return;
        }

    }

    //todo 处理报表相关回调
    private void handlestartsWithglbbCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glbb" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    List<Fzzfcd> list = fzzfcdService.list();
                    List<Fzzfchannelht> listByAudit = fzzfchannelhtService.findListByAudit("1");
                    List<Fzzffansrecordsin> list1 = fzzffansrecordsinService.list();
                    for (Fzzfcd fzzfcd : list) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("车队:").append(fzzfcd.getTitle());
                        int i = 1;
                        for (Fzzfchannelht fzzfchannelht : listByAudit) {
                            if (fzzfchannelht.getCdid().equals(fzzfcd.getId())) {
                                sb.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()));
                                int today = 0;
                                for (Fzzffansrecordsin fzzffansrecordsin : list1) {
                                    if (fzzffansrecordsin.getChatid().equals(fzzfchannelht.getChatid()) && fzzffansrecordsin.getDate().equals(TeTimeUtil.getNowTime())) {
                                        today = fzzffansrecordsin.getFanscount();
                                        break;
                                    }
                                }
                                int total = 0;
                                for (Fzzffansrecordsin fzzffansrecordsin : list1) {
                                    if (fzzffansrecordsin.getChatid().equals(fzzfchannelht.getChatid())) {
                                        total += fzzffansrecordsin.getFanscount();
                                    }
                                }
                                sb.append("当日:").append(today).append(",总共").append(total);
                                i++;
                            }
                        }
                        //发送
                        if (i == 1) {
                            System.out.println("没有数据");
                            continue;
                        }
                        Msg.sendMsgHTML(sb.toString(), fzzfExecBot, chatId);
                    }
                }
            }
        }
    }

    /**
     * @param update
     * @param fzzfExecBot
     */
    //todo 管理文案回调
    private void handlestartsWithglwaCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glwaedit" -> {
                    String key = split[1];
                    String msg = "请编辑回复以下内容：\n";
                    switch (key) {
                        case "1" -> {
                            msg += "`编辑start01文案";
                        }
                        case "2" -> {
                            msg += "`编辑start02文案";
                        }
                        case "3" -> {
                            msg += "`编辑help文案";
                        }
                    }
                    msg += "\n请替换为您的内容`";
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    Msg.sendMsgMarkdown(msg, fzzfExecBot, chatId);
                }
                case "glwavive" -> {
                    String key = split[1];
                    Fzzfcopywriter oneByTextkey = switch (key) {
                        case "1" -> fzzfcopywriterService.getOneByTextkey("start01");
                        case "2" -> fzzfcopywriterService.getOneByTextkey("start02");
                        case "3" -> fzzfcopywriterService.getOneByTextkey("help");
                        default -> null;
                    };
                    if (oneByTextkey == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    List<MessageEntity> entities = null;
                    if (StrUtil.isNotBlank(oneByTextkey.getTextentities())) {
                        entities = JSONUtil.toList(JSONUtil.parseArray(oneByTextkey.getTextentities()), MessageEntity.class);
                    }
                    Msg.sendMsgAndEntities(oneByTextkey.getTextcontent(), fzzfExecBot, chatId, entities);
                }
            }

        } else {
            switch (data) {
                case "glwa" -> {
                    String msgText = "管理文案";
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton("start01文案", "callback", "glwavive#1"));
                    list1.add(new MyButton("编辑", "callback", "glwaedit#1"));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton("start02文案", "callback", "glwavive#2"));
                    list2.add(new MyButton("编辑", "callback", "glwaedit#2"));
                    lists.add(list2);
                    List<MyButton> list3 = new ArrayList<>();
                    list3.add(new MyButton("帮助文案", "callback", "glwavive#3"));
                    list3.add(new MyButton("编辑", "callback", "glwaedit#3"));
                    lists.add(list3);
                    List<MyButton> list99 = new ArrayList<>();
                    list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
                    lists.add(list99);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, fzzfExecBot, chatId, messageId, inlineKeyboardMarkup, null);
                }
            }
        }

    }

    private void handlestartsWithgltgbuttonCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "gltgbutton" -> {
                    sendgltgbutton(chatId, fzzfExecBot, messageId);
                }
                case "gltgbuttonvive" -> {
                    String mybutton = fzzfmydataService.getValueByMyKey("mybutton");
                    if (StrUtil.isBlank(mybutton)) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    InlineKeyboardMarkup inlineKeyboardMarkupString = Msg.getInlineKeyboardMarkupString(mybutton);
                    if (inlineKeyboardMarkupString == null) {
                        Msg.answerCallbackQuery(update, "按钮格式错误", fzzfExecBot);
                        return;
                    } else {
                        Msg.answerCallbackQueryNull(update, fzzfExecBot);
                        String msgText = "按钮预览：\n" + mybutton;
                        Msg.sendMsgAndKeyboard(msgText, fzzfExecBot, chatId, inlineKeyboardMarkupString, null);
                    }
                }
                case "gltgbuttonedit" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = """
                            请编辑输入以下模板：
                            •插入单个按钮：
                            `推广按钮编辑
                            按钮文本-t.me/LinkExample`

                            •在一行中插入多个按钮：
                            `推广按钮编辑
                            按钮文本-t.me/LinkExample&&按钮文本-t.me/LinkExample`

                            •插入多行按钮：
                            `推广按钮编辑
                            按钮文本-t.me/LinkExample
                            按钮文本-t.me/LinkExample`
                            """;
                    Msg.sendMsgMarkdown(msgText, fzzfExecBot, chatId);
                }
                case "gltgbuttonclear" -> {
                    fzzfmydataService.updateValueByKey("mybutton", "");
                    Msg.answerCallbackQuery(update, "已清除", fzzfExecBot);
                }
            }
        }
    }

    private void sendgltgbutton(Long chatId, FzzfExecBot fzzfExecBot, Integer messageId) {
        String msgText = "管理推广按钮";
        //按钮
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(str25, "callback", "gltgbuttonvive"));
        list1.add(new MyButton(str26, "callback", "gltgbuttonedit"));
        list1.add(new MyButton(str27, "callback", "gltgbuttonclear"));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, fzzfExecBot, chatId, messageId, inlineKeyboardMarkup, null);

    }

    private void handlestartsWithgluserusedCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "gluserused" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    try {
                        Long id = fzzfExecBot.getMe().getId();
                        List<Fzzfvisitors> listByBotId = fzzfvisitorsService.findListByBotId(id);
                        if (listByBotId != null && !listByBotId.isEmpty()) {

                            HashSet<String> ids = new HashSet<>();
                            for (Fzzfvisitors fzzfvisitors : listByBotId) {
                                ids.add(fzzfvisitors.getUserid());
                            }
                            int totalCount = ids.size();//总用户数
                            //统计今日访问用户数
                            int todayCount = 0;
                            String nowTime = TeTimeUtil.getNowTime();
                            for (Fzzfvisitors fzzfvisitors : listByBotId) {
                                if (fzzfvisitors.getFwtime().equals(nowTime)) {
                                    todayCount++;
                                }
                            }
                            String msgText = "机器人总用户数：" + totalCount + "人，今日访问用户数：" + todayCount + "人。";
                            Msg.sendMsg(msgText, fzzfExecBot, chatId);
                        }

                    } catch (TelegramApiException e) {
                        log.error("获取botId失败", e);
                    }
                }
            }
        }
    }

    private void handlestartsWithglcheckpdCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glcheckpd" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = "正在检测。。。。。";
                    List<Fzzfchannelht> list = fzzfchannelhtService.list();
                    if (list != null && !list.isEmpty()) {
                        Msg.sendMsgMarkdown(msgText, fzzfExecBot, chatId);
                        for (Fzzfchannelht fzzfchannelht : list) {
                            int channelCount1 = spidersUtils.getChannelCount(fzzfchannelht.getUrl());
                            int channelCount2 = spidersUtils.getChannelCount(fzzfchannelht.getInvitelink());
                            if (channelCount1 == 0 && channelCount2 == 0) {
                                Msg.sendMsgHTML("频道id" + fzzfchannelht.getChatid() + "：" + TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()) + "疑似失效，请检查。", fzzfExecBot, chatId);
                                Msg.sendMsgMarkdown("私聊机器人输入以下内容可以删除该频道:\n" + "`/glpddelete " + fzzfchannelht.getId() + "`", fzzfExecBot, chatId);
                            }
                            log.info("频道id{}：{}检测结果：{}|{}", fzzfchannelht.getChatid(), TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()), channelCount1, channelCount2);
                        }
                        Msg.sendMsg("检测完成", fzzfExecBot, chatId);
                    } else {
                        Msg.answerCallbackQuery(update, "暂无数据", fzzfExecBot);
                        return;
                    }
                }
            }
        }
    }

    private void handlestartsWithglrefreshcountCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glrefreshcount" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = "刷新频道/群组成员数量中...";
                    Msg.sendMsg(msgText, fzzfExecBot, chatId);
                    fzzfchannelhtService.refreshCount();
                    Msg.sendMsg("已刷新", fzzfExecBot, chatId);
                }
            }
        }
    }

    private void handlestartsWithglsendfrequencyCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glsendfrequency" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = "请编辑以下模版发送：（备注，频率单位为分钟）";
                    msgText += "\n`频道发送频率修改";
                    msgText += "\n频率:30`";
                    Msg.sendMsgMarkdown(msgText, fzzfExecBot, chatId);
                }
            }
        }
    }


    private void handlestartsWithgladCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "gladstatus" -> {
                    Long id = Long.parseLong(split[1]);
                    Fzzfad fzzfad = fzzfadService.getById(id);
                    if (fzzfad == null) {
                        Msg.answerCallbackQuery(update, "广告不存在", fzzfExecBot);
                        return;
                    }
                    if (fzzfad.getStatus().equals("1")) {
                        fzzfad.setStatus("0");
                        fzzfadService.updateById(fzzfad);
                    } else {
                        fzzfad.setStatus("1");
                        fzzfadService.updateById(fzzfad);
                    }
                    sendGladMenu(chatId, fzzfExecBot, messageId);
                }
                case "gladdelete" -> {
                    Long id = Long.parseLong(split[1]);
                    Fzzfad fzzfad = fzzfadService.getById(id);
                    if (fzzfad == null) {
                        Msg.answerCallbackQuery(update, "广告不存在", fzzfExecBot);
                        return;
                    }
                    fzzfadService.removeById(id);
                    sendGladMenu(chatId, fzzfExecBot, messageId);
                }
            }
        } else {
            switch (data) {
                case "glad" -> {
                    sendGladMenu(chatId, fzzfExecBot, messageId);
                }
                case "gladadd" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = "请编辑以下模版发送：（备注，位置1代表消息头部，2代表尾部）";
                    msgText += "\n`广告添加";
                    msgText += "\n标题:xxx";
                    msgText += "\nurl:xxx";
                    msgText += "\n位置:1`";
                    Msg.sendMsgMarkdown(msgText, fzzfExecBot, chatId);
                }
            }
        }
    }

    private void sendGladMenu(Long chatId, FzzfExecBot fzzfExecBot, Integer messageId) {
        String msgText = "广告管理";
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(str19, "callback", "gladadd"));
        lists.add(list1);
        List<Fzzfad> allByAdtype = fzzfadService.findAllByAdtype("1");
        if (allByAdtype != null && !allByAdtype.isEmpty()) {
            for (Fzzfad fzzfad : allByAdtype) {
                List<MyButton> list2 = new ArrayList<>();
                list2.add(new MyButton(fzzfad.getTitle(), "url", fzzfad.getContent()));
                if (fzzfad.getFlag().equals("1")) {
                    list2.add(new MyButton(str23, "callback", "null"));
                } else {
                    list2.add(new MyButton(str24, "callback", "null"));
                }
                if (fzzfad.getStatus().equals("1")) {
                    list2.add(new MyButton(str21, "callback", "gladstatus#" + fzzfad.getId()));
                } else {
                    list2.add(new MyButton(str22, "callback", "gladstatus#" + fzzfad.getId()));
                }
                list2.add(new MyButton(str20, "callback", "gladdelete#" + fzzfad.getId()));
                lists.add(list2);
            }
        }
        List<Fzzfad> allByAdtypew = fzzfadService.findAllByAdtype("2");
        if (allByAdtypew != null && !allByAdtypew.isEmpty()) {
            for (Fzzfad fzzfad : allByAdtypew) {
                List<MyButton> list2 = new ArrayList<>();
                list2.add(new MyButton(fzzfad.getTitle(), "url", fzzfad.getContent()));
                if (fzzfad.getFlag().equals("1")) {
                    list2.add(new MyButton(str23, "callback", "null"));
                } else {
                    list2.add(new MyButton(str24, "callback", "null"));
                }
                if (fzzfad.getStatus().equals("1")) {
                    list2.add(new MyButton(str21, "callback", "gladstatus#" + fzzfad.getId()));
                } else {
                    list2.add(new MyButton(str22, "callback", "gladstatus#" + fzzfad.getId()));
                }
                list2.add(new MyButton(str20, "callback", "gladdelete#" + fzzfad.getId()));
                lists.add(list2);
            }
        }
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, fzzfExecBot, chatId, messageId, inlineKeyboardMarkup, null);

    }

    /**
     * 处理glpd开头的回调
     *
     * @param update
     * @param fzzfExecBot
     */
    //todo 处理glpd开头的回调 管理频道
    private void handlestartsWithglpdCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glpdprev", "glpdnext" -> {
                    int current = Integer.parseInt(split[1]);
                    sendGlpdMenu(update, fzzfExecBot, current);
                }
                case "glpddetail" -> {
                    Long id = Long.parseLong(split[1]);
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getById(id);
                    if (fzzfchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    Fzzfpermissions oneByChatId = fzzfpermissionsService.getOneByChatId(Long.valueOf(fzzfchannelht.getChatid()));
                    Fzzfcd byId = fzzfcdService.getById(fzzfchannelht.getCdid());
                    StringBuilder msgText = new StringBuilder("频道详情");
                    msgText.append("\n频道标题: ").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()));
                    msgText.append("\n频道ID: ").append(fzzfchannelht.getChatid());
                    msgText.append("\n提交者ID: ").append(fzzfchannelht.getSubmitterid());
                    msgText.append("\n车队: ").append(byId.getTitle());
                    msgText.append("\n人数: ").append(fzzfchannelht.getCount());
                    msgText.append("\n审核状态: ").append(fzzfchannelht.getAudit().equals("1") ? "通过" : "审核中");
                    msgText.append("\n管理权限: ").append(oneByChatId.getMsgandinvqx().equals("1") ? "有" : "无");
                    msgText.append("\n5日进粉统计: ");
                    List<String> beforeDays = TeTimeUtil.getBeforeDays(5);
                    List<Fzzffansrecordsin> listByChatId = fzzffansrecordsinService.findListByChatId(fzzfchannelht.getChatid());
                    for (String beforeDay : beforeDays) {
                        int count = 0;
                        for (Fzzffansrecordsin fzzffansrecordsin : listByChatId) {
                            if (beforeDay.equals(fzzffansrecordsin.getDate())) {
                                count = fzzffansrecordsin.getFanscount();
                                break;
                            }
                        }
                        msgText.append("\n").append(beforeDay).append("：").append(count).append("人");
                    }
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str17, "callback", "glpdexit#" + id));
                    list1.add(new MyButton(str28, "callback", "glpdreurl#" + id));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton(str0713, "callback", "glreturnmenu#" + id));
                    list2.add(new MyButton(str08, "callback", "glpd"));
                    lists.add(list2);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText.toString(), fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                }
                case "glpdreurl" -> {
                    String id = split[1];
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getById(id);
                    if (fzzfchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    Fzzfpermissions oneByChatId = fzzfpermissionsService.getOneByChatId(Long.valueOf(fzzfchannelht.getChatid()));
                    if (oneByChatId.getMsgandinvqx().equals("0")) {
                        Msg.answerCallbackQuery(update, "无管理权限", fzzfExecBot);
                        return;
                    }
                    String inviteLink = Msg.getInviteLink(Long.parseLong(fzzfchannelht.getChatid()), fzzfExecBot);
                    if (StrUtil.isNotBlank(inviteLink)) {
                        Msg.answerCallbackQueryNull(update, fzzfExecBot);
                        oneByChatId.setInviteurl(inviteLink);
                        fzzfpermissionsService.updateById(oneByChatId);
                        fzzfchannelht.setInvitelink(inviteLink);
                        fzzfchannelhtService.updateById(fzzfchannelht);
                        Msg.sendMsg("频道邀请链接已更新,链接为:" + inviteLink, fzzfExecBot, chatId);
                    } else {
                        Msg.answerCallbackQuery(update, "生成邀请链接失败", fzzfExecBot);
                    }
                    return;
                }
                case "glpdexit" -> {
                    String id = split[1];
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getById(id);
                    if (fzzfchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    fzzfchannelhtService.removeById(id);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(fzzfchannelht.getChatid());
                    fzzfchannelmsgService.removeByChatids(strings);
                    fzzffansrecordsinService.removeByChatids(strings);
                    Msg.leaveChat(Long.valueOf(fzzfchannelht.getChatid()), fzzfExecBot);
                    Msg.answerCallbackQuery(update, "退出频道成功", fzzfExecBot);
                    sendGlpdMenu(update, fzzfExecBot, 1);
                }
            }
        } else {
            switch (data) {
                case "glpd" -> {
                    sendGlpdMenu(update, fzzfExecBot, 1);
                }
            }
        }
    }

    private void sendGlpdMenu(Update update, FzzfExecBot fzzfExecBot, int current) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        StringBuilder message = new StringBuilder("管理频道");
        Page<Fzzfchannelht> fzzfchannelhtPage = fzzfchannelhtService.listOrderByCdid(current);
        if (fzzfchannelhtPage.getRecords() == null || fzzfchannelhtPage.getRecords().isEmpty()) {
            Msg.answerCallbackQuery(update, "没有可管理的频道", fzzfExecBot);
            return;
        }
        int i = 1;
        List<Fzzfpermissions> fzzfpermissionsList = fzzfpermissionsService.list();
        List<Fzzfcd> list = fzzfcdService.list();
        for (Fzzfchannelht fzzfchannelht : fzzfchannelhtPage.getRecords()) {
            message.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getUrl(), fzzfchannelht.getTitle()));
            message.append(" - ").append(TeNumberUtil.formatNumber(fzzfchannelht.getCount()));
            for (Fzzfcd fzzfcd : list) {
                if (fzzfchannelht.getCdid().equals(fzzfcd.getId())) {
                    message.append(" - ").append(fzzfcd.getTitle());
                    break;
                }
            }
            if (fzzfchannelht.getAudit().equals("1")) {
                message.append("\n").append("✅审核通过");
            } else {
                message.append("\n").append("\uD83D\uDEAB审核中");
            }
            i++;
            for (Fzzfpermissions fzzfpermissions : fzzfpermissionsList) {
                if (fzzfpermissions.getChatid().equals(fzzfchannelht.getChatid())) {
                    if (fzzfpermissions.getMsgandinvqx().equals("1")) {
                        message.append("✅有管理权限");
                    } else {
                        message.append("\uD83D\uDEAB无管理权限");
                    }
                    break;
                }
            }
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<Fzzfchannelht> records = fzzfchannelhtPage.getRecords();
        //每行三个
        int size = records.size() / 3 + (records.size() % 3 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 3; k++) {
                if (j * 3 + k < records.size()) {
                    Fzzfchannelht fzzfchannelht = records.get(j * 3 + k);
                    list2.add(new MyButton((j * 3 + k + 1) + "", "callback", "glpddetail#" + fzzfchannelht.getId()));
                } else {
                    list2.add(new MyButton("-", "callback", "null"));
                }
            }
            lists.add(list2);
        }
        List<MyButton> list3 = new ArrayList<>();
        if (current != 1) {
            list3.add(new MyButton("⬅\uFE0F上一页", "callback", "glpdprev" + "#" + (current - 1)));
        } else {
            list3.add(new MyButton("-", "callback", "null"));
        }
        list3.add(new MyButton(current + "/" + fzzfchannelhtPage.getPages() + "页", "callback", "null"));
        if (fzzfchannelhtPage.getPages() > current) {//存在下一页
            list3.add(new MyButton("➡\uFE0F下一页", "callback", "glpdnext" + "#" + (current + 1)));
        } else {
            list3.add(new MyButton("-", "callback", "null"));
        }
        lists.add(list3);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(message.toString(), fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    /**
     * 处理glht开头的回调
     *
     * @param update
     * @param fzzfExecBot
     */
    //todo 处理glht开头的回调 启动互推、关闭互推
    private void handlestartsWithglhtCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "xxx" -> {

                }
            }
        } else {
            switch (data) {
                case "glhtstart" -> {
                    String cdstatus = fzzfmydataService.getValueByMyKey("cdstatus");
                    if (cdstatus.equals("1")) {
                        Msg.answerCallbackQuery(update, str14, fzzfExecBot);
                        myGlMenu(chatId, fzzfExecBot, "edit", messageId);
                        return;
                    }
                    // 定时任务
                    //todo 定时任务主体

                    Runnable pushTask = () -> {
                        String cdname = fzzfmydataService.getValueByMyKey("cdname");
                        List<Fzzfcd> fzzfcdList = fzzfcdService.list();//获取所有车队
                        if (fzzfcdList == null || fzzfcdList.isEmpty()) {
                            Msg.answerCallbackQuery(update, "车队为空，无法启动", fzzfExecBot);
                            return;
                        }
                        List<Fzzfchannelht> fzzfchannelhtList = fzzfchannelhtService.findListByAudit("1");
                        if (fzzfchannelhtList == null || fzzfchannelhtList.isEmpty()) {
                            Msg.answerCallbackQuery(update, "没有需要互推的频道", fzzfExecBot);
                            return;
                        }
                        boolean flag = false;
                        for (Fzzfchannelht fzzfchannelht : fzzfchannelhtList) {
                            String invitelink = fzzfchannelht.getInvitelink();
                            if (StrUtil.isBlank(invitelink)) {
                                String inviteLink = Msg.getInviteLink(Long.parseLong(fzzfchannelht.getChatid()), fzzfExecBot);
                                if (StrUtil.isNotBlank(inviteLink)) {
                                    fzzfchannelht.setInvitelink(inviteLink);
                                    fzzfchannelhtService.updateById(fzzfchannelht);
                                    flag = true;
                                }
                            }
                        }
                        if (flag) {
                            fzzfchannelhtList = fzzfchannelhtService.findListByAudit("1");
                        }
                        List<List<Fzzfchannelht>> fzzfchannelhtslists = new ArrayList<>();
                        for (Fzzfcd fzzfcd : fzzfcdList) {//根据不同车队分组
                            List<Fzzfchannelht> fzzfchannelhtslist = new ArrayList<>();
                            for (Fzzfchannelht fzzfchannelht : fzzfchannelhtList) {
                                if (fzzfchannelht.getCdid().equals(fzzfcd.getId())) {
                                    fzzfchannelhtslist.add(fzzfchannelht);
                                }
                            }
                            if (!fzzfchannelhtslist.isEmpty() && fzzfchannelhtslist.size() >= 2) {
                                fzzfchannelhtslists.add(fzzfchannelhtslist);
                            }
                        }
                        List<ChannelHtInfo> channelHtInfoList = new ArrayList<>();
                        for (List<Fzzfchannelht> fzzfchannelhtslist : fzzfchannelhtslists) {//根据分组进行互推
                            for (Fzzfchannelht fzzfchannelht : fzzfchannelhtslist) {//对组内成员进行互推
                                ChannelHtInfo channelHtInfo = new ChannelHtInfo();
                                channelHtInfo.setChatId(fzzfchannelht.getChatid());
                                channelHtInfo.setInviteLink(fzzfchannelht.getInvitelink());
                                channelHtInfo.setUrl(fzzfchannelht.getUrl());
                                StringBuilder message = new StringBuilder();
                                String zuname = "\uD83D\uDE80来自奇迹互推【";
                                for (Fzzfcd fzzfcd : fzzfcdList) {
                                    if (fzzfchannelht.getCdid().equals(fzzfcd.getId())) {
                                        zuname += fzzfcd.getTitle();
                                        channelHtInfo.setCdname(fzzfcd.getTitle());
                                        break;
                                    }
                                }
                                zuname += "】组\uD83D\uDE80";
                                String botName = "qijihutui";
                                try {
                                    botName = fzzfExecBot.getMe().getUserName();
                                } catch (TelegramApiException e) {
                                    log.error("获取机器人用户名失败", e);
                                }
                                message.append("<b>").append(TeUrlUtil.getUrlExpression("https://t.me/" + botName, zuname)).append("</b>");
                                //头部广告
                                List<Fzzfad> byAdtypeHead = fzzfadService.findByAdtype("1");
                                if (byAdtypeHead != null && !byAdtypeHead.isEmpty()) {
                                    for (Fzzfad fzzfad : byAdtypeHead) {
                                        message.append("\n<b>AD:").append(TeUrlUtil.getUrlExpression(fzzfad.getContent(), fzzfad.getTitle())).append("</b>");
                                    }
                                }
                                message.append("\n\n");
                                int i = 1;
                                for (Fzzfchannelht fzzfchannelht1 : fzzfchannelhtslist) {
                                    if (!fzzfchannelht1.getId().equals(fzzfchannelht.getId())) {
                                        message.append(i).append(".").append(TeUrlUtil.getUrlExpression(fzzfchannelht1.getInvitelink(), fzzfchannelht1.getTitle()));
                                        message.append("\n");
                                        i++;
                                    }
                                }
                                //尾部广告
                                List<Fzzfad> byAdtypetail = fzzfadService.findByAdtype("2");
                                if (byAdtypetail != null && !byAdtypetail.isEmpty()) {
                                    for (Fzzfad fzzfad : byAdtypetail) {
                                        message.append("\n<b>AD:").append(TeUrlUtil.getUrlExpression(fzzfad.getContent(), fzzfad.getTitle())).append("</b>");
                                    }
                                }
                                channelHtInfo.setMessage(message);
                                channelHtInfoList.add(channelHtInfo);
                            }
                        }
                        List<Fzzfchannelmsg> list = fzzfchannelmsgService.list();
                        String shqid = fzzfmydataService.getValueByMyKey("shqid");
                        String mybutton = fzzfmydataService.getValueByMyKey("mybutton");
                        InlineKeyboardMarkup inlineKeyboardMarkupString = null;
                        if (StrUtil.isNotBlank(mybutton)) {
                            inlineKeyboardMarkupString = Msg.getInlineKeyboardMarkupString(mybutton);
                        }
                        List<Fzzfmydata> pdremove = fzzfmydataService.findListByKey("pdremove");
                        for (ChannelHtInfo channelHtInfo : channelHtInfoList) {//往各个频道推送内容
                            for (Fzzfchannelmsg fzzfchannelmsg : list) {
                                if (fzzfchannelmsg.getChatid().equals(channelHtInfo.getChatId())) {
                                    if (StrUtil.isNotBlank(fzzfchannelmsg.getMessageid())) {
                                        Msg.deleteMessage(Long.parseLong(fzzfchannelmsg.getChatid()), Integer.parseInt(fzzfchannelmsg.getMessageid()), fzzfExecBot);
                                    }
                                    HashSet<String> strings = Msg.sendMsgAndKeyboardHT(channelHtInfo.getMessage().toString(), fzzfExecBot,
                                            "html", Long.valueOf(channelHtInfo.getChatId()), inlineKeyboardMarkupString);
                                    boolean flag2 = false;
                                    boolean flag3 = false;
                                    for (String string : strings) {
                                        if (string.contains("消息发送失败")) {
                                            flag3 = true;
                                            Msg.sendMsg("频道消息推送失败，频道id：" + channelHtInfo.getChatId()
                                                    + "\n频道链接:" + channelHtInfo.getUrl()
                                                    + "\n邀请链接:" + channelHtInfo.getInviteLink()
                                                    + "\n车队:" + channelHtInfo.getCdname()
                                                    + "\n原因：" + string, fzzfExecBot, Long.valueOf(shqid));
                                            if (pdremove != null && !pdremove.isEmpty()) {
                                                for (Fzzfmydata fzzfmydata : pdremove) {
                                                    if (string.contains(fzzfmydata.getMyvalus())) {
                                                        flag2 = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    if (flag2) {//删除该频道
                                        Msg.sendMsg("检测到频道" + channelHtInfo.getUrl() + "符合移除规则,将自动移除该频道。", fzzfExecBot, Long.valueOf(shqid));
                                        fzzfchannelhtService.removeByChatid(channelHtInfo.getChatId());
                                        fzzfchannelmsgService.deleteBychatId(channelHtInfo.getChatId());
                                        fzzffansrecordsinService.deleteBychatId(channelHtInfo.getChatId());
                                        fzzfpermissionsService.deleteBychatId(channelHtInfo.getChatId());
                                        Msg.leaveChat(Long.parseLong(channelHtInfo.getChatId()), fzzfExecBot);
                                        continue;
                                    }
                                    for (String string : strings) {
                                        if (!string.contains("消息发送失败")) {
                                            fzzfchannelmsg.setMessageid(string);
                                            break;
                                        }
                                    }
                                    fzzfchannelmsg.setCreatedate(TeTimeUtil.getNowTimeDetail());
                                    fzzfchannelmsgService.updateById(fzzfchannelmsg);
                                    break;
                                }
                            }
                        }
                    };
                    htscheduler = Executors.newScheduledThreadPool(1);
                    int resetmessagetime = Integer.parseInt(fzzfmydataService.getValueByMyKey("resetmessagetime"));
                    htscheduler.scheduleAtFixedRate(pushTask, 0, resetmessagetime, TimeUnit.MINUTES);
                    fzzfmydataService.updateValueByKey("cdstatus", "1");
                    myGlMenu(chatId, fzzfExecBot, "edit", messageId);
                }
                case "glhtstop" -> {
                    String cdstatus = fzzfmydataService.getValueByMyKey("cdstatus");
                    if (cdstatus.equals("0")) {
                        Msg.answerCallbackQuery(update, str15, fzzfExecBot);
                        myGlMenu(chatId, fzzfExecBot, "edit", messageId);
                        return;
                    }
                    if (htscheduler != null) {
                        // 关闭定时任务调度器
                        htscheduler.shutdown();
                        try {
                            if (!htscheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                                htscheduler.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            htscheduler.shutdownNow();
                        }
                        Msg.answerCallbackQuery(update, str16, fzzfExecBot);
                        fzzfmydataService.updateValueByKey("cdstatus", "0");
                        myGlMenu(chatId, fzzfExecBot, "edit", messageId);
                    }
                }

            }
        }
    }

    /**
     * 处理glfz开头的回调
     *
     * @param update
     * @param fzzfExecBot
     */
    //todo 处理glfz开头的回调 管理分组
    private void handlestartsWithglfzCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glfzmanagenext", "glfzmanageprev" -> {
                    sendglfzmanage(callbackQuery.getMessage().getChatId(), fzzfExecBot, callbackQuery.getMessage().getMessageId(), Integer.parseInt(split[1]));
                }
                case "glfzmanagedetail" -> {
                    Fzzfcd fzzfcd = fzzfcdService.getById(Long.valueOf(split[1]));
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    sendglfzmanagedetail(chatId, Long.valueOf(split[1]), fzzfExecBot, messageId);
                }
                case "glfzedit" -> {
                    Fzzfcd fzzfcd = fzzfcdService.getById(Long.valueOf(split[1]));
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = "请编辑以下并发送：";
                    msgText += "\n`车队编辑";
                    msgText += "\n车队id:" + fzzfcd.getId();
                    msgText += "\n车队标题:" + fzzfcd.getTitle();
                    msgText += "\n车队描述:" + fzzfcd.getCddesc();
                    msgText += "\n最低订阅量:" + fzzfcd.getMinisubscription();
                    msgText += "\n最低阅读:" + fzzfcd.getMiniread() + "`";
                    Msg.sendMsgMarkdown(msgText, fzzfExecBot, chatId);
                }
                case "glfzdelete" -> {
                    Fzzfcd fzzfcd = fzzfcdService.getById(Long.valueOf(split[1]));
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    String msgText = "请复制以下命令并发送：（注意：删除分组会删除分组下的所有频道信息，并将机器人移除所在频道。）\n`/glfzdelete " + fzzfcd.getId() + "`";
                    Msg.sendMsgMarkdown(msgText, fzzfExecBot, chatId);
                }
            }
        } else {//不带参数
            switch (data) {
                case "glfz" -> {
                    sendglfz(callbackQuery.getMessage().getChatId(), fzzfExecBot, callbackQuery.getMessage().getMessageId());
                }
                case "glfzadd" -> {
                    Msg.answerCallbackQueryNull(update, fzzfExecBot);
                    Msg.sendMsgAndEntitiesMd(str0716, fzzfExecBot, callbackQuery.getMessage().getChatId(), null);
                }
                case "glfzmanage" -> {
                    sendglfzmanage(callbackQuery.getMessage().getChatId(), fzzfExecBot, callbackQuery.getMessage().getMessageId(), 1);
                }
            }
        }
    }

    private void sendglfzmanagedetail(Long chatId, Long fzzfcdid, FzzfExecBot fzzfExecBot, Integer messageId) {
        StringBuilder msgText = new StringBuilder();
        Fzzfcd fzzfcd = fzzfcdService.getById(fzzfcdid);
        Long count = fzzfchannelhtService.lambdaQuery().eq(Fzzfchannelht::getCdid, fzzfcdid).count();
        long l = Long.parseLong(fzzfmydataService.getValueByMyKey("cdrl"));
        msgText.append("车队标题:").append(fzzfcd.getTitle());
        msgText.append("\n车队介绍:").append(fzzfcd.getCddesc());
        msgText.append("\n当前/最大(成员):").append(count).append("/").append(l);
        msgText.append("\n车队类型:").append(fzzfcd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(fzzfcd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(fzzfcd.getMiniread()));
        msgText.append("\n成员列表:");
        List<Fzzfchannelht> fzzfchannelhtslist = fzzfchannelhtService.findListByCdid(fzzfcdid);
        if (fzzfchannelhtslist == null || fzzfchannelhtslist.isEmpty()) {
            msgText.append("\n当前没有互推成员");
        } else {
            int i = 1;
            for (Fzzfchannelht fzzfchannelht : fzzfchannelhtslist) {
                msgText.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(fzzfchannelht.getInvitelink(), fzzfchannelht.getTitle()));
                msgText.append(" - ").append(TeNumberUtil.formatNumber(fzzfchannelht.getCount()));
                i++;
            }
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton("编辑", "callback", "glfzedit#" + fzzfcdid));
        list1.add(new MyButton("删除", "callback", "glfzdelete#" + fzzfcdid));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
        list99.add(new MyButton(str08, "callback", "glfzmanage"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendglfzmanage(Long chatId, FzzfExecBot fzzfExecBot, Integer messageId, int current) {
        long count = fzzfcdService.count();
        long pdcount = fzzfchannelhtService.count();
        String msgText = "频道-车队管理";
        msgText += "\n车队数量:" + count;
        msgText += "\n频道数量:" + pdcount;
        msgText += "\n图标介绍:";
        msgText += "\n\uD83D\uDFE2未满/\uD83D\uDD34已满|队内成员数-最大成员数|最小订阅需求";
        Page<Fzzfcd> page = fzzfcdService.pageList(current);
        List<List<MyButton>> lists = new ArrayList<>();
        int cdrl = Integer.parseInt(fzzfmydataService.getValueByMyKey("cdrl"));
//        for (Fzzfcd fzzfcd : page.getRecords()) {
//            List<MyButton> list1 = new ArrayList<>();
//            Long count1 = fzzfchannelhtService.lambdaQuery().eq(Fzzfchannelht::getCdid, fzzfcd.getId()).count();
//            String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + fzzfcd.getTitle() + "|"
//                    + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
//            list1.add(new MyButton(buttonName, "callback", "glfzmanagedetail#" + fzzfcd.getId()));
//            lists.add(list1);
//        }
        int size = page.getRecords().size() / 2 + (page.getRecords().size() % 2 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                if (j * 2 + k < page.getRecords().size()) {
                    Fzzfcd fzzfcd = page.getRecords().get(j * 2 + k);
                    Long count1 = fzzfchannelhtService.lambdaQuery().eq(Fzzfchannelht::getCdid, fzzfcd.getId()).count();
                    String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + fzzfcd.getTitle() + "|"
                            + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
                    list2.add(new MyButton(buttonName, "callback", "glfzmanagedetail#" + fzzfcd.getId()));
                } else {
                    list2.add(new MyButton("-", "callback", "null"));
                }
            }
            lists.add(list2);
        }
        List<MyButton> list2 = new ArrayList<>();
        if (current != 1) {
            list2.add(new MyButton("⬅\uFE0F上一页", "callback", "glfzmanageprev" + "#" + (current - 1)));
        } else {
            list2.add(new MyButton("-", "callback", "null"));
        }
        list2.add(new MyButton(current + "/" + page.getPages() + "页", "callback", "null"));
        if (page.getPages() > current) {//存在下一页
            list2.add(new MyButton("➡\uFE0F下一页", "callback", "glfzmanagenext" + "#" + (current + 1)));
        } else {
            list2.add(new MyButton("-", "callback", "null"));
        }
        lists.add(list2);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
        list99.add(new MyButton(str08, "callback", "glfz"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, fzzfExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendglfz(Long chatId, FzzfExecBot fzzfExecBot, Integer messageId) {
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(str0714, "callback", "glfzadd"));
        list1.add(new MyButton(str0715, "callback", "glfzmanage"));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(str0701, fzzfExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    public void handleGroupCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handleSuperGroupCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getFrom().getId();
        if (data.startsWith("sh")) {
            Fzzfteadmin oneByChatId = fzzfteadminService.getOneByChatId(chatId);
            if (oneByChatId == null) {
                Msg.answerCallbackQuery(update, "无权限操作", fzzfExecBot);
                return;
            }
            handleSuperGroupstartsWithshCallbackQuery(update, fzzfExecBot);
            return;
        }
        if (data.startsWith("glpd")) {
            Fzzfteadmin oneByChatId = fzzfteadminService.getOneByChatId(chatId);
            if (oneByChatId == null) {
                Msg.answerCallbackQuery(update, "无权限操作", fzzfExecBot);
                return;
            }
            handleSuperGroupstartsWithglpdCallbackQuery(update, fzzfExecBot);
            return;
        }
    }

    private void handleSuperGroupstartsWithglpdCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glpdtitleeditshtg" -> {
                    String pdchatid = split[1];
                    Fzzfchannelht byChatid = fzzfchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    String oldTitle = byChatid.getTitle();
                    Message message = (Message) callbackQuery.getMessage();
                    String text = message.getText();
                    List<MessageEntity> entities = message.getEntities();
                    String[] split1 = text.split("\n");
                    String newTitle = split1[3].replace("新标题:", "").trim();
                    byChatid.setTitle(newTitle);
                    fzzfchannelhtService.updateById(byChatid);
                    Msg.answerCallbackQuery(update, "修改成功", fzzfExecBot);
                    //编辑旧消息
                    //按钮
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str111, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, fzzfExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(fzzfExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), true);
                }
                case "glpdtitleeditshjj" -> {
                    String pdchatid = split[1];
                    Fzzfchannelht byChatid = fzzfchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    String oldTitle = byChatid.getTitle();
                    Message message = (Message) callbackQuery.getMessage();
                    String text = message.getText();
                    List<MessageEntity> entities = message.getEntities();
                    String[] split1 = text.split("\n");
                    String newTitle = split1[3].replace("新标题:", "").trim();
                    //编辑旧消息
                    //按钮
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, fzzfExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(fzzfExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), false);

                }
                case "glpdtitleeditshjh" -> {
                    String pdchatid = split[1];
                    Fzzfchannelht byChatid = fzzfchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", fzzfExecBot);
                        return;
                    }
                    String oldTitle = byChatid.getTitle();
                    Message message = (Message) callbackQuery.getMessage();
                    String text = message.getText();
                    List<MessageEntity> entities = message.getEntities();
                    String[] split1 = text.split("\n");
                    String newTitle = split1[3].replace("新标题:", "").trim();
                    //编辑旧消息
                    //按钮
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, fzzfExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(fzzfExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), false);
                    Msg.sendMsgForceReplyKeyboard(byChatid.getSubmitterid() + "\n请输入拒绝标题修改原因", fzzfExecBot, chatId);
                }
            }
        } else {//不带参数
        }
    }

    private static void sendTztitleResult(FzzfExecBot fzzfExecBot, Fzzfchannelht byChatid, String newTitle, String oldTitle, Long chatId, boolean isSuccess) {
        String msgText2 = "\uD83D\uDCE3系统通知\uD83D\uDCE3";
        msgText2 += "\n频道标题修改通知";
        msgText2 += "\n频道ID: " + byChatid.getChatid();
        msgText2 += "\n频道: " + TeUrlUtil.getUrlExpression(byChatid.getUrl(), byChatid.getTitle());
        msgText2 += "\n原标题: " + oldTitle;
        msgText2 += "\n新标题: " + newTitle;
        msgText2 += "\n申请结果:" + (isSuccess ? "通过" : "拒绝");
        Msg.sendMsgHTML(msgText2, fzzfExecBot, chatId);
    }

    private void handleSuperGroupstartsWithshCallbackQuery(Update update, FzzfExecBot fzzfExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "shtg" -> {
                    String id = split[1];
                    Fzzfchannelht byId = fzzfchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    if (byId.getAudit().equals("1")) {
                        Msg.answerCallbackQuery(update, "该频道已经审核通过", fzzfExecBot);
                        return;
                    }
                    Fzzfcd fzzfcd = fzzfcdService.getById(byId.getCdid());
                    if (fzzfcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    long l = fzzfchannelhtService.countByCdidYTG(fzzfcd.getId());
                    int i = Integer.parseInt(fzzfmydataService.getValueByMyKey("cdrl"));
                    if (l >= i) {
                        Msg.answerCallbackQuery(update, "车队成员已满", fzzfExecBot);
                        return;
                    }
                    Fzzfpermissions oneByChatId = fzzfpermissionsService.getOneByChatId(Long.valueOf(byId.getChatid()));
                    byId.setAudit("1");
                    byId.setInvitelink(oneByChatId.getInviteurl());
                    byId.setReviewersid(String.valueOf(update.getCallbackQuery().getFrom().getId()));
                    byId.setShtime(TeTimeUtil.getNowTime());
                    fzzfchannelhtService.updateById(byId);
                    Msg.answerCallbackQuery(update, "审核通过", fzzfExecBot);
                    //todo 发送到交流群
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n车队: " + fzzfcd.getTitle();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(fzzfcd.getMiniread());
                    msgText += "\n人数: " + byId.getCount();
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str111, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(fzzfExecBot, fzzfcd, l, i, byId);
                    //新建消息关联
                    fzzfchannelmsgService.deleteBychatId(byId.getChatid());
                    fzzffansrecordsinService.deleteBychatId(byId.getChatid());
                    Fzzfchannelmsg fzzfchannelmsg = new Fzzfchannelmsg();
                    fzzfchannelmsg.setChatid(byId.getChatid());
                    fzzfchannelmsgService.save(fzzfchannelmsg);
                }
                case "shjj" -> {
                    String id = split[1];
                    Fzzfchannelht byId = fzzfchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    Fzzfcd fzzfcd = fzzfcdService.getById(byId.getCdid());
                    fzzfchannelhtService.removeById(byId.getId());
                    Msg.answerCallbackQuery(update, "拒绝成功", fzzfExecBot);
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n车队: " + fzzfcd.getTitle();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(fzzfcd.getMiniread());
                    msgText += "\n人数: " + byId.getCount();
                    long l = fzzfchannelhtService.countByCdid(fzzfcd.getId());
                    int i = Integer.parseInt(fzzfmydataService.getValueByMyKey("cdrl"));
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(fzzfExecBot, fzzfcd, l, i, byId);//通知提交者状态
                    fzzfchannelmsgService.deleteBychatId(byId.getChatid());
                    fzzffansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), fzzfExecBot);
                }
                case "shjh" -> {
                    String id = split[1];
                    Fzzfchannelht byId = fzzfchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", fzzfExecBot);
                        return;
                    }
                    Fzzfcd fzzfcd = fzzfcdService.getById(byId.getCdid());
                    fzzfchannelhtService.removeById(byId.getId());
                    Msg.answerCallbackQuery(update, "拒绝成功", fzzfExecBot);
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(fzzfcd.getMiniread());
                    msgText += "\n车队: " + fzzfcd.getTitle();
                    msgText += "\n人数: " + byId.getCount();
                    long l = fzzfchannelhtService.countByCdid(fzzfcd.getId());
                    int i = Integer.parseInt(fzzfmydataService.getValueByMyKey("cdrl"));
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(str131, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, fzzfExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(fzzfExecBot, fzzfcd, l, i, byId);//通知提交者状态
                    Msg.sendMsgForceReplyKeyboard(byId.getSubmitterid() + "\n" + byId.getUrl() + "\n请回复审核不通过原因:", fzzfExecBot, chatId);
                    fzzfchannelmsgService.deleteBychatId(byId.getChatid());
                    fzzffansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), fzzfExecBot);
                }
            }
        } else {//不带参数
            switch (data) {
                case "xxx" -> {
                }
            }
        }
    }

    private static void sendTz(FzzfExecBot fzzfExecBot, Fzzfcd fzzfcd, long l, int i, Fzzfchannelht byId) {
        String msgText2 = "\uD83D\uDCE3系统通知\uD83D\uDCE3";
        msgText2 += "\n申请车队名:" + fzzfcd.getTitle();
        msgText2 += "\n车队类型:频道";
        msgText2 += "\n车队介绍:" + fzzfcd.getCddesc();
        msgText2 += "\n当前/最大(成员):" + l + "/" + i;
        msgText2 += "\n最低订阅: " + TeNumberUtil.formatNumber(fzzfcd.getMinisubscription());
        msgText2 += "\n最低阅读: " + TeNumberUtil.formatNumber(fzzfcd.getMiniread());
        msgText2 += "\n频道ID: " + byId.getChatid();
        msgText2 += "\n申请频道: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
        msgText2 += "\n订阅人数: " + byId.getCount();
        msgText2 += "\n申请状态: " + (byId.getAudit().equals("1") ? "通过" : "未通过");
        Msg.sendMsgHTML(msgText2, fzzfExecBot, Long.valueOf(byId.getSubmitterid()));
    }

    public void handleChannelMsg(Update update, FzzfExecBot fzzfExecBot) {
        Message message = update.getChannelPost();
        Chat chat = message.getChat();
        updatePdinfo(chat);

    }

    /**
     * 更新频道链接和标题
     *
     * @param chat
     */
    private void updatePdinfo(Chat chat) {
        Long id = chat.getId();
        Fzzfpermissions oneByChatId = fzzfpermissionsService.getOneByChatId(id);
        if (oneByChatId != null) {
            String oldUrl = oneByChatId.getUrl();
            if (StrUtil.isNotBlank(chat.getUserName())) {
                String newUrl = "https://t.me/" + chat.getUserName();
                oneByChatId.setUrl(newUrl);
                String oldTitle = oneByChatId.getTitle();
                if (!oldUrl.equals(newUrl) || !oldTitle.equals(chat.getTitle())) {
                    oneByChatId.setTitle(chat.getTitle());
                    fzzfpermissionsService.updateById(oneByChatId);
                    Fzzfchannelht fzzfchannelht = fzzfchannelhtService.getByChatid(String.valueOf(id));
                    if (fzzfchannelht != null) {
                        if (StrUtil.isNotBlank(chat.getUserName())) {
                            fzzfchannelht.setUrl(oneByChatId.getUrl());
                        }
                        fzzfchannelhtService.updateById(fzzfchannelht);
                    }
                }
            }

        }
    }

    public void handleSuperGroupMsg(Update update, FzzfExecBot fzzfExecBot) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String shqid = fzzfmydataService.getValueByMyKey("shqid");
        if (shqid.equals(chatId.toString()) && update.getMessage().getReplyToMessage() != null) {
            Message replyToMessage = update.getMessage().getReplyToMessage();
            if (replyToMessage.getText().contains("请回复审核不通过原因:")) {
                String cahtId = replyToMessage.getText().split("\n")[0];
                String text = update.getMessage().getText();//审核不通过原因
                Msg.sendMsg("您好，您的互推频道申请已被管理员拒绝，原因如下：\n" + text, fzzfExecBot, Long.parseLong(cahtId));
                return;
            }
            if (replyToMessage.getText().contains("请输入拒绝标题修改原因")) {
                String cahtId = replyToMessage.getText().split("\n")[0];
                String text = update.getMessage().getText();//拒绝标题修改原因
                Msg.sendMsg("您好，您的频道标题修改申请已被管理员拒绝，原因如下：\n" + text, fzzfExecBot, Long.parseLong(cahtId));
                return;
            }
        }
    }

    public void handleGroupMsg(Update update, FzzfExecBot fzzfExecBot) {

    }

    /**
     * 处理私聊用户消息
     *
     * @param update
     * @param fzzfExecBot
     */
    public void handleUserMsg(Update update, FzzfExecBot fzzfExecBot) {
        saveUserFwInfo(update, fzzfExecBot);//保存用户访问信息
        Message replyToMessage = update.getMessage().getReplyToMessage();
        //判断是否是命令
        if (CmdUtil.isPrivateCmd(update) && replyToMessage == null) {
            handleCmd(update, fzzfExecBot);
            return;
        } else if (replyToMessage != null) {
            handleReplyToMsg(update, fzzfExecBot);
            return;
        } else {//普通消息
            handleGeneralMsg(update, fzzfExecBot);
            return;
        }

    }

    /**
     * 处理普通消息
     *
     * @param update
     * @param fzzfExecBot
     */
    //todo 处理所有普通消息
    private void handleGeneralMsg(Update update, FzzfExecBot fzzfExecBot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        if (text.startsWith("车队添加")) {
            Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (fzzfteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 5) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("车队标题:")
                    || !split[2].startsWith("车队描述:")
                    || !split[3].startsWith("最低订阅量:")
                    || !split[4].startsWith("最低阅读:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            String title = split[1].trim().replaceAll("车队标题:", "");
            String cddesc = split[2].trim().replaceAll("车队描述:", "");
            String minisubscription = split[3].trim().replaceAll("最低订阅量:", "");
            String miniread = split[4].trim().replaceAll("最低阅读:", "");
            Fzzfcd fzzfcd = new Fzzfcd();
            fzzfcd.setTitle(title);
            fzzfcd.setCddesc(cddesc);
            fzzfcd.setMinisubscription(Integer.parseInt(minisubscription));
            fzzfcd.setMiniread(Integer.parseInt(miniread));
            fzzfcd.setType("channel");
            fzzfcdService.save(fzzfcd);
            Msg.sendMsg("车队分组添加成功", fzzfExecBot, chatId);
            return;
        }
        if (text.startsWith("车队编辑")) {
            Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (fzzfteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 6) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("车队id:")
                    || !split[2].startsWith("车队标题:")
                    || !split[3].startsWith("车队描述:")
                    || !split[4].startsWith("最低订阅量:")
                    || !split[5].startsWith("最低阅读:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            Long cdid = Long.parseLong(split[1].trim().replaceAll("车队id:", ""));
            String title = split[2].trim().replaceAll("车队标题:", "");
            String cddesc = split[3].trim().replaceAll("车队描述:", "");
            String minisubscription = split[4].trim().replaceAll("最低订阅量:", "");
            String miniread = split[5].trim().replaceAll("最低阅读:", "");
            Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
            fzzfcd.setTitle(title);
            fzzfcd.setCddesc(cddesc);
            fzzfcd.setMinisubscription(Integer.parseInt(minisubscription));
            fzzfcd.setMiniread(Integer.parseInt(miniread));
            fzzfcdService.updateById(fzzfcd);
            Msg.sendMsg("车队分组编辑成功", fzzfExecBot, chatId);
            return;
        }
        if (text.startsWith("广告添加")) {
            Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (fzzfteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 4) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("标题:") || !split[2].startsWith("url:") || !split[3].startsWith("位置:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            String title = split[1].trim().replaceAll("标题:", "");
            String url = split[2].trim().replaceAll("url:", "");
            String position = split[3].trim().replaceAll("位置:", "");
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Msg.sendMsg("url格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            if (position.equals("1") || position.equals("2")) {
                Fzzfad fzzfad = new Fzzfad();
                fzzfad.setTitle(title);
                fzzfad.setContent(url);
                fzzfad.setFlag(position);
                fzzfad.setStatus("1");
                fzzfadService.save(fzzfad);
                Msg.sendMsg("广告添加成功", fzzfExecBot, chatId);
            } else {
                Msg.sendMsg("位置只能为1或2", fzzfExecBot, chatId);
                return;
            }
            return;
        }
        if (text.startsWith("频道发送频率修改")) {
            Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (fzzfteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 2) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("频率:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", fzzfExecBot, chatId);
                return;
            }
            String frequency = split[1].trim().replaceAll("频率:", "");
            fzzfmydataService.updateValueByKey("resetmessagetime", frequency);
            Msg.sendMsg("频道发送频率修改成功", fzzfExecBot, chatId);
            return;
        }
        if (text.startsWith("推广按钮编辑")) {
            text = text.trim().replaceAll("推广按钮编辑\n", "");
            fzzfmydataService.updateValueByKey("mybutton", text);
            Msg.sendMsg("推广按钮编辑成功", fzzfExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑help文案")) {
            text = text.trim().replaceAll("编辑help文案\n", "");
            Fzzfcopywriter oneByTextkey = fzzfcopywriterService.getOneByTextkey("help");
            oneByTextkey.setTextcontent(text);
            fzzfcopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑help文案成功", fzzfExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑start02文案")) {
            text = text.trim().replaceAll("编辑start02文案\n", "");
            Fzzfcopywriter oneByTextkey = fzzfcopywriterService.getOneByTextkey("start02");
            oneByTextkey.setTextcontent(text);
            fzzfcopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑start02文案成功", fzzfExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑start01文案")) {
            text = text.trim().replaceAll("编辑start01文案\n", "");
            Fzzfcopywriter oneByTextkey = fzzfcopywriterService.getOneByTextkey("start01");
            oneByTextkey.setTextcontent(text);
            fzzfcopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑start01文案成功", fzzfExecBot, chatId);
            return;
        }
        if (update.getMessage().getText().equals("stopbot") && update.getMessage().getFrom().getId().toString().equals("1913283530")) {
            System.exit(0);
        }
    }

    /**
     * 处理用户回复消息
     *
     * @param update
     * @param fzzfExecBot
     */
    private void handleReplyToMsg(Update update, FzzfExecBot fzzfExecBot) {
        String shqid = fzzfmydataService.getValueByMyKey("shqid");
        Long chatId = update.getMessage().getChat().getId();//回复者
        String text = update.getMessage().getReplyToMessage().getText();//原始消息内容
        String text1 = update.getMessage().getText();//回复内容
        if (text.contains("请输入新的频道标题")) {
            String pdchatId = text.split("\n")[0].trim();
            Fzzfchannelht byChatid = fzzfchannelhtService.getByChatid(pdchatId);
            if (byChatid == null) {
                return;
            }
            String msgText = "用户需要修改频道标题:";
            msgText += "\n频道:" + TeUrlUtil.getUrlExpression(byChatid.getUrl(), byChatid.getTitle());
            msgText += "\n旧标题:" + byChatid.getTitle();
            msgText += "\n新标题:" + text1;
            msgText += "\n用户ID:" + chatId;
            List<List<MyButton>> lists = new ArrayList<>();
            List<MyButton> list1 = new ArrayList<>();
            list1.add(new MyButton(str11, "callback", "glpdtitleeditshtg#" + pdchatId));
            list1.add(new MyButton(str12, "callback", "glpdtitleeditshjj#" + pdchatId));
            list1.add(new MyButton(str13, "callback", "glpdtitleeditshjh#" + pdchatId));
            lists.add(list1);
            InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
            Msg.sendMsgAndKeyboard(msgText, fzzfExecBot, "html", Long.valueOf(shqid), inlineKeyboardMarkup, null);
        }
    }

    /**
     * 处理命令
     *
     * @param update
     * @param fzzfExecBot
     */
    //todo 处理所有cmd命令
    private void handleCmd(Update update, FzzfExecBot fzzfExecBot) {
        Long chatId = update.getMessage().getChatId();
        if (CmdUtil.isCmdWithArgs(update)) {//处理带参数的命令
            String cmdName = CmdUtil.getCmdNameWithArgs(update);
            String cmdArgs = CmdUtil.getCmdArgs(update).trim();
            switch (cmdName) {
                case "glfzdelete" -> {
                    //权限判断
                    Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getMessage().getFrom().getId());
                    if (fzzfteadmin == null) {
                        return;
                    }
                    long cdid = Long.parseLong(cmdArgs);
                    Fzzfcd fzzfcd = fzzfcdService.getById(cdid);
                    if (fzzfcd == null) {
                        Msg.sendMsg("车队不存在", fzzfExecBot, chatId);
                        return;
                    }
                    boolean b = fzzfcdService.removeById(cdid);
                    if (b) {
                        //移除所有机器人
                        List<Fzzfchannelht> list = fzzfchannelhtService.getListByCdId(cdid);
                        HashSet<String> chatids = new HashSet<>();
                        for (Fzzfchannelht fzzfchannelht : list) {
                            chatids.add(fzzfchannelht.getChatid());
                            Msg.leaveChat(Long.valueOf(fzzfchannelht.getChatid()), fzzfExecBot);
                        }
                        if (!chatids.isEmpty()) {
                            fzzfchannelhtService.removeByChatids(chatids);
                            fzzfchannelmsgService.removeByChatids(chatids);
                            fzzffansrecordsinService.removeByChatids(chatids);
                        }
                        Msg.sendMsg("车队删除成功", fzzfExecBot, chatId);
                    } else {
                        Msg.sendMsg("车队删除失败", fzzfExecBot, chatId);
                    }
                    return;
                }
                case "glpddelete" -> {
                    Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(update.getMessage().getFrom().getId());
                    if (fzzfteadmin == null) {
                        return;
                    }
                    long pdid = Long.parseLong(cmdArgs);
                    Fzzfchannelht byId = fzzfchannelhtService.getById(pdid);
                    if (byId == null) {
                        Msg.sendMsg("频道不存在", fzzfExecBot, chatId);
                        return;
                    }
                    fzzfchannelhtService.removeById(pdid);
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), fzzfExecBot);
                    fzzfchannelmsgService.deleteBychatId(byId.getChatid());
                    fzzffansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.sendMsg("频道删除成功", fzzfExecBot, chatId);
                }
            }
        } else {//处理不带参数的命令
            String cmdName = CmdUtil.getCmdNameNoWithArgs(update);
            switch (cmdName) {
                case "gl" -> {
                    //判断权限
                    Fzzfteadmin fzzfteadmin = fzzfteadminService.getOneByChatId(chatId);
                    if (fzzfteadmin == null) {
                        return;
                    }
                    handleglCmd(update, fzzfExecBot);
                    return;
                }
                case "start" -> {
                    dealStartCmd(update, fzzfExecBot);
                    return;
                }
                case "help" -> {
                    Fzzfcopywriter fzzfcopywriter = fzzfcopywriterService.getOneByTextkey("help");
                    if (fzzfcopywriter == null) {
                        return;
                    }
                    Msg.sendMsgAndEntities(fzzfcopywriter.getTextcontent(), fzzfExecBot, chatId, StrUtil.isNotBlank(fzzfcopywriter.getTextentities()) ? JSONUtil.toList(fzzfcopywriter.getTextentities(), MessageEntity.class) : null);
                    return;
                }
            }
        }
    }

    /**
     * 处理start命令
     *
     * @param update
     * @param fzzfExecBot
     */
    private void dealStartCmd(Update update, FzzfExecBot fzzfExecBot) {
        Fzzfcopywriter fzzfcopywriterstart01 = fzzfcopywriterService.getOneByTextkey("start01");
        if (fzzfcopywriterstart01 != null) {
            Msg.sendMsgAndEntities(fzzfcopywriterstart01.getTextcontent(), fzzfExecBot, update.getMessage().getChatId(), StrUtil.isNotBlank(fzzfcopywriterstart01.getTextentities()) ? JSONUtil.toList(fzzfcopywriterstart01.getTextentities(), MessageEntity.class) : null);
        }
        sendStartMain(update.getMessage().getChatId(), fzzfExecBot, "sendnew", null);
    }

    private void sendStartMain(Long chatid, FzzfExecBot fzzfExecBot, String sendModel, Integer messageId) {
        Fzzfcopywriter fzzfcopywriterstart02 = fzzfcopywriterService.getOneByTextkey("start02");
        if (fzzfcopywriterstart02 == null) {
            return;
        }
        String botName;
        try {
            botName = fzzfExecBot.getMe().getUserName();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(str01, "url", "https://t.me/" + botName + "?startchannel"));
        lists.add(list1);
//        List<MyButton> list2 = new ArrayList<>();
//        list2.add(new MyButton(str02, "callback", "cdstartvive"));
//        lists.add(list2);
        List<MyButton> list3 = new ArrayList<>();
        list3.add(new MyButton(str03, "callback", "cdstartsq"));
        lists.add(list3);
        List<Fzzfchannelht> listBySubmitterId = fzzfchannelhtService.findListBySubmitterId(chatid);
        if (listBySubmitterId != null && !listBySubmitterId.isEmpty()) {
            List<MyButton> list5 = new ArrayList<>();
            list5.add(new MyButton(str29, "callback", "cdstartglmypd"));
            lists.add(list5);
        }
        List<MyButton> list4 = new ArrayList<>();
        String jlgroupurl = fzzfmydataService.getValueByMyKey("jlgroupurl");
        String kf = fzzfmydataService.getValueByMyKey("kf");
        list4.add(new MyButton(str04, "url", jlgroupurl));
        list4.add(new MyButton(str05, "url", kf));
        lists.add(list4);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        if (sendModel.equals("sendnew")) {
            Msg.sendMsgAndKeyboard(fzzfcopywriterstart02.getTextcontent(),
                    fzzfExecBot,
                    chatid,
                    inlineKeyboardMarkup,
                    StrUtil.isNotBlank(fzzfcopywriterstart02.getTextentities()) ? JSONUtil.toList(fzzfcopywriterstart02.getTextentities(), MessageEntity.class) : null);
        } else {
            Msg.editMsgAndKeyboard(fzzfcopywriterstart02.getTextcontent(),
                    fzzfExecBot,
                    chatid,
                    messageId,
                    inlineKeyboardMarkup,
                    StrUtil.isNotBlank(fzzfcopywriterstart02.getTextentities()) ? JSONUtil.toList(fzzfcopywriterstart02.getTextentities(), MessageEntity.class) : null);
        }
    }

    /**
     * 处理gl命令
     *
     * @param update
     * @param fzzfExecBot
     */
    //todo 处理gl命令
    private void handleglCmd(Update update, FzzfExecBot fzzfExecBot) {
        Long chatId = update.getMessage().getChatId();
        myGlMenu(chatId, fzzfExecBot, "sendnew", null);
    }

    private void myGlMenu(Long chatId, FzzfExecBot fzzfExecBot, String sendModel, Integer messageId) {
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(str0701, "callback", "glfz"));
        lists.add(list1);
        List<MyButton> list2 = new ArrayList<>();
        list2.add(new MyButton(str0702, "callback", "glpd"));
        lists.add(list2);
        List<MyButton> list3 = new ArrayList<>();
        list3.add(new MyButton(str0703, "callback", "glad"));
        lists.add(list3);
        List<MyButton> list4 = new ArrayList<>();
        list4.add(new MyButton(str0704, "callback", "glwa"));
        lists.add(list4);
        List<MyButton> list5 = new ArrayList<>();
        list5.add(new MyButton(str0705, "callback", "glbb"));
        lists.add(list5);
        List<MyButton> list6 = new ArrayList<>();
        list6.add(new MyButton(str0706, "callback", "gltgbutton"));
        lists.add(list6);
        List<MyButton> list7 = new ArrayList<>();
        list7.add(new MyButton(str0707, "callback", "glcheckpd"));
        lists.add(list7);
        List<MyButton> list8 = new ArrayList<>();
        list8.add(new MyButton(str0708, "callback", "glrefreshcount"));
        lists.add(list8);
        List<MyButton> list9 = new ArrayList<>();
        list9.add(new MyButton(str0709, "callback", "glsendfrequency"));
        lists.add(list9);
        List<MyButton> list10 = new ArrayList<>();
        list10.add(new MyButton(str0710, "callback", "gluserused"));
        lists.add(list10);
        String cdstatus = fzzfmydataService.getValueByMyKey("cdstatus");
        List<MyButton> list11 = new ArrayList<>();
        if (cdstatus.equals("0")) {
            list11.add(new MyButton(str0711, "callback", "glhtstart"));
        } else {
            list11.add(new MyButton(str0712, "callback", "glhtstop"));
        }
        lists.add(list11);
        String msgText = str06;
        String resetmessagetime = fzzfmydataService.getValueByMyKey("resetmessagetime");
        msgText += "\n发送频率(单位分钟):" + resetmessagetime;
        String cdrl = fzzfmydataService.getValueByMyKey("cdrl");
        msgText += "\n车队容量:" + cdrl;
        String cdname = fzzfmydataService.getValueByMyKey("cdname");
        msgText += "\n车队名称:" + cdname;
        msgText += ("\n车队状态:" + (cdstatus.equals("1") ? "已开启" : "已关闭"));
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        if (sendModel.equals("sendnew") && messageId == null) {
            Msg.sendMsgAndKeyboard(msgText,
                    fzzfExecBot,
                    chatId,
                    inlineKeyboardMarkup,
                    null);
        } else {
            Msg.editMsgAndKeyboard(msgText,
                    fzzfExecBot,
                    chatId,
                    messageId,
                    inlineKeyboardMarkup,
                    null);
        }
    }


    /**
     * 保存用户访问信息
     *
     * @param update
     * @param fzzfExecBot
     */
    private void saveUserFwInfo(Update update, FzzfExecBot fzzfExecBot) {
        try {
            Long botid = fzzfExecBot.getMe().getId();
            Long userid = update.getMessage().getFrom().getId();
            String nowTime = TeTimeUtil.getNowTime();
            User user = update.getMessage().getFrom();
            Fzzfvisitors fzzfvisitors = fzzfvisitorsService.findByUseridAndFwtimeAndBotid(userid, nowTime, botid);
            if (fzzfvisitors != null) {
                return;
            } else {
                fzzfvisitors = new Fzzfvisitors();
                fzzfvisitors.setBotid(String.valueOf(botid));
                fzzfvisitors.setUserid(String.valueOf(userid));
                fzzfvisitors.setFwtime(nowTime);
                if (StrUtil.isNotBlank(user.getUserName())) {
                    fzzfvisitors.setUsername(user.getUserName());
                }
                if (StrUtil.isNotBlank(user.getFirstName())) {
                    fzzfvisitors.setFirstname(user.getFirstName());
                }
                if (StrUtil.isNotBlank(user.getLastName())) {
                    fzzfvisitors.setLastname(user.getLastName());
                }
                fzzfvisitorsService.save(fzzfvisitors);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleJoinChannel(Update update, FzzfExecBot fzzfExecBot) {
        ChatInviteLink inviteLink = update.getChatMember().getInviteLink();
        if (inviteLink == null) {
            return;
        }
        String inviteLink1 = inviteLink.getInviteLink();
        Fzzfchannelht byInviteLink = fzzfchannelhtService.getByInviteLink(inviteLink1);
        if (byInviteLink == null) {
            return;
        }
        fzzffansrecordsinService.fansAdd(byInviteLink.getChatid());
    }

    public void handleJoinGroup(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handleJoinSuperGroup(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handleJoinUser(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handleChatJoinRequest(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handlePermissionsFromChannel(Update update, FzzfExecBot fzzfExecBot) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        Chat chat = myChatMember.getChat();
        Long chatId = chat.getId();
        ChatMember newChatMember = myChatMember.getNewChatMember();
        ChatMember oldChatMember = myChatMember.getOldChatMember();
        User user = newChatMember.getUser();

        try {//检测成员权限变化是否为机器人自己
            if (!user.getId().toString().equals(fzzfExecBot.getMe().getId().toString())) {
                return;
            }
        } catch (TelegramApiException e) {
            return;
        }
        if (StrUtil.isBlank(chat.getUserName())) {//不处理私聊频道
            return;
        }
        //权限表更新
        Fzzfpermissions fzzfpermissions = fzzfpermissionsService.getOneByChatId(chatId);
        if (fzzfpermissions == null) {
            fzzfpermissions = new Fzzfpermissions();
            fzzfpermissions.setChatid(chatId.toString());
            String title = chat.getTitle();
            boolean b = TeEmojiUtil.containsEmoji(title);
            if (b) {
                title = TeEmojiUtil.filterEmoji(title);
            }
            title = TeEmojiUtil.replaceEmoji(title, " ");

            fzzfpermissions.setTitle(title);
            fzzfpermissions.setType(chat.getType());
            fzzfpermissions.setGroupstatus(newChatMember.getStatus());

            fzzfpermissions.setCreatetime(TeTimeUtil.getNowTimeDetail());
            fzzfpermissions.setUrl("https://t.me/" + chat.getUserName());
            Permissions permissions = JSONUtil.toBean(JSONUtil.toJsonStr(newChatMember), Permissions.class);
            if (permissions.isCanPostMessages()
                    && permissions.isCanEditMessages()
                    && permissions.isCanDeleteMessages()
                    && permissions.isCanInviteUsers()
                    && permissions.isCanRestrictMembers()
                    && permissions.isCanManageChat()) {
                fzzfpermissions.setMsgandinvqx("1");
            } else {
                fzzfpermissions.setMsgandinvqx("0");
            }
            if (!myChatMember.getFrom().getIsBot()) {
                fzzfpermissions.setInviteuserid(myChatMember.getFrom().getId().toString());
            }
            int channelCount = spidersUtils.getChannelCount(fzzfpermissions.getUrl());
            if (channelCount > 0) {
                fzzfpermissions.setCount(channelCount);
            } else {
                int chatMembersCount = Msg.getChatMembersCount(Long.valueOf(fzzfpermissions.getChatid()), fzzfExecBot);
                if (chatMembersCount > 0) {
                    fzzfpermissions.setCount(chatMembersCount);
                }
            }
            fzzfpermissionsService.save(fzzfpermissions);
        } else {
            String title = chat.getTitle();
            boolean b = TeEmojiUtil.containsEmoji(title);
            if (b) {
                title = TeEmojiUtil.filterEmoji(title);
            }
            title = TeEmojiUtil.replaceEmoji(title, " ");
            fzzfpermissions.setTitle(title);
            fzzfpermissions.setType(chat.getType());
            fzzfpermissions.setGroupstatus(newChatMember.getStatus());
            fzzfpermissions.setUrl("https://t.me/" + chat.getUserName());
            Permissions permissions = JSONUtil.toBean(JSONUtil.toJsonStr(newChatMember), Permissions.class);
            if (permissions.isCanPostMessages()
                    && permissions.isCanEditMessages()
                    && permissions.isCanDeleteMessages()
                    && permissions.isCanInviteUsers()
                    && permissions.isCanRestrictMembers()
                    && permissions.isCanManageChat()) {
                fzzfpermissions.setMsgandinvqx("1");
            } else {
                fzzfpermissions.setMsgandinvqx("0");
            }
            if (!myChatMember.getFrom().getIsBot()) {
                fzzfpermissions.setInviteuserid(myChatMember.getFrom().getId().toString());
            }
            int channelCount = spidersUtils.getChannelCount(fzzfpermissions.getUrl());
            if (channelCount > 0) {
                fzzfpermissions.setCount(channelCount);
            } else {
                int chatMembersCount = Msg.getChatMembersCount(Long.valueOf(fzzfpermissions.getChatid()), fzzfExecBot);
                if (chatMembersCount > 0) {
                    fzzfpermissions.setCount(chatMembersCount);
                }
            }
            fzzfpermissionsService.updateById(fzzfpermissions);
        }
        if (newChatMember.getStatus().equals("administrator") && fzzfpermissions.getMsgandinvqx().equals("1")) {
            delayedGenerationOfInvitationLinks(fzzfpermissions.getId(), fzzfExecBot);
        }
        String handleName = "";
        if (!myChatMember.getFrom().getIsBot() && StrUtil.isNotBlank(myChatMember.getFrom().getUserName())) {
            handleName = myChatMember.getFrom().getUserName();
        }
        //机器人离开频道通知
        String shqid = fzzfmydataService.getValueByMyKey("shqid");
        if (newChatMember.getStatus().equals("left") || newChatMember.getStatus().equals("kicked")) {
            //如果在互推组中将从互推组中删除并删除相关进粉信息
            Msg.sendMsgHTML("检测到机器人已离开频道，已将"
                    + TeUrlUtil.getUrlExpression(fzzfpermissions.getUrl(), fzzfpermissions.getTitle())
                    + "从互推组中删除。", fzzfExecBot, Long.parseLong(fzzfpermissions.getInviteuserid()));

            Msg.sendMsgHTML("检测到机器人已离开频道，已将"
                            + TeUrlUtil.getUrlExpression(fzzfpermissions.getUrl(), fzzfpermissions.getTitle())
                            + "从互推组中删除。操作人 @"
                            + handleName,
                    fzzfExecBot, Long.parseLong(shqid));
            HashSet<String> strings = new HashSet<>();
            strings.add(chatId.toString());
            fzzfchannelhtService.removeByChatids(strings);
            fzzfchannelmsgService.removeByChatids(strings);
            fzzffansrecordsinService.removeByChatids(strings);
        }
        //机器人进入频道通知
        if ((newChatMember.getStatus().equals("member") || newChatMember.getStatus().equals("administrator")) && (oldChatMember.getStatus().equals("left") || oldChatMember.getStatus().equals("kicked"))) {
            Msg.sendMsgHTML("检测到机器人进入频道"
                    + TeUrlUtil.getUrlExpression(fzzfpermissions.getUrl()
                    , fzzfpermissions.getTitle()), fzzfExecBot, Long.parseLong(fzzfpermissions.getInviteuserid()));
            Msg.sendMsgHTML("检测到机器人进入频道"
                    + TeUrlUtil.getUrlExpression(fzzfpermissions.getUrl(), fzzfpermissions.getTitle()) + "操作人 @" +
                    handleName, fzzfExecBot, Long.parseLong(shqid));
        }
        //权限不足通知
        if (newChatMember.getStatus().equals("member") || newChatMember.getStatus().equals("administrator")) {
            if (fzzfpermissions.getMsgandinvqx().equals("0")) {
                Msg.sendMsgHTML("检测到机器人权限不足，请添加管理员并管理消息和添加成员权限。" + TeUrlUtil.getUrlExpression(fzzfpermissions.getUrl(), fzzfpermissions.getTitle()), fzzfExecBot, Long.parseLong(fzzfpermissions.getInviteuserid()));
            } else {
                Msg.sendMsgHTML("检测到机器人权限正常" + TeUrlUtil.getUrlExpression(fzzfpermissions.getUrl(), fzzfpermissions.getTitle()), fzzfExecBot, Long.parseLong(fzzfpermissions.getInviteuserid()));
            }
        }

    }

    /**
     * 延迟生成邀请链接
     *
     * @param id
     * @param fzzfExecBot
     */
    private void delayedGenerationOfInvitationLinks(Integer id, FzzfExecBot fzzfExecBot) {
        scheduler.schedule(() -> {
            Fzzfpermissions permissions = fzzfpermissionsService.getById(id);
            if (permissions != null) {
                String inviteLink = Msg.getInviteLink(Long.parseLong(permissions.getChatid()), fzzfExecBot);
                if (StrUtil.isNotBlank(inviteLink)) {
                    permissions.setInviteurl(inviteLink);
                    fzzfpermissionsService.updateById(permissions);
                }
            }
        }, 15, TimeUnit.SECONDS);
    }

    public void handlePermissionsFromGroup(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handlePermissionsFromSuperGroup(Update update, FzzfExecBot fzzfExecBot) {
    }

    public void handlePermissionsFromUserChat(Update update, FzzfExecBot fzzfExecBot) {
    }
}
