package com.bot.botb.bot;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bota.pojo.ChannelHtInfo;
import com.bot.bota.pojo.Permissions;
import com.bot.bota.utils.Msg;
import com.bot.botb.entity.*;
import com.bot.botb.enums.BStr;
import com.bot.botb.service.*;
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


@Component
/**
 * 处理信息
 */
@Slf4j
public class BDealBot {

    @Autowired
    private SpidersUtils spidersUtils;
    @Autowired
    private BadService badService;
    @Autowired
    private BcdService bcdService;
    @Autowired
    private BchannelhtService bchannelhtService;
    @Autowired
    private BfansrecordsinService bfansrecordsinService;
    @Autowired
    private BchannelmsgService bchannelmsgService;
    @Autowired
    private BteadminService bteadminService;
    @Autowired
    private BpermissionsService bpermissionsService;
    @Autowired
    private BmydataService bmydataService;
    @Autowired
    private BcopywriterService bcopywriterService;
    @Autowired
    private BvisitorsService bvisitorsService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    // 定时任务调度器
    private ScheduledExecutorService htscheduler;


    public void handleUserCallbackQuery(Update update, BExecBot bExecBot) {
        if (update.getCallbackQuery().getData().startsWith("gl")) {
            Bteadmin bteadmin = bteadminService.getOneByChatId(update.getCallbackQuery().getFrom().getId());
            if (bteadmin == null) {
                return;
            }
            handlestartsWithglCallbackQuery(update, bExecBot);
            return;
        }
        if (update.getCallbackQuery().getData().startsWith("cdstart")) {
            handlestartsWithcdstartCallbackQuery(update, bExecBot);
            return;
        }
    }

    private void handlestartsWithcdstartCallbackQuery(Update update, BExecBot bExecBot) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (data.contains("#")) {
            String[] split = data.split("#");
            switch (split[0]) {
                case "cdstartsqprev", "cdstartsqnext" -> {
                    sendfzsq(chatId, bExecBot, update.getCallbackQuery().getMessage().getMessageId(), Integer.parseInt(split[1]));
                }
                case "cdstartsqselectfz" -> {
                    Long cdid = Long.valueOf(split[1]);
                    Bcd bcd = bcdService.getById(cdid);
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    sendcdstartsqselectfz(chatId, cdid, bExecBot, update.getCallbackQuery().getMessage().getMessageId());
                }
                case "cdstartsqadd" -> {
                    Long cdid = Long.valueOf(split[1]);
                    Bcd bcd = bcdService.getById(cdid);
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    long count = bchannelhtService.countByCdidYTG(Math.toIntExact(cdid));
                    long l = Long.parseLong(bmydataService.getValueByMyKey("cdrl"));
                    if (count >= l) {
                        Msg.answerCallbackQueryALert(update, "当前车队成员已满", bExecBot);
                        return;
                    }
                    //判断是否有满足的频道
                    List<Bpermissions> listByInviteuserid = bpermissionsService.findListByInviteuserid(chatId);
                    if (listByInviteuserid == null || listByInviteuserid.isEmpty()) {
                        Msg.answerCallbackQueryALert(update, BStr.str10, bExecBot);
                        return;
                    }
                    boolean flag = false;
                    for (Bpermissions bpermissions : listByInviteuserid) {
                        if (bpermissions.getCount() > bcd.getMinisubscription()) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        Msg.answerCallbackQueryALert(update, BStr.str31, bExecBot);
                        return;
                    }
                    List<Bchannelht> listBySubmitterid = bchannelhtService.getListBySubmitterid(chatId);
                    //将listByInviteuserid中chatid与listBySubmitterid中cdid相同的元素剔除
                    if (listBySubmitterid == null || listBySubmitterid.isEmpty()) {
                        sendcdstartsqadd(chatId, cdid, bExecBot, update.getCallbackQuery().getMessage().getMessageId());
                    } else {
                        HashSet<String> chatids = new HashSet<>();
                        for (Bpermissions bpermissions : listByInviteuserid) {
                            chatids.add(bpermissions.getChatid());
                        }
                        for (Bchannelht bchannelht : listBySubmitterid) {
                            chatids.remove(bchannelht.getChatid());
                        }
                        if (chatids.isEmpty()) {
                            Msg.answerCallbackQueryALert(update, BStr.str10, bExecBot);
                        } else {
                            sendcdstartsqadd(chatId, cdid, bExecBot, update.getCallbackQuery().getMessage().getMessageId());
                        }
                    }
                }
                case "cdstartsqaddpd" -> {
                    Long cdid = Long.valueOf(split[1]);
                    String chatid = split[2];
                    Bcd bcd = bcdService.getById(cdid);
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    Bchannelht bchannelht = bchannelhtService.getByChatid(chatid);
                    if (bchannelht != null) {
                        Msg.answerCallbackQuery(update, "频道已存在", bExecBot);
                        return;
                    }
                    long count = bchannelhtService.countByCdidYTG(Math.toIntExact(cdid));
                    long l = Long.parseLong(bmydataService.getValueByMyKey("cdrl"));
                    if (count >= l) {
                        Msg.answerCallbackQuery(update, "当前车队成员已满", bExecBot);
                        return;
                    }
                    Bpermissions oneByChatId = bpermissionsService.getOneByChatId(Long.valueOf(chatid));
                    if (oneByChatId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    bchannelht = new Bchannelht();
                    bchannelht.setTitle(oneByChatId.getTitle());
                    bchannelht.setCdid(bcd.getId());
                    bchannelht.setChatid(chatid);
                    bchannelht.setSubmitterid(oneByChatId.getInviteuserid());
                    int channelCount = spidersUtils.getChannelCount(oneByChatId.getUrl());
                    if (channelCount == 0) {
                        int channelCount1 = spidersUtils.getChannelCount(oneByChatId.getInviteurl());
                        if (channelCount1 == 0) {
                            int chatMembersCount = Msg.getChatMembersCount(chatId, bExecBot);
                            bchannelht.setCount(chatMembersCount);
                        } else {
                            bchannelht.setCount(channelCount1);
                        }
                    } else {
                        bchannelht.setCount(channelCount);
                    }
                    bchannelht.setCreatetime(TeTimeUtil.getNowTime());
                    bchannelht.setUrl(oneByChatId.getUrl());
                    bchannelht.setAudit("0");
                    bchannelht.setInvitelink(oneByChatId.getInviteurl());
                    bchannelhtService.save(bchannelht);
                    Msg.answerCallbackQuery(update, "提交成功", bExecBot);
                    //15秒后再次同步邀请链接
                    scheduler.schedule(() -> {
                        Bchannelht byChatid = bchannelhtService.getByChatid(chatid);
                        Bpermissions oneByChatId1 = bpermissionsService.getOneByChatId(chatId);
                        if (byChatid != null && oneByChatId1 != null) {
                            byChatid.setInvitelink(oneByChatId1.getInviteurl());
                            bchannelhtService.updateById(byChatid);
                        }
                    }, 15, TimeUnit.SECONDS);
                    sendcdstartsqadd(chatId, cdid, bExecBot, update.getCallbackQuery().getMessage().getMessageId());
                    //发送频道审核通知
                    sendChannelAuditNotice(bExecBot, bchannelht.getId(), "sendnew", null);
                    Msg.sendMsgHTML("您提交的频道" + TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()) + "已提交审核，请耐心等待审核结果。", bExecBot, chatId);
                }
                case "cdstartglmypddetail" -> {
                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                    Long id = Long.parseLong(split[1]);
                    Bchannelht bchannelht = bchannelhtService.getById(id);
                    if (bchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    Bpermissions oneByChatId = bpermissionsService.getOneByChatId(Long.valueOf(bchannelht.getChatid()));
                    Bcd byId = bcdService.getById(bchannelht.getCdid());
                    StringBuilder msgText = new StringBuilder("频道详情");
                    msgText.append("\n频道标题: ").append(TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()));
                    msgText.append("\n频道ID: ").append(bchannelht.getChatid());
                    msgText.append("\n提交者ID: ").append(bchannelht.getSubmitterid());
                    msgText.append("\n车队: ").append(byId.getTitle());
                    msgText.append("\n人数: ").append(bchannelht.getCount());
                    msgText.append("\n审核状态: ").append(bchannelht.getAudit().equals("1") ? "通过" : "审核中");
                    msgText.append("\n管理权限: ").append(oneByChatId.getMsgandinvqx().equals("1") ? "有" : "无");
                    msgText.append("\n5日进粉统计: ");
                    List<String> beforeDays = TeTimeUtil.getBeforeDays(5);
                    List<Bfansrecordsin> listByChatId = bfansrecordsinService.findListByChatId(bchannelht.getChatid());
                    for (String beforeDay : beforeDays) {
                        int count = 0;
                        for (Bfansrecordsin bfansrecordsin : listByChatId) {
                            if (beforeDay.equals(bfansrecordsin.getDate())) {
                                count = bfansrecordsin.getFanscount();
                                break;
                            }
                        }
                        msgText.append("\n").append(beforeDay).append("：").append(count).append("人");
                    }
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(BStr.str17, "callback", "cdstartglmypdexit#" + id));
                    list1.add(new MyButton(BStr.str30, "callback", "cdstartglmypdedittitle#" + id));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton(BStr.str0713, "callback", "cdstartreturnmenu"));
                    list2.add(new MyButton(BStr.str08, "callback", "cdstartglmypd"));
                    lists.add(list2);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText.toString(), bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                }
                case "cdstartglmypdedittitle" -> {
                    String id = split[1];
                    Bchannelht bchannelht = bchannelhtService.getById(id);
                    if (bchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    if (!chatId.toString().equals(bchannelht.getSubmitterid())) {
                        Msg.answerCallbackQuery(update, "无权限操作", bExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    Msg.sendMsgForceReplyKeyboard(bchannelht.getChatid() + "\n请输入新的频道标题", bExecBot, chatId);
                }
                case "cdstartglmypdexit" -> {
                    String id = split[1];
                    Bchannelht bchannelht = bchannelhtService.getById(id);
                    if (bchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    if (!chatId.toString().equals(bchannelht.getSubmitterid())) {
                        Msg.answerCallbackQuery(update, "无权限操作", bExecBot);
                        return;
                    }
                    bchannelhtService.removeById(id);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(bchannelht.getChatid());
                    bchannelmsgService.removeByChatids(strings);
                    bfansrecordsinService.removeByChatids(strings);
                    Msg.leaveChat(Long.valueOf(bchannelht.getChatid()), bExecBot);
                    Msg.answerCallbackQuery(update, "退出频道成功", bExecBot);
                    sendcdstartglmypd(bExecBot, update, 1);
                }
                case "cdstartglmypdprev", "cdstartglmypdnext" -> {
                    String current = split[1];
                    sendcdstartglmypd(bExecBot, update, Integer.parseInt(current));
                }

            }
        } else {
            switch (data) {
                case "cdstartvive" -> {

                }
                case "cdstartsq" -> {
                    sendfzsq(update.getCallbackQuery().getMessage().getChatId(), bExecBot, update.getCallbackQuery().getMessage().getMessageId(), 1);
                }
                case "cdstartreturnmenu" -> {
                    sendStartMain(update.getCallbackQuery().getMessage().getChatId(), bExecBot, "edit", update.getCallbackQuery().getMessage().getMessageId());
                }
                case "cdstartglmypd" -> {
                    sendcdstartglmypd(bExecBot, update, 1);
                }
            }
        }
    }

    //todo 我的频道管理
    private void sendcdstartglmypd(BExecBot bExecBot, Update update, int current) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        List<Bchannelht> listBySubmitterId = bchannelhtService.findListBySubmitterId(chatId);
        if (listBySubmitterId == null || listBySubmitterId.isEmpty()) {
            Msg.answerCallbackQuery(update, "没有数据", bExecBot);
            return;
        } else {
            Page<Bchannelht> pagesBySubmitterId = bchannelhtService.getPagesBySubmitterId(chatId, current);
            if (pagesBySubmitterId.getRecords() == null || pagesBySubmitterId.getRecords().isEmpty()) {
                Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                return;
            } else {
                List<Bchannelht> records = pagesBySubmitterId.getRecords();
                StringBuilder message = new StringBuilder();
                message.append("我的车队频道列表");
                int i = 1;
                List<Bpermissions> bpermissionsList = bpermissionsService.list();
                List<Bcd> list = bcdService.list();
                for (Bchannelht bchannelht : records) {
                    message.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()));
                    message.append(" - ").append(TeNumberUtil.formatNumber(bchannelht.getCount()));
                    for (Bcd bcd : list) {
                        if (bchannelht.getCdid().equals(bcd.getId())) {
                            message.append(" - ").append(bcd.getTitle());
                            break;
                        }
                    }
                    if (bchannelht.getAudit().equals("1")) {
                        message.append("\n").append("✅审核通过");
                    } else {
                        message.append("\n").append("\uD83D\uDEAB审核中");
                    }
                    i++;
                    for (Bpermissions bpermissions : bpermissionsList) {
                        if (bpermissions.getChatid().equals(bchannelht.getChatid())) {
                            if (bpermissions.getMsgandinvqx().equals("1")) {
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
                            Bchannelht bchannelht = records.get(j * 3 + k);
                            list2.add(new MyButton((j * 3 + k + 1) + "", "callback", "cdstartglmypddetail#" + bchannelht.getId()));
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
                list99.add(new MyButton(BStr.str0713, "callback", "cdstartreturnmenu"));
                lists.add(list99);
                InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                Msg.editMsgAndKeyboard(message.toString(), bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
            }
        }
    }

    /**
     * 发送审核
     *
     * @param bExecBot
     * @param id
     * @param sendMode
     * @param messageId
     */
    private void sendChannelAuditNotice(BExecBot bExecBot, Integer id, String sendMode, Integer messageId) {
        Bchannelht byId = bchannelhtService.getById(id);
        if (byId == null) {
            return;
        }
        Integer cdid = byId.getCdid();
        Bcd bcd = bcdService.getById(cdid);
        String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
        msgText += "\n频道ID: " + byId.getChatid();
        msgText += "\n提交者ID: " + byId.getSubmitterid();
        msgText += "\n车队: " + bcd.getTitle();
        msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(bcd.getMinisubscription());
        msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(bcd.getMiniread());
        msgText += "\n人数: " + byId.getCount();
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(BStr.str11, "callback", "shtg#" + id));
        list1.add(new MyButton(BStr.str12, "callback", "shjj#" + id));
        list1.add(new MyButton(BStr.str13, "callback", "shjh#" + id));
        lists.add(list1);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        String shqid = bmydataService.getValueByMyKey("shqid");
        if (sendMode.equals("sendnew") && messageId == null) {
            Msg.sendMsgAndKeyboard(msgText, bExecBot, "html", Long.valueOf(shqid), inlineKeyboardMarkup, null);
        } else {
            Msg.editMsgAndKeyboard(msgText, bExecBot, "html", Long.valueOf(shqid), messageId, inlineKeyboardMarkup, null);
        }

    }

    private void sendcdstartsqadd(Long chatId, Long cdid, BExecBot bExecBot, Integer messageId) {
        Bcd bcd = bcdService.getById(cdid);
        StringBuilder msgText = new StringBuilder();
        msgText.append("请选择频道");
        msgText.append("\n频道标题: ").append(bcd.getTitle());
        msgText.append("\n频道介绍: ").append(bcd.getCddesc());
        msgText.append("\n车队类型:").append(bcd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(bcd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(bcd.getMiniread()));
        msgText.append("\n请确认是否已经将机器人拉入频道并授予管理员权限");
        msgText.append("\n请选择一个提交");
        List<List<MyButton>> lists = new ArrayList<>();
        List<Bpermissions> listByInviteuserid = bpermissionsService.findListByInviteuserid(chatId);
        List<Bchannelht> listBySubmitterid = bchannelhtService.getListBySubmitterid(chatId);
        if (listBySubmitterid == null || listBySubmitterid.isEmpty()) {
            for (Bpermissions bpermissions : listByInviteuserid) {
                List<MyButton> list1 = new ArrayList<>();
                list1.add(new MyButton(bpermissions.getTitle(), "callback", "cdstartsqaddpd#" + cdid + "#" + bpermissions.getChatid()));
                lists.add(list1);
            }
        } else {
            HashSet<String> chatids = new HashSet<>();
            for (Bpermissions bpermissions : listByInviteuserid) {
                if (bpermissions.getCount() > bcd.getMinisubscription()) {
                    chatids.add(bpermissions.getChatid());
                }
            }
            for (Bchannelht bchannelht : listBySubmitterid) {
                chatids.remove(bchannelht.getChatid());
            }
            for (String chatid : chatids) {
                List<MyButton> list1 = new ArrayList<>();
                String title = "";
                for (Bpermissions bpermissions : listByInviteuserid) {
                    if (bpermissions.getChatid().equals(chatid)) {
                        title = bpermissions.getTitle();
                        break;
                    }
                }
                list1.add(new MyButton(title, "callback", "cdstartsqaddpd#" + cdid + "#" + chatid));
                lists.add(list1);
            }
        }
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "cdstartreturnmenu"));
        list99.add(new MyButton(BStr.str08, "callback", "cdstartsq"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), bExecBot, null, chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendcdstartsqselectfz(Long chatId, Long bcdid, BExecBot bExecBot, Integer messageId) {
        StringBuilder msgText = new StringBuilder();
        Bcd bcd = bcdService.getById(bcdid);
        Long count = bchannelhtService.lambdaQuery().eq(Bchannelht::getCdid, bcdid).count();
        long l = Long.parseLong(bmydataService.getValueByMyKey("cdrl"));
        msgText.append("车队标题:").append(bcd.getTitle());
        msgText.append("\n车队介绍:").append(bcd.getCddesc());
        msgText.append("\n当前/最大(成员):").append(count).append("/").append(l);
        msgText.append("\n车队类型:").append(bcd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(bcd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(bcd.getMiniread()));
        msgText.append("\n成员列表:");
        List<Bchannelht> bchannelhtslist = bchannelhtService.findListByCdid(bcdid);
        if (bchannelhtslist == null || bchannelhtslist.isEmpty()) {
            msgText.append("\n当前没有互推成员");
        } else {
            int i = 1;
            for (Bchannelht bchannelht : bchannelhtslist) {
                msgText.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(bchannelht.getInvitelink(), bchannelht.getTitle()));
                msgText.append(" - ").append(TeNumberUtil.formatNumber(bchannelht.getCount()));
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
            list1.add(new MyButton(BStr.str09, "callback", "cdstartsqadd#" + bcdid));
            lists.add(list1);
        }

        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "cdstartreturnmenu"));
        list99.add(new MyButton(BStr.str08, "callback", "cdstartsq"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    /**
     * 分组申请选择界面
     *
     * @param chatId
     * @param bExecBot
     * @param messageId
     * @param current
     */
    private void sendfzsq(Long chatId, BExecBot bExecBot, Integer messageId, int current) {
        long count = bcdService.count();
        long pdcount = bchannelhtService.count();
        String msgText = "频道-车队大厅";
        msgText += "\n车队数量:" + count;
        msgText += "\n频道数量:" + pdcount;
        msgText += "\n图标介绍:";
        msgText += "\n\uD83D\uDFE2未满/\uD83D\uDD34已满|队内成员数-最大成员数|最小订阅需求";
        msgText += "\n选择下方车队进入指定车队，然后选择你要申请的频道进行上车提交。";
        Page<Bcd> page = bcdService.pageList(current);
        List<List<MyButton>> lists = new ArrayList<>();
        int cdrl = Integer.parseInt(bmydataService.getValueByMyKey("cdrl"));
//        for (Bcd bcd : page.getRecords()) {
//            List<MyButton> list1 = new ArrayList<>();
//            Long count1 = bchannelhtService.lambdaQuery().eq(Bchannelht::getCdid, bcd.getId()).count();
//            String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + bcd.getTitle() + "|"
//                    + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(bcd.getMinisubscription());
//            list1.add(new MyButton(buttonName, "callback", "cdstartsqselectfz#" + bcd.getId()));
//            lists.add(list1);
//        }
        int size = page.getRecords().size() / 2 + (page.getRecords().size() % 2 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                if (j * 2 + k < page.getRecords().size()) {
                    Bcd bcd = page.getRecords().get(j * 2 + k);
                    Long count1 = bchannelhtService.lambdaQuery().eq(Bchannelht::getCdid, bcd.getId()).count();
                    String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + bcd.getTitle() + "|"
                            + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(bcd.getMinisubscription());
                    list2.add(new MyButton(buttonName, "callback", "cdstartsqselectfz#" + bcd.getId()));
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
        list99.add(new MyButton(BStr.str0713, "callback", "cdstartreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, bExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    //todo gl开头回调分支
    private void handlestartsWithglCallbackQuery(Update update, BExecBot bExecBot) {
        String data = update.getCallbackQuery().getData();
        if (data.equals("glreturnmenu")) {
            myGlMenu(update.getCallbackQuery().getMessage().getChatId(), bExecBot, "edit", update.getCallbackQuery().getMessage().getMessageId());
        }
        if (data.startsWith("glfz")) {
            handlestartsWithglfzCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glht")) {
            handlestartsWithglhtCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glpd")) {
            handlestartsWithglpdCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glad")) {
            handlestartsWithgladCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glsendfrequency")) {
            handlestartsWithglsendfrequencyCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glrefreshcount")) {
            handlestartsWithglrefreshcountCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glcheckpd")) {
            handlestartsWithglcheckpdCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("gluserused")) {
            handlestartsWithgluserusedCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("gltgbutton")) {
            handlestartsWithgltgbuttonCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glwa")) {
            handlestartsWithglwaCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glbb")) {
            handlestartsWithglbbCallbackQuery(update, bExecBot);
            return;
        }

    }

    //todo 处理报表相关回调
    private void handlestartsWithglbbCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glbb" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    List<Bcd> list = bcdService.list();
                    List<Bchannelht> listByAudit = bchannelhtService.findListByAudit("1");
                    List<Bfansrecordsin> list1 = bfansrecordsinService.list();
                    for (Bcd bcd : list) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("车队:").append(bcd.getTitle());
                        int i = 1;
                        for (Bchannelht bchannelht : listByAudit) {
                            if (bchannelht.getCdid().equals(bcd.getId())) {
                                sb.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()));
                                int today = 0;
                                for (Bfansrecordsin bfansrecordsin : list1) {
                                    if (bfansrecordsin.getChatid().equals(bchannelht.getChatid()) && bfansrecordsin.getDate().equals(TeTimeUtil.getNowTime())) {
                                        today = bfansrecordsin.getFanscount();
                                        break;
                                    }
                                }
                                int total = 0;
                                for (Bfansrecordsin bfansrecordsin : list1) {
                                    if (bfansrecordsin.getChatid().equals(bchannelht.getChatid())) {
                                        total += bfansrecordsin.getFanscount();
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
                        Msg.sendMsgHTML(sb.toString(), bExecBot, chatId);
                    }
                }
            }
        }
    }

    /**
     * @param update
     * @param bExecBot
     */
    //todo 管理文案回调
    private void handlestartsWithglwaCallbackQuery(Update update, BExecBot bExecBot) {
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
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    Msg.sendMsgMarkdown(msg, bExecBot, chatId);
                }
                case "glwavive" -> {
                    String key = split[1];
                    Bcopywriter oneByTextkey = switch (key) {
                        case "1" -> bcopywriterService.getOneByTextkey("start01");
                        case "2" -> bcopywriterService.getOneByTextkey("start02");
                        case "3" -> bcopywriterService.getOneByTextkey("help");
                        default -> null;
                    };
                    if (oneByTextkey == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    List<MessageEntity> entities = null;
                    if (StrUtil.isNotBlank(oneByTextkey.getTextentities())) {
                        entities = JSONUtil.toList(JSONUtil.parseArray(oneByTextkey.getTextentities()), MessageEntity.class);
                    }
                    Msg.sendMsgAndEntities(oneByTextkey.getTextcontent(), bExecBot, chatId, entities);
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
                    list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
                    lists.add(list99);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, bExecBot, chatId, messageId, inlineKeyboardMarkup, null);
                }
            }
        }

    }

    private void handlestartsWithgltgbuttonCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "gltgbutton" -> {
                    sendgltgbutton(chatId, bExecBot, messageId);
                }
                case "gltgbuttonvive" -> {
                    String mybutton = bmydataService.getValueByMyKey("mybutton");
                    if (StrUtil.isBlank(mybutton)) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    InlineKeyboardMarkup inlineKeyboardMarkupString = Msg.getInlineKeyboardMarkupString(mybutton);
                    if (inlineKeyboardMarkupString == null) {
                        Msg.answerCallbackQuery(update, "按钮格式错误", bExecBot);
                        return;
                    } else {
                        Msg.answerCallbackQueryNull(update, bExecBot);
                        String msgText = "按钮预览：\n" + mybutton;
                        Msg.sendMsgAndKeyboard(msgText, bExecBot, chatId, inlineKeyboardMarkupString, null);
                    }
                }
                case "gltgbuttonedit" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
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
                    Msg.sendMsgMarkdown(msgText, bExecBot, chatId);
                }
                case "gltgbuttonclear" -> {
                    bmydataService.updateValueByKey("mybutton", "");
                    Msg.answerCallbackQuery(update, "已清除", bExecBot);
                }
            }
        }
    }

    private void sendgltgbutton(Long chatId, BExecBot bExecBot, Integer messageId) {
        String msgText = "管理推广按钮";
        //按钮
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(BStr.str25, "callback", "gltgbuttonvive"));
        list1.add(new MyButton(BStr.str26, "callback", "gltgbuttonedit"));
        list1.add(new MyButton(BStr.str27, "callback", "gltgbuttonclear"));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, bExecBot, chatId, messageId, inlineKeyboardMarkup, null);

    }

    private void handlestartsWithgluserusedCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "gluserused" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    try {
                        Long id = bExecBot.getMe().getId();
                        List<Bvisitors> listByBotId = bvisitorsService.findListByBotId(id);
                        if (listByBotId != null && !listByBotId.isEmpty()) {

                            HashSet<String> ids = new HashSet<>();
                            for (Bvisitors bvisitors : listByBotId) {
                                ids.add(bvisitors.getUserid());
                            }
                            int totalCount = ids.size();//总用户数
                            //统计今日访问用户数
                            int todayCount = 0;
                            String nowTime = TeTimeUtil.getNowTime();
                            for (Bvisitors bvisitors : listByBotId) {
                                if (bvisitors.getFwtime().equals(nowTime)) {
                                    todayCount++;
                                }
                            }
                            String msgText = "机器人总用户数：" + totalCount + "人，今日访问用户数：" + todayCount + "人。";
                            Msg.sendMsg(msgText, bExecBot, chatId);
                        }

                    } catch (TelegramApiException e) {
                        log.error("获取botId失败", e);
                    }
                }
            }
        }
    }

    private void handlestartsWithglcheckpdCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glcheckpd" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    String msgText = "正在检测。。。。。";
                    List<Bchannelht> list = bchannelhtService.list();
                    if (list != null && !list.isEmpty()) {
                        Msg.sendMsgMarkdown(msgText, bExecBot, chatId);
                        for (Bchannelht bchannelht : list) {
                            int channelCount1 = spidersUtils.getChannelCount(bchannelht.getUrl());
                            int channelCount2 = spidersUtils.getChannelCount(bchannelht.getInvitelink());
                            if (channelCount1 == 0 && channelCount2 == 0) {
                                Msg.sendMsgHTML("频道id" + bchannelht.getChatid() + "：" + TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()) + "疑似失效，请检查。", bExecBot, chatId);
                                Msg.sendMsgMarkdown("私聊机器人输入以下内容可以删除该频道:\n" + "`/glpddelete " + bchannelht.getId() + "`", bExecBot, chatId);
                            }
                            log.info("频道id{}：{}检测结果：{}|{}", bchannelht.getChatid(), TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()), channelCount1, channelCount2);
                        }
                        Msg.sendMsg("检测完成", bExecBot, chatId);
                    } else {
                        Msg.answerCallbackQuery(update, "暂无数据", bExecBot);
                        return;
                    }
                }
            }
        }
    }

    private void handlestartsWithglrefreshcountCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glrefreshcount" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    String msgText = "刷新频道/群组成员数量中...";
                    Msg.sendMsg(msgText, bExecBot, chatId);
                    bchannelhtService.refreshCount();
                    Msg.sendMsg("已刷新", bExecBot, chatId);
                }
            }
        }
    }

    private void handlestartsWithglsendfrequencyCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glsendfrequency" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    String msgText = "请编辑以下模版发送：（备注，频率单位为分钟）";
                    msgText += "\n`频道发送频率修改";
                    msgText += "\n频率:30`";
                    Msg.sendMsgMarkdown(msgText, bExecBot, chatId);
                }
            }
        }
    }


    private void handlestartsWithgladCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "gladstatus" -> {
                    Long id = Long.parseLong(split[1]);
                    Bad bad = badService.getById(id);
                    if (bad == null) {
                        Msg.answerCallbackQuery(update, "广告不存在", bExecBot);
                        return;
                    }
                    if (bad.getStatus().equals("1")) {
                        bad.setStatus("0");
                        badService.updateById(bad);
                    } else {
                        bad.setStatus("1");
                        badService.updateById(bad);
                    }
                    sendGladMenu(chatId, bExecBot, messageId);
                }
                case "gladdelete" -> {
                    Long id = Long.parseLong(split[1]);
                    Bad bad = badService.getById(id);
                    if (bad == null) {
                        Msg.answerCallbackQuery(update, "广告不存在", bExecBot);
                        return;
                    }
                    badService.removeById(id);
                    sendGladMenu(chatId, bExecBot, messageId);
                }
            }
        } else {
            switch (data) {
                case "glad" -> {
                    sendGladMenu(chatId, bExecBot, messageId);
                }
                case "gladadd" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    String msgText = "请编辑以下模版发送：（备注，位置1代表消息头部，2代表尾部）";
                    msgText += "\n`广告添加";
                    msgText += "\n标题:xxx";
                    msgText += "\nurl:xxx";
                    msgText += "\n位置:1`";
                    Msg.sendMsgMarkdown(msgText, bExecBot, chatId);
                }
            }
        }
    }

    private void sendGladMenu(Long chatId, BExecBot bExecBot, Integer messageId) {
        String msgText = "广告管理";
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(BStr.str19, "callback", "gladadd"));
        lists.add(list1);
        List<Bad> allByAdtype = badService.findAllByAdtype("1");
        if (allByAdtype != null && !allByAdtype.isEmpty()) {
            for (Bad bad : allByAdtype) {
                List<MyButton> list2 = new ArrayList<>();
                list2.add(new MyButton(bad.getTitle(), "url", bad.getContent()));
                if (bad.getFlag().equals("1")) {
                    list2.add(new MyButton(BStr.str23, "callback", "null"));
                } else {
                    list2.add(new MyButton(BStr.str24, "callback", "null"));
                }
                if (bad.getStatus().equals("1")) {
                    list2.add(new MyButton(BStr.str21, "callback", "gladstatus#" + bad.getId()));
                } else {
                    list2.add(new MyButton(BStr.str22, "callback", "gladstatus#" + bad.getId()));
                }
                list2.add(new MyButton(BStr.str20, "callback", "gladdelete#" + bad.getId()));
                lists.add(list2);
            }
        }
        List<Bad> allByAdtypew = badService.findAllByAdtype("2");
        if (allByAdtypew != null && !allByAdtypew.isEmpty()) {
            for (Bad bad : allByAdtypew) {
                List<MyButton> list2 = new ArrayList<>();
                list2.add(new MyButton(bad.getTitle(), "url", bad.getContent()));
                if (bad.getFlag().equals("1")) {
                    list2.add(new MyButton(BStr.str23, "callback", "null"));
                } else {
                    list2.add(new MyButton(BStr.str24, "callback", "null"));
                }
                if (bad.getStatus().equals("1")) {
                    list2.add(new MyButton(BStr.str21, "callback", "gladstatus#" + bad.getId()));
                } else {
                    list2.add(new MyButton(BStr.str22, "callback", "gladstatus#" + bad.getId()));
                }
                list2.add(new MyButton(BStr.str20, "callback", "gladdelete#" + bad.getId()));
                lists.add(list2);
            }
        }
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, bExecBot, chatId, messageId, inlineKeyboardMarkup, null);

    }

    /**
     * 处理glpd开头的回调
     *
     * @param update
     * @param bExecBot
     */
    //todo 处理glpd开头的回调 管理频道
    private void handlestartsWithglpdCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glpdprev", "glpdnext" -> {
                    int current = Integer.parseInt(split[1]);
                    sendGlpdMenu(update, bExecBot, current);
                }
                case "glpddetail" -> {
                    Long id = Long.parseLong(split[1]);
                    Bchannelht bchannelht = bchannelhtService.getById(id);
                    if (bchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    Bpermissions oneByChatId = bpermissionsService.getOneByChatId(Long.valueOf(bchannelht.getChatid()));
                    Bcd byId = bcdService.getById(bchannelht.getCdid());
                    StringBuilder msgText = new StringBuilder("频道详情");
                    msgText.append("\n频道标题: ").append(TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()));
                    msgText.append("\n频道ID: ").append(bchannelht.getChatid());
                    msgText.append("\n提交者ID: ").append(bchannelht.getSubmitterid());
                    msgText.append("\n车队: ").append(byId.getTitle());
                    msgText.append("\n人数: ").append(bchannelht.getCount());
                    msgText.append("\n审核状态: ").append(bchannelht.getAudit().equals("1") ? "通过" : "审核中");
                    msgText.append("\n管理权限: ").append(oneByChatId.getMsgandinvqx().equals("1") ? "有" : "无");
                    msgText.append("\n5日进粉统计: ");
                    List<String> beforeDays = TeTimeUtil.getBeforeDays(5);
                    List<Bfansrecordsin> listByChatId = bfansrecordsinService.findListByChatId(bchannelht.getChatid());
                    for (String beforeDay : beforeDays) {
                        int count = 0;
                        for (Bfansrecordsin bfansrecordsin : listByChatId) {
                            if (beforeDay.equals(bfansrecordsin.getDate())) {
                                count = bfansrecordsin.getFanscount();
                                break;
                            }
                        }
                        msgText.append("\n").append(beforeDay).append("：").append(count).append("人");
                    }
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(BStr.str17, "callback", "glpdexit#" + id));
                    list1.add(new MyButton(BStr.str28, "callback", "glpdreurl#" + id));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton(BStr.str0713, "callback", "glreturnmenu#" + id));
                    list2.add(new MyButton(BStr.str08, "callback", "glpd"));
                    lists.add(list2);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText.toString(), bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                }
                case "glpdreurl" -> {
                    String id = split[1];
                    Bchannelht bchannelht = bchannelhtService.getById(id);
                    if (bchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    Bpermissions oneByChatId = bpermissionsService.getOneByChatId(Long.valueOf(bchannelht.getChatid()));
                    if (oneByChatId.getMsgandinvqx().equals("0")) {
                        Msg.answerCallbackQuery(update, "无管理权限", bExecBot);
                        return;
                    }
                    String inviteLink = Msg.getInviteLink(Long.parseLong(bchannelht.getChatid()), bExecBot);
                    if (StrUtil.isNotBlank(inviteLink)) {
                        Msg.answerCallbackQueryNull(update, bExecBot);
                        oneByChatId.setInviteurl(inviteLink);
                        bpermissionsService.updateById(oneByChatId);
                        bchannelht.setInvitelink(inviteLink);
                        bchannelhtService.updateById(bchannelht);
                        Msg.sendMsg("频道邀请链接已更新,链接为:" + inviteLink, bExecBot, chatId);
                    } else {
                        Msg.answerCallbackQuery(update, "生成邀请链接失败", bExecBot);
                    }
                    return;
                }
                case "glpdexit" -> {
                    String id = split[1];
                    Bchannelht bchannelht = bchannelhtService.getById(id);
                    if (bchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    bchannelhtService.removeById(id);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(bchannelht.getChatid());
                    bchannelmsgService.removeByChatids(strings);
                    bfansrecordsinService.removeByChatids(strings);
                    Msg.leaveChat(Long.valueOf(bchannelht.getChatid()), bExecBot);
                    Msg.answerCallbackQuery(update, "退出频道成功", bExecBot);
                    sendGlpdMenu(update, bExecBot, 1);
                }
            }
        } else {
            switch (data) {
                case "glpd" -> {
                    sendGlpdMenu(update, bExecBot, 1);
                }
            }
        }
    }

    private void sendGlpdMenu(Update update, BExecBot bExecBot, int current) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        StringBuilder message = new StringBuilder("管理频道");
        Page<Bchannelht> bchannelhtPage = bchannelhtService.listOrderByCdid(current);
        if (bchannelhtPage.getRecords() == null || bchannelhtPage.getRecords().isEmpty()) {
            Msg.answerCallbackQuery(update, "没有可管理的频道", bExecBot);
            return;
        }
        int i = 1;
        List<Bpermissions> bpermissionsList = bpermissionsService.list();
        List<Bcd> list = bcdService.list();
        for (Bchannelht bchannelht : bchannelhtPage.getRecords()) {
            message.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(bchannelht.getUrl(), bchannelht.getTitle()));
            message.append(" - ").append(TeNumberUtil.formatNumber(bchannelht.getCount()));
            for (Bcd bcd : list) {
                if (bchannelht.getCdid().equals(bcd.getId())) {
                    message.append(" - ").append(bcd.getTitle());
                    break;
                }
            }
            if (bchannelht.getAudit().equals("1")) {
                message.append("\n").append("✅审核通过");
            } else {
                message.append("\n").append("\uD83D\uDEAB审核中");
            }
            i++;
            for (Bpermissions bpermissions : bpermissionsList) {
                if (bpermissions.getChatid().equals(bchannelht.getChatid())) {
                    if (bpermissions.getMsgandinvqx().equals("1")) {
                        message.append("✅有管理权限");
                    } else {
                        message.append("\uD83D\uDEAB无管理权限");
                    }
                    break;
                }
            }
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<Bchannelht> records = bchannelhtPage.getRecords();
        //每行三个
        int size = records.size() / 3 + (records.size() % 3 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 3; k++) {
                if (j * 3 + k < records.size()) {
                    Bchannelht bchannelht = records.get(j * 3 + k);
                    list2.add(new MyButton((j * 3 + k + 1) + "", "callback", "glpddetail#" + bchannelht.getId()));
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
        list3.add(new MyButton(current + "/" + bchannelhtPage.getPages() + "页", "callback", "null"));
        if (bchannelhtPage.getPages() > current) {//存在下一页
            list3.add(new MyButton("➡\uFE0F下一页", "callback", "glpdnext" + "#" + (current + 1)));
        } else {
            list3.add(new MyButton("-", "callback", "null"));
        }
        lists.add(list3);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(message.toString(), bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    /**
     * 处理glht开头的回调
     *
     * @param update
     * @param bExecBot
     */
    //todo 处理glht开头的回调 启动互推、关闭互推
    private void handlestartsWithglhtCallbackQuery(Update update, BExecBot bExecBot) {
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
                    String cdstatus = bmydataService.getValueByMyKey("cdstatus");
                    if (cdstatus.equals("1")) {
                        Msg.answerCallbackQuery(update, BStr.str14, bExecBot);
                        myGlMenu(chatId, bExecBot, "edit", messageId);
                        return;
                    }
                    // 定时任务
                    //todo 定时任务主体

                    Runnable pushTask = () -> {
                        String cdname = bmydataService.getValueByMyKey("cdname");
                        List<Bcd> bcdList = bcdService.list();//获取所有车队
                        if (bcdList == null || bcdList.isEmpty()) {
                            Msg.answerCallbackQuery(update, "车队为空，无法启动", bExecBot);
                            return;
                        }
                        List<Bchannelht> bchannelhtList = bchannelhtService.findListByAudit("1");
                        if (bchannelhtList == null || bchannelhtList.isEmpty()) {
                            Msg.answerCallbackQuery(update, "没有需要互推的频道", bExecBot);
                            return;
                        }
                        boolean flag = false;
                        for (Bchannelht bchannelht : bchannelhtList) {
                            String invitelink = bchannelht.getInvitelink();
                            if (StrUtil.isBlank(invitelink)) {
                                String inviteLink = Msg.getInviteLink(Long.parseLong(bchannelht.getChatid()), bExecBot);
                                if (StrUtil.isNotBlank(inviteLink)) {
                                    bchannelht.setInvitelink(inviteLink);
                                    bchannelhtService.updateById(bchannelht);
                                    flag = true;
                                }
                            }
                        }
                        if (flag) {
                            bchannelhtList = bchannelhtService.findListByAudit("1");
                        }
                        List<List<Bchannelht>> bchannelhtslists = new ArrayList<>();
                        for (Bcd bcd : bcdList) {//根据不同车队分组
                            List<Bchannelht> bchannelhtslist = new ArrayList<>();
                            for (Bchannelht bchannelht : bchannelhtList) {
                                if (bchannelht.getCdid().equals(bcd.getId())) {
                                    bchannelhtslist.add(bchannelht);
                                }
                            }
                            if (!bchannelhtslist.isEmpty() && bchannelhtslist.size() >= 2) {
                                bchannelhtslists.add(bchannelhtslist);
                            }
                        }
                        List<ChannelHtInfo> channelHtInfoList = new ArrayList<>();
                        for (List<Bchannelht> bchannelhtslist : bchannelhtslists) {//根据分组进行互推
                            for (Bchannelht bchannelht : bchannelhtslist) {//对组内成员进行互推
                                ChannelHtInfo channelHtInfo = new ChannelHtInfo();
                                channelHtInfo.setChatId(bchannelht.getChatid());
                                channelHtInfo.setInviteLink(bchannelht.getInvitelink());
                                channelHtInfo.setUrl(bchannelht.getUrl());
                                StringBuilder message = new StringBuilder();
                                String zuname = "\uD83D\uDE80来自奇迹互推【";
                                for (Bcd bcd : bcdList) {
                                    if (bchannelht.getCdid().equals(bcd.getId())) {
                                        zuname += bcd.getTitle();
                                        channelHtInfo.setCdname(bcd.getTitle());
                                        break;
                                    }
                                }
                                zuname += "】组\uD83D\uDE80";
                                String botName = "qijihutui";
                                try {
                                    botName = bExecBot.getMe().getUserName();
                                } catch (TelegramApiException e) {
                                    log.error("获取机器人用户名失败", e);
                                }
                                message.append("<b>").append(TeUrlUtil.getUrlExpression("https://t.me/" + botName, zuname)).append("</b>");
                                //头部广告
                                List<Bad> byAdtypeHead = badService.findByAdtype("1");
                                if (byAdtypeHead != null && !byAdtypeHead.isEmpty()) {
                                    for (Bad bad : byAdtypeHead) {
                                        message.append("\n<b>AD:").append(TeUrlUtil.getUrlExpression(bad.getContent(), bad.getTitle())).append("</b>");
                                    }
                                }
                                message.append("\n\n");
                                int i = 1;
                                for (Bchannelht bchannelht1 : bchannelhtslist) {
                                    if (!bchannelht1.getId().equals(bchannelht.getId())) {
                                        message.append(i).append(".").append(TeUrlUtil.getUrlExpression(bchannelht1.getInvitelink(), bchannelht1.getTitle()));
                                        message.append("\n");
                                        i++;
                                    }
                                }
                                //尾部广告
                                List<Bad> byAdtypetail = badService.findByAdtype("2");
                                if (byAdtypetail != null && !byAdtypetail.isEmpty()) {
                                    for (Bad bad : byAdtypetail) {
                                        message.append("\n<b>AD:").append(TeUrlUtil.getUrlExpression(bad.getContent(), bad.getTitle())).append("</b>");
                                    }
                                }
                                channelHtInfo.setMessage(message);
                                channelHtInfoList.add(channelHtInfo);
                            }
                        }
                        List<Bchannelmsg> list = bchannelmsgService.list();
                        String shqid = bmydataService.getValueByMyKey("shqid");
                        String mybutton = bmydataService.getValueByMyKey("mybutton");
                        InlineKeyboardMarkup inlineKeyboardMarkupString = null;
                        if (StrUtil.isNotBlank(mybutton)) {
                            inlineKeyboardMarkupString = Msg.getInlineKeyboardMarkupString(mybutton);
                        }
                        List<Bmydata> pdremove = bmydataService.findListByKey("pdremove");
                        for (ChannelHtInfo channelHtInfo : channelHtInfoList) {//往各个频道推送内容
                            for (Bchannelmsg bchannelmsg : list) {
                                if (bchannelmsg.getChatid().equals(channelHtInfo.getChatId())) {
                                    if (StrUtil.isNotBlank(bchannelmsg.getMessageid())) {
                                        Msg.deleteMessage(Long.parseLong(bchannelmsg.getChatid()), Integer.parseInt(bchannelmsg.getMessageid()), bExecBot);
                                    }
                                    HashSet<String> strings = Msg.sendMsgAndKeyboardHT(channelHtInfo.getMessage().toString(), bExecBot,
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
                                                    + "\n原因：" + string, bExecBot, Long.valueOf(shqid));
                                            if (pdremove != null && !pdremove.isEmpty()) {
                                                for (Bmydata bmydata : pdremove) {
                                                    if (string.contains(bmydata.getMyvalus())) {
                                                        flag2 = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    if (flag2) {//删除该频道
                                        Msg.sendMsg("检测到频道" + channelHtInfo.getUrl() + "符合移除规则,将自动移除该频道。", bExecBot, Long.valueOf(shqid));
                                        bchannelhtService.removeByChatid(channelHtInfo.getChatId());
                                        bchannelmsgService.deleteBychatId(channelHtInfo.getChatId());
                                        bfansrecordsinService.deleteBychatId(channelHtInfo.getChatId());
                                        bpermissionsService.deleteBychatId(channelHtInfo.getChatId());
                                        Msg.leaveChat(Long.parseLong(channelHtInfo.getChatId()), bExecBot);
                                        continue;
                                    }
                                    for (String string : strings) {
                                        if (!string.contains("消息发送失败")) {
                                            bchannelmsg.setMessageid(string);
                                            break;
                                        }
                                    }
                                    bchannelmsg.setCreatedate(TeTimeUtil.getNowTimeDetail());
                                    bchannelmsgService.updateById(bchannelmsg);
                                    break;
                                }
                            }
                        }
                    };
                    htscheduler = Executors.newScheduledThreadPool(1);
                    int resetmessagetime = Integer.parseInt(bmydataService.getValueByMyKey("resetmessagetime"));
                    htscheduler.scheduleAtFixedRate(pushTask, 0, resetmessagetime, TimeUnit.MINUTES);
                    bmydataService.updateValueByKey("cdstatus", "1");
                    myGlMenu(chatId, bExecBot, "edit", messageId);
                }
                case "glhtstop" -> {
                    String cdstatus = bmydataService.getValueByMyKey("cdstatus");
                    if (cdstatus.equals("0")) {
                        Msg.answerCallbackQuery(update, BStr.str15, bExecBot);
                        myGlMenu(chatId, bExecBot, "edit", messageId);
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
                        Msg.answerCallbackQuery(update, BStr.str16, bExecBot);
                        bmydataService.updateValueByKey("cdstatus", "0");
                        myGlMenu(chatId, bExecBot, "edit", messageId);
                    }
                }

            }
        }
    }

    /**
     * 处理glfz开头的回调
     *
     * @param update
     * @param bExecBot
     */
    //todo 处理glfz开头的回调 管理分组
    private void handlestartsWithglfzCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glfzmanagenext", "glfzmanageprev" -> {
                    sendglfzmanage(callbackQuery.getMessage().getChatId(), bExecBot, callbackQuery.getMessage().getMessageId(), Integer.parseInt(split[1]));
                }
                case "glfzmanagedetail" -> {
                    Bcd bcd = bcdService.getById(Long.valueOf(split[1]));
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    sendglfzmanagedetail(chatId, Long.valueOf(split[1]), bExecBot, messageId);
                }
                case "glfzedit" -> {
                    Bcd bcd = bcdService.getById(Long.valueOf(split[1]));
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    String msgText = "请编辑以下并发送：";
                    msgText += "\n`车队编辑";
                    msgText += "\n车队id:" + bcd.getId();
                    msgText += "\n车队标题:" + bcd.getTitle();
                    msgText += "\n车队描述:" + bcd.getCddesc();
                    msgText += "\n最低订阅量:" + bcd.getMinisubscription();
                    msgText += "\n最低阅读:" + bcd.getMiniread() + "`";
                    Msg.sendMsgMarkdown(msgText, bExecBot, chatId);
                }
                case "glfzdelete" -> {
                    Bcd bcd = bcdService.getById(Long.valueOf(split[1]));
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    String msgText = "请复制以下命令并发送：（注意：删除分组会删除分组下的所有频道信息，并将机器人移除所在频道。）\n`/glfzdelete " + bcd.getId() + "`";
                    Msg.sendMsgMarkdown(msgText, bExecBot, chatId);
                }
            }
        } else {//不带参数
            switch (data) {
                case "glfz" -> {
                    sendglfz(callbackQuery.getMessage().getChatId(), bExecBot, callbackQuery.getMessage().getMessageId());
                }
                case "glfzadd" -> {
                    Msg.answerCallbackQueryNull(update, bExecBot);
                    Msg.sendMsgAndEntitiesMd(BStr.str0716, bExecBot, callbackQuery.getMessage().getChatId(), null);
                }
                case "glfzmanage" -> {
                    sendglfzmanage(callbackQuery.getMessage().getChatId(), bExecBot, callbackQuery.getMessage().getMessageId(), 1);
                }
            }
        }
    }

    private void sendglfzmanagedetail(Long chatId, Long bcdid, BExecBot bExecBot, Integer messageId) {
        StringBuilder msgText = new StringBuilder();
        Bcd bcd = bcdService.getById(bcdid);
        Long count = bchannelhtService.lambdaQuery().eq(Bchannelht::getCdid, bcdid).count();
        long l = Long.parseLong(bmydataService.getValueByMyKey("cdrl"));
        msgText.append("车队标题:").append(bcd.getTitle());
        msgText.append("\n车队介绍:").append(bcd.getCddesc());
        msgText.append("\n当前/最大(成员):").append(count).append("/").append(l);
        msgText.append("\n车队类型:").append(bcd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(bcd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(bcd.getMiniread()));
        msgText.append("\n成员列表:");
        List<Bchannelht> bchannelhtslist = bchannelhtService.findListByCdid(bcdid);
        if (bchannelhtslist == null || bchannelhtslist.isEmpty()) {
            msgText.append("\n当前没有互推成员");
        } else {
            int i = 1;
            for (Bchannelht bchannelht : bchannelhtslist) {
                msgText.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(bchannelht.getInvitelink(), bchannelht.getTitle()));
                msgText.append(" - ").append(TeNumberUtil.formatNumber(bchannelht.getCount()));
                i++;
            }
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton("编辑", "callback", "glfzedit#" + bcdid));
        list1.add(new MyButton("删除", "callback", "glfzdelete#" + bcdid));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
        list99.add(new MyButton(BStr.str08, "callback", "glfzmanage"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendglfzmanage(Long chatId, BExecBot bExecBot, Integer messageId, int current) {
        long count = bcdService.count();
        long pdcount = bchannelhtService.count();
        String msgText = "频道-车队管理";
        msgText += "\n车队数量:" + count;
        msgText += "\n频道数量:" + pdcount;
        msgText += "\n图标介绍:";
        msgText += "\n\uD83D\uDFE2未满/\uD83D\uDD34已满|队内成员数-最大成员数|最小订阅需求";
        Page<Bcd> page = bcdService.pageList(current);
        List<List<MyButton>> lists = new ArrayList<>();
        int cdrl = Integer.parseInt(bmydataService.getValueByMyKey("cdrl"));
//        for (Bcd bcd : page.getRecords()) {
//            List<MyButton> list1 = new ArrayList<>();
//            Long count1 = bchannelhtService.lambdaQuery().eq(Bchannelht::getCdid, bcd.getId()).count();
//            String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + bcd.getTitle() + "|"
//                    + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(bcd.getMinisubscription());
//            list1.add(new MyButton(buttonName, "callback", "glfzmanagedetail#" + bcd.getId()));
//            lists.add(list1);
//        }
        int size = page.getRecords().size() / 2 + (page.getRecords().size() % 2 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                if (j * 2 + k < page.getRecords().size()) {
                    Bcd bcd = page.getRecords().get(j * 2 + k);
                    Long count1 = bchannelhtService.lambdaQuery().eq(Bchannelht::getCdid, bcd.getId()).count();
                    String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + bcd.getTitle() + "|"
                            + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(bcd.getMinisubscription());
                    list2.add(new MyButton(buttonName, "callback", "glfzmanagedetail#" + bcd.getId()));
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
        list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
        list99.add(new MyButton(BStr.str08, "callback", "glfz"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, bExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendglfz(Long chatId, BExecBot bExecBot, Integer messageId) {
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(BStr.str0714, "callback", "glfzadd"));
        list1.add(new MyButton(BStr.str0715, "callback", "glfzmanage"));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(BStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(BStr.str0701, bExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    public void handleGroupCallbackQuery(Update update, BExecBot bExecBot) {
    }

    public void handleSuperGroupCallbackQuery(Update update, BExecBot bExecBot) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getFrom().getId();
        if (data.startsWith("sh")) {
            Bteadmin oneByChatId = bteadminService.getOneByChatId(chatId);
            if (oneByChatId == null) {
                Msg.answerCallbackQuery(update, "无权限操作", bExecBot);
                return;
            }
            handleSuperGroupstartsWithshCallbackQuery(update, bExecBot);
            return;
        }
        if (data.startsWith("glpd")) {
            Bteadmin oneByChatId = bteadminService.getOneByChatId(chatId);
            if (oneByChatId == null) {
                Msg.answerCallbackQuery(update, "无权限操作", bExecBot);
                return;
            }
            handleSuperGroupstartsWithglpdCallbackQuery(update, bExecBot);
            return;
        }
    }

    private void handleSuperGroupstartsWithglpdCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glpdtitleeditshtg" -> {
                    String pdchatid = split[1];
                    Bchannelht byChatid = bchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
                        return;
                    }
                    String oldTitle = byChatid.getTitle();
                    Message message = (Message) callbackQuery.getMessage();
                    String text = message.getText();
                    List<MessageEntity> entities = message.getEntities();
                    String[] split1 = text.split("\n");
                    String newTitle = split1[3].replace("新标题:", "").trim();
                    byChatid.setTitle(newTitle);
                    bchannelhtService.updateById(byChatid);
                    Msg.answerCallbackQuery(update, "修改成功", bExecBot);
                    //编辑旧消息
                    //按钮
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(BStr.str111, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, bExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(bExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), true);
                }
                case "glpdtitleeditshjj" -> {
                    String pdchatid = split[1];
                    Bchannelht byChatid = bchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
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
                    list1.add(new MyButton(BStr.str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, bExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(bExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), false);

                }
                case "glpdtitleeditshjh" -> {
                    String pdchatid = split[1];
                    Bchannelht byChatid = bchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", bExecBot);
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
                    list1.add(new MyButton(BStr.str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, bExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(bExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), false);
                    Msg.sendMsgForceReplyKeyboard(byChatid.getSubmitterid() + "\n请输入拒绝标题修改原因", bExecBot, chatId);
                }
            }
        } else {//不带参数
        }
    }

    private static void sendTztitleResult(BExecBot bExecBot, Bchannelht byChatid, String newTitle, String oldTitle, Long chatId, boolean isSuccess) {
        String msgText2 = "\uD83D\uDCE3系统通知\uD83D\uDCE3";
        msgText2 += "\n频道标题修改通知";
        msgText2 += "\n频道ID: " + byChatid.getChatid();
        msgText2 += "\n频道: " + TeUrlUtil.getUrlExpression(byChatid.getUrl(), byChatid.getTitle());
        msgText2 += "\n原标题: " + oldTitle;
        msgText2 += "\n新标题: " + newTitle;
        msgText2 += "\n申请结果:" + (isSuccess ? "通过" : "拒绝");
        Msg.sendMsgHTML(msgText2, bExecBot, chatId);
    }

    private void handleSuperGroupstartsWithshCallbackQuery(Update update, BExecBot bExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "shtg" -> {
                    String id = split[1];
                    Bchannelht byId = bchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    if (byId.getAudit().equals("1")) {
                        Msg.answerCallbackQuery(update, "该频道已经审核通过", bExecBot);
                        return;
                    }
                    Bcd bcd = bcdService.getById(byId.getCdid());
                    if (bcd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    long l = bchannelhtService.countByCdidYTG(bcd.getId());
                    int i = Integer.parseInt(bmydataService.getValueByMyKey("cdrl"));
                    if (l >= i) {
                        Msg.answerCallbackQuery(update, "车队成员已满", bExecBot);
                        return;
                    }
                    Bpermissions oneByChatId = bpermissionsService.getOneByChatId(Long.valueOf(byId.getChatid()));
                    byId.setAudit("1");
                    byId.setInvitelink(oneByChatId.getInviteurl());
                    byId.setReviewersid(String.valueOf(update.getCallbackQuery().getFrom().getId()));
                    byId.setShtime(TeTimeUtil.getNowTime());
                    bchannelhtService.updateById(byId);
                    Msg.answerCallbackQuery(update, "审核通过", bExecBot);
                    //todo 发送到交流群
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n车队: " + bcd.getTitle();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(bcd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(bcd.getMiniread());
                    msgText += "\n人数: " + byId.getCount();
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(BStr.str111, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(bExecBot, bcd, l, i, byId);
                    //新建消息关联
                    bchannelmsgService.deleteBychatId(byId.getChatid());
                    bfansrecordsinService.deleteBychatId(byId.getChatid());
                    Bchannelmsg bchannelmsg = new Bchannelmsg();
                    bchannelmsg.setChatid(byId.getChatid());
                    bchannelmsgService.save(bchannelmsg);
                }
                case "shjj" -> {
                    String id = split[1];
                    Bchannelht byId = bchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    Bcd bcd = bcdService.getById(byId.getCdid());
                    bchannelhtService.removeById(byId.getId());
                    Msg.answerCallbackQuery(update, "拒绝成功", bExecBot);
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n车队: " + bcd.getTitle();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(bcd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(bcd.getMiniread());
                    msgText += "\n人数: " + byId.getCount();
                    long l = bchannelhtService.countByCdid(bcd.getId());
                    int i = Integer.parseInt(bmydataService.getValueByMyKey("cdrl"));
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(BStr.str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(bExecBot, bcd, l, i, byId);//通知提交者状态
                    bchannelmsgService.deleteBychatId(byId.getChatid());
                    bfansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), bExecBot);
                }
                case "shjh" -> {
                    String id = split[1];
                    Bchannelht byId = bchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", bExecBot);
                        return;
                    }
                    Bcd bcd = bcdService.getById(byId.getCdid());
                    bchannelhtService.removeById(byId.getId());
                    Msg.answerCallbackQuery(update, "拒绝成功", bExecBot);
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(bcd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(bcd.getMiniread());
                    msgText += "\n车队: " + bcd.getTitle();
                    msgText += "\n人数: " + byId.getCount();
                    long l = bchannelhtService.countByCdid(bcd.getId());
                    int i = Integer.parseInt(bmydataService.getValueByMyKey("cdrl"));
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(BStr.str131, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, bExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(bExecBot, bcd, l, i, byId);//通知提交者状态
                    Msg.sendMsgForceReplyKeyboard(byId.getSubmitterid() + "\n" + byId.getUrl() + "\n请回复审核不通过原因:", bExecBot, chatId);
                    bchannelmsgService.deleteBychatId(byId.getChatid());
                    bfansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), bExecBot);
                }
            }
        } else {//不带参数
            switch (data) {
                case "xxx" -> {
                }
            }
        }
    }

    private static void sendTz(BExecBot bExecBot, Bcd bcd, long l, int i, Bchannelht byId) {
        String msgText2 = "\uD83D\uDCE3系统通知\uD83D\uDCE3";
        msgText2 += "\n申请车队名:" + bcd.getTitle();
        msgText2 += "\n车队类型:频道";
        msgText2 += "\n车队介绍:" + bcd.getCddesc();
        msgText2 += "\n当前/最大(成员):" + l + "/" + i;
        msgText2 += "\n最低订阅: " + TeNumberUtil.formatNumber(bcd.getMinisubscription());
        msgText2 += "\n最低阅读: " + TeNumberUtil.formatNumber(bcd.getMiniread());
        msgText2 += "\n频道ID: " + byId.getChatid();
        msgText2 += "\n申请频道: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
        msgText2 += "\n订阅人数: " + byId.getCount();
        msgText2 += "\n申请状态: " + (byId.getAudit().equals("1") ? "通过" : "未通过");
        Msg.sendMsgHTML(msgText2, bExecBot, Long.valueOf(byId.getSubmitterid()));
    }

    public void handleChannelMsg(Update update, BExecBot bExecBot) {
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
        Bpermissions oneByChatId = bpermissionsService.getOneByChatId(id);
        if (oneByChatId != null) {
            String oldUrl = oneByChatId.getUrl();
            if (StrUtil.isNotBlank(chat.getUserName())) {
                String newUrl = "https://t.me/" + chat.getUserName();
                oneByChatId.setUrl(newUrl);
                String oldTitle = oneByChatId.getTitle();
                if (!oldUrl.equals(newUrl) || !oldTitle.equals(chat.getTitle())) {
                    oneByChatId.setTitle(chat.getTitle());
                    bpermissionsService.updateById(oneByChatId);
                    Bchannelht bchannelht = bchannelhtService.getByChatid(String.valueOf(id));
                    if (bchannelht != null) {
                        if (StrUtil.isNotBlank(chat.getUserName())) {
                            bchannelht.setUrl(oneByChatId.getUrl());
                        }
                        bchannelhtService.updateById(bchannelht);
                    }
                }
            }

        }
    }

    public void handleSuperGroupMsg(Update update, BExecBot bExecBot) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String shqid = bmydataService.getValueByMyKey("shqid");
        if (shqid.equals(chatId.toString()) && update.getMessage().getReplyToMessage() != null) {
            Message replyToMessage = update.getMessage().getReplyToMessage();
            if (replyToMessage.getText().contains("请回复审核不通过原因:")) {
                String cahtId = replyToMessage.getText().split("\n")[0];
                String text = update.getMessage().getText();//审核不通过原因
                Msg.sendMsg("您好，您的互推频道申请已被管理员拒绝，原因如下：\n" + text, bExecBot, Long.parseLong(cahtId));
                return;
            }
            if (replyToMessage.getText().contains("请输入拒绝标题修改原因")) {
                String cahtId = replyToMessage.getText().split("\n")[0];
                String text = update.getMessage().getText();//拒绝标题修改原因
                Msg.sendMsg("您好，您的频道标题修改申请已被管理员拒绝，原因如下：\n" + text, bExecBot, Long.parseLong(cahtId));
                return;
            }
        }
    }

    public void handleGroupMsg(Update update, BExecBot bExecBot) {

    }

    /**
     * 处理私聊用户消息
     *
     * @param update
     * @param bExecBot
     */
    public void handleUserMsg(Update update, BExecBot bExecBot) {
        saveUserFwInfo(update, bExecBot);//保存用户访问信息
        Message replyToMessage = update.getMessage().getReplyToMessage();
        //判断是否是命令
        if (CmdUtil.isPrivateCmd(update) && replyToMessage == null) {
            handleCmd(update, bExecBot);
            return;
        } else if (replyToMessage != null) {
            handleReplyToMsg(update, bExecBot);
            return;
        } else {//普通消息
            handleGeneralMsg(update, bExecBot);
            return;
        }

    }

    /**
     * 处理普通消息
     *
     * @param update
     * @param bExecBot
     */
    //todo 处理所有普通消息
    private void handleGeneralMsg(Update update, BExecBot bExecBot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        if (text.startsWith("车队添加")) {
            Bteadmin bteadmin = bteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (bteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 5) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("车队标题:")
                    || !split[2].startsWith("车队描述:")
                    || !split[3].startsWith("最低订阅量:")
                    || !split[4].startsWith("最低阅读:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            String title = split[1].trim().replaceAll("车队标题:", "");
            String cddesc = split[2].trim().replaceAll("车队描述:", "");
            String minisubscription = split[3].trim().replaceAll("最低订阅量:", "");
            String miniread = split[4].trim().replaceAll("最低阅读:", "");
            Bcd bcd = new Bcd();
            bcd.setTitle(title);
            bcd.setCddesc(cddesc);
            bcd.setMinisubscription(Integer.parseInt(minisubscription));
            bcd.setMiniread(Integer.parseInt(miniread));
            bcd.setType("channel");
            bcdService.save(bcd);
            Msg.sendMsg("车队分组添加成功", bExecBot, chatId);
            return;
        }
        if (text.startsWith("车队编辑")) {
            Bteadmin bteadmin = bteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (bteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 6) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("车队id:")
                    || !split[2].startsWith("车队标题:")
                    || !split[3].startsWith("车队描述:")
                    || !split[4].startsWith("最低订阅量:")
                    || !split[5].startsWith("最低阅读:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            Long cdid = Long.parseLong(split[1].trim().replaceAll("车队id:", ""));
            String title = split[2].trim().replaceAll("车队标题:", "");
            String cddesc = split[3].trim().replaceAll("车队描述:", "");
            String minisubscription = split[4].trim().replaceAll("最低订阅量:", "");
            String miniread = split[5].trim().replaceAll("最低阅读:", "");
            Bcd bcd = bcdService.getById(cdid);
            bcd.setTitle(title);
            bcd.setCddesc(cddesc);
            bcd.setMinisubscription(Integer.parseInt(minisubscription));
            bcd.setMiniread(Integer.parseInt(miniread));
            bcdService.updateById(bcd);
            Msg.sendMsg("车队分组编辑成功", bExecBot, chatId);
            return;
        }
        if (text.startsWith("广告添加")) {
            Bteadmin bteadmin = bteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (bteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 4) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("标题:") || !split[2].startsWith("url:") || !split[3].startsWith("位置:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            String title = split[1].trim().replaceAll("标题:", "");
            String url = split[2].trim().replaceAll("url:", "");
            String position = split[3].trim().replaceAll("位置:", "");
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Msg.sendMsg("url格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            if (position.equals("1") || position.equals("2")) {
                Bad bad = new Bad();
                bad.setTitle(title);
                bad.setContent(url);
                bad.setFlag(position);
                bad.setStatus("1");
                badService.save(bad);
                Msg.sendMsg("广告添加成功", bExecBot, chatId);
            } else {
                Msg.sendMsg("位置只能为1或2", bExecBot, chatId);
                return;
            }
            return;
        }
        if (text.startsWith("频道发送频率修改")) {
            Bteadmin bteadmin = bteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (bteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 2) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("频率:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", bExecBot, chatId);
                return;
            }
            String frequency = split[1].trim().replaceAll("频率:", "");
            bmydataService.updateValueByKey("resetmessagetime", frequency);
            Msg.sendMsg("频道发送频率修改成功", bExecBot, chatId);
            return;
        }
        if (text.startsWith("推广按钮编辑")) {
            text = text.trim().replaceAll("推广按钮编辑\n", "");
            bmydataService.updateValueByKey("mybutton", text);
            Msg.sendMsg("推广按钮编辑成功", bExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑help文案")) {
            text = text.trim().replaceAll("编辑help文案\n", "");
            Bcopywriter oneByTextkey = bcopywriterService.getOneByTextkey("help");
            oneByTextkey.setTextcontent(text);
            bcopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑help文案成功", bExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑start02文案")) {
            text = text.trim().replaceAll("编辑start02文案\n", "");
            Bcopywriter oneByTextkey = bcopywriterService.getOneByTextkey("start02");
            oneByTextkey.setTextcontent(text);
            bcopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑start02文案成功", bExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑start01文案")) {
            text = text.trim().replaceAll("编辑start01文案\n", "");
            Bcopywriter oneByTextkey = bcopywriterService.getOneByTextkey("start01");
            oneByTextkey.setTextcontent(text);
            bcopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑start01文案成功", bExecBot, chatId);
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
     * @param bExecBot
     */
    private void handleReplyToMsg(Update update, BExecBot bExecBot) {
        String shqid = bmydataService.getValueByMyKey("shqid");
        Long chatId = update.getMessage().getChat().getId();//回复者
        String text = update.getMessage().getReplyToMessage().getText();//原始消息内容
        String text1 = update.getMessage().getText();//回复内容
        if (text.contains("请输入新的频道标题")) {
            String pdchatId = text.split("\n")[0].trim();
            Bchannelht byChatid = bchannelhtService.getByChatid(pdchatId);
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
            list1.add(new MyButton(BStr.str11, "callback", "glpdtitleeditshtg#" + pdchatId));
            list1.add(new MyButton(BStr.str12, "callback", "glpdtitleeditshjj#" + pdchatId));
            list1.add(new MyButton(BStr.str13, "callback", "glpdtitleeditshjh#" + pdchatId));
            lists.add(list1);
            InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
            Msg.sendMsgAndKeyboard(msgText, bExecBot, "html", Long.valueOf(shqid), inlineKeyboardMarkup, null);
        }
    }

    /**
     * 处理命令
     *
     * @param update
     * @param bExecBot
     */
    //todo 处理所有cmd命令
    private void handleCmd(Update update, BExecBot bExecBot) {
        Long chatId = update.getMessage().getChatId();
        if (CmdUtil.isCmdWithArgs(update)) {//处理带参数的命令
            String cmdName = CmdUtil.getCmdNameWithArgs(update);
            String cmdArgs = CmdUtil.getCmdArgs(update).trim();
            switch (cmdName) {
                case "glfzdelete" -> {
                    //权限判断
                    Bteadmin bteadmin = bteadminService.getOneByChatId(update.getMessage().getFrom().getId());
                    if (bteadmin == null) {
                        return;
                    }
                    long cdid = Long.parseLong(cmdArgs);
                    Bcd bcd = bcdService.getById(cdid);
                    if (bcd == null) {
                        Msg.sendMsg("车队不存在", bExecBot, chatId);
                        return;
                    }
                    boolean b = bcdService.removeById(cdid);
                    if (b) {
                        //移除所有机器人
                        List<Bchannelht> list = bchannelhtService.getListByCdId(cdid);
                        HashSet<String> chatids = new HashSet<>();
                        for (Bchannelht bchannelht : list) {
                            chatids.add(bchannelht.getChatid());
                            Msg.leaveChat(Long.valueOf(bchannelht.getChatid()), bExecBot);
                        }
                        if (!chatids.isEmpty()) {
                            bchannelhtService.removeByChatids(chatids);
                            bchannelmsgService.removeByChatids(chatids);
                            bfansrecordsinService.removeByChatids(chatids);
                        }
                        Msg.sendMsg("车队删除成功", bExecBot, chatId);
                    } else {
                        Msg.sendMsg("车队删除失败", bExecBot, chatId);
                    }
                    return;
                }
                case "glpddelete" -> {
                    Bteadmin bteadmin = bteadminService.getOneByChatId(update.getMessage().getFrom().getId());
                    if (bteadmin == null) {
                        return;
                    }
                    long pdid = Long.parseLong(cmdArgs);
                    Bchannelht byId = bchannelhtService.getById(pdid);
                    if (byId == null) {
                        Msg.sendMsg("频道不存在", bExecBot, chatId);
                        return;
                    }
                    bchannelhtService.removeById(pdid);
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), bExecBot);
                    bchannelmsgService.deleteBychatId(byId.getChatid());
                    bfansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.sendMsg("频道删除成功", bExecBot, chatId);
                }
            }
        } else {//处理不带参数的命令
            String cmdName = CmdUtil.getCmdNameNoWithArgs(update);
            switch (cmdName) {
                case "gl" -> {
                    //判断权限
                    Bteadmin bteadmin = bteadminService.getOneByChatId(chatId);
                    if (bteadmin == null) {
                        return;
                    }
                    handleglCmd(update, bExecBot);
                    return;
                }
                case "start" -> {
                    dealStartCmd(update, bExecBot);
                    return;
                }
                case "help" -> {
                    Bcopywriter bcopywriter = bcopywriterService.getOneByTextkey("help");
                    if (bcopywriter == null) {
                        return;
                    }
                    Msg.sendMsgAndEntities(bcopywriter.getTextcontent(), bExecBot, chatId, StrUtil.isNotBlank(bcopywriter.getTextentities()) ? JSONUtil.toList(bcopywriter.getTextentities(), MessageEntity.class) : null);
                    return;
                }
            }
        }
    }

    /**
     * 处理start命令
     *
     * @param update
     * @param bExecBot
     */
    private void dealStartCmd(Update update, BExecBot bExecBot) {
        Bcopywriter bcopywriterstart01 = bcopywriterService.getOneByTextkey("start01");
        if (bcopywriterstart01 != null) {
            Msg.sendMsgAndEntities(bcopywriterstart01.getTextcontent(), bExecBot, update.getMessage().getChatId(), StrUtil.isNotBlank(bcopywriterstart01.getTextentities()) ? JSONUtil.toList(bcopywriterstart01.getTextentities(), MessageEntity.class) : null);
        }
        sendStartMain(update.getMessage().getChatId(), bExecBot, "sendnew", null);
    }

    private void sendStartMain(Long chatid, BExecBot bExecBot, String sendModel, Integer messageId) {
        Bcopywriter bcopywriterstart02 = bcopywriterService.getOneByTextkey("start02");
        if (bcopywriterstart02 == null) {
            return;
        }
        String botName;
        try {
            botName = bExecBot.getMe().getUserName();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(BStr.str01, "url", "https://t.me/" + botName + "?startchannel"));
        lists.add(list1);
//        List<MyButton> list2 = new ArrayList<>();
//        list2.add(new MyButton(str02, "callback", "cdstartvive"));
//        lists.add(list2);
        List<MyButton> list3 = new ArrayList<>();
        list3.add(new MyButton(BStr.str03, "callback", "cdstartsq"));
        lists.add(list3);
        List<Bchannelht> listBySubmitterId = bchannelhtService.findListBySubmitterId(chatid);
        if (listBySubmitterId != null && !listBySubmitterId.isEmpty()) {
            List<MyButton> list5 = new ArrayList<>();
            list5.add(new MyButton(BStr.str29, "callback", "cdstartglmypd"));
            lists.add(list5);
        }
        List<MyButton> list4 = new ArrayList<>();
        String jlgroupurl = bmydataService.getValueByMyKey("jlgroupurl");
        String kf = bmydataService.getValueByMyKey("kf");
        list4.add(new MyButton(BStr.str04, "url", jlgroupurl));
        list4.add(new MyButton(BStr.str05, "url", kf));
        lists.add(list4);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        if (sendModel.equals("sendnew")) {
            Msg.sendMsgAndKeyboard(bcopywriterstart02.getTextcontent(),
                    bExecBot,
                    chatid,
                    inlineKeyboardMarkup,
                    StrUtil.isNotBlank(bcopywriterstart02.getTextentities()) ? JSONUtil.toList(bcopywriterstart02.getTextentities(), MessageEntity.class) : null);
        } else {
            Msg.editMsgAndKeyboard(bcopywriterstart02.getTextcontent(),
                    bExecBot,
                    chatid,
                    messageId,
                    inlineKeyboardMarkup,
                    StrUtil.isNotBlank(bcopywriterstart02.getTextentities()) ? JSONUtil.toList(bcopywriterstart02.getTextentities(), MessageEntity.class) : null);
        }
    }

    /**
     * 处理gl命令
     *
     * @param update
     * @param bExecBot
     */
    //todo 处理gl命令
    private void handleglCmd(Update update, BExecBot bExecBot) {
        Long chatId = update.getMessage().getChatId();
        myGlMenu(chatId, bExecBot, "sendnew", null);
    }

    private void myGlMenu(Long chatId, BExecBot bExecBot, String sendModel, Integer messageId) {
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(BStr.str0701, "callback", "glfz"));
        lists.add(list1);
        List<MyButton> list2 = new ArrayList<>();
        list2.add(new MyButton(BStr.str0702, "callback", "glpd"));
        lists.add(list2);
        List<MyButton> list3 = new ArrayList<>();
        list3.add(new MyButton(BStr.str0703, "callback", "glad"));
        lists.add(list3);
        List<MyButton> list4 = new ArrayList<>();
        list4.add(new MyButton(BStr.str0704, "callback", "glwa"));
        lists.add(list4);
        List<MyButton> list5 = new ArrayList<>();
        list5.add(new MyButton(BStr.str0705, "callback", "glbb"));
        lists.add(list5);
        List<MyButton> list6 = new ArrayList<>();
        list6.add(new MyButton(BStr.str0706, "callback", "gltgbutton"));
        lists.add(list6);
        List<MyButton> list7 = new ArrayList<>();
        list7.add(new MyButton(BStr.str0707, "callback", "glcheckpd"));
        lists.add(list7);
        List<MyButton> list8 = new ArrayList<>();
        list8.add(new MyButton(BStr.str0708, "callback", "glrefreshcount"));
        lists.add(list8);
        List<MyButton> list9 = new ArrayList<>();
        list9.add(new MyButton(BStr.str0709, "callback", "glsendfrequency"));
        lists.add(list9);
        List<MyButton> list10 = new ArrayList<>();
        list10.add(new MyButton(BStr.str0710, "callback", "gluserused"));
        lists.add(list10);
        String cdstatus = bmydataService.getValueByMyKey("cdstatus");
        List<MyButton> list11 = new ArrayList<>();
        if (cdstatus.equals("0")) {
            list11.add(new MyButton(BStr.str0711, "callback", "glhtstart"));
        } else {
            list11.add(new MyButton(BStr.str0712, "callback", "glhtstop"));
        }
        lists.add(list11);
        String msgText = BStr.str06;
        String resetmessagetime = bmydataService.getValueByMyKey("resetmessagetime");
        msgText += "\n发送频率(单位分钟):" + resetmessagetime;
        String cdrl = bmydataService.getValueByMyKey("cdrl");
        msgText += "\n车队容量:" + cdrl;
        String cdname = bmydataService.getValueByMyKey("cdname");
        msgText += "\n车队名称:" + cdname;
        msgText += ("\n车队状态:" + (cdstatus.equals("1") ? "已开启" : "已关闭"));
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        if (sendModel.equals("sendnew") && messageId == null) {
            Msg.sendMsgAndKeyboard(msgText,
                    bExecBot,
                    chatId,
                    inlineKeyboardMarkup,
                    null);
        } else {
            Msg.editMsgAndKeyboard(msgText,
                    bExecBot,
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
     * @param bExecBot
     */
    private void saveUserFwInfo(Update update, BExecBot bExecBot) {
        try {
            Long botid = bExecBot.getMe().getId();
            Long userid = update.getMessage().getFrom().getId();
            String nowTime = TeTimeUtil.getNowTime();
            User user = update.getMessage().getFrom();
            Bvisitors bvisitors = bvisitorsService.findByUseridAndFwtimeAndBotid(userid, nowTime, botid);
            if (bvisitors != null) {
                return;
            } else {
                bvisitors = new Bvisitors();
                bvisitors.setBotid(String.valueOf(botid));
                bvisitors.setUserid(String.valueOf(userid));
                bvisitors.setFwtime(nowTime);
                if (StrUtil.isNotBlank(user.getUserName())) {
                    bvisitors.setUsername(user.getUserName());
                }
                if (StrUtil.isNotBlank(user.getFirstName())) {
                    bvisitors.setFirstname(user.getFirstName());
                }
                if (StrUtil.isNotBlank(user.getLastName())) {
                    bvisitors.setLastname(user.getLastName());
                }
                bvisitorsService.save(bvisitors);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleJoinChannel(Update update, BExecBot bExecBot) {
        ChatInviteLink inviteLink = update.getChatMember().getInviteLink();
        if (inviteLink == null) {
            return;
        }
        String inviteLink1 = inviteLink.getInviteLink();
        Bchannelht byInviteLink = bchannelhtService.getByInviteLink(inviteLink1);
        if (byInviteLink == null) {
            return;
        }
        bfansrecordsinService.fansAdd(byInviteLink.getChatid());
    }

    public void handleJoinGroup(Update update, BExecBot bExecBot) {
    }

    public void handleJoinSuperGroup(Update update, BExecBot bExecBot) {
    }

    public void handleJoinUser(Update update, BExecBot bExecBot) {
    }

    public void handleChatJoinRequest(Update update, BExecBot bExecBot) {
    }

    public void handlePermissionsFromChannel(Update update, BExecBot bExecBot) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        Chat chat = myChatMember.getChat();
        Long chatId = chat.getId();
        ChatMember newChatMember = myChatMember.getNewChatMember();
        ChatMember oldChatMember = myChatMember.getOldChatMember();
        User user = newChatMember.getUser();

        try {//检测成员权限变化是否为机器人自己
            if (!user.getId().toString().equals(bExecBot.getMe().getId().toString())) {
                return;
            }
        } catch (TelegramApiException e) {
            return;
        }
        if (StrUtil.isBlank(chat.getUserName())) {//不处理私聊频道
            return;
        }
        //权限表更新
        Bpermissions bpermissions = bpermissionsService.getOneByChatId(chatId);
        if (bpermissions == null) {
            bpermissions = new Bpermissions();
            bpermissions.setChatid(chatId.toString());
            String title = chat.getTitle();
            boolean b = TeEmojiUtil.containsEmoji(title);
            if (b) {
                title = TeEmojiUtil.filterEmoji(title);
            }
            title = TeEmojiUtil.replaceEmoji(title, " ");

            bpermissions.setTitle(title);
            bpermissions.setType(chat.getType());
            bpermissions.setGroupstatus(newChatMember.getStatus());

            bpermissions.setCreatetime(TeTimeUtil.getNowTimeDetail());
            bpermissions.setUrl("https://t.me/" + chat.getUserName());
            Permissions permissions = JSONUtil.toBean(JSONUtil.toJsonStr(newChatMember), Permissions.class);
            if (permissions.isCanPostMessages()
                    && permissions.isCanEditMessages()
                    && permissions.isCanDeleteMessages()
                    && permissions.isCanInviteUsers()
                    && permissions.isCanRestrictMembers()
                    && permissions.isCanManageChat()) {
                bpermissions.setMsgandinvqx("1");
            } else {
                bpermissions.setMsgandinvqx("0");
            }
            if (!myChatMember.getFrom().getIsBot()) {
                bpermissions.setInviteuserid(myChatMember.getFrom().getId().toString());
            }
            int channelCount = spidersUtils.getChannelCount(bpermissions.getUrl());
            if (channelCount > 0) {
                bpermissions.setCount(channelCount);
            } else {
                int chatMembersCount = Msg.getChatMembersCount(Long.valueOf(bpermissions.getChatid()), bExecBot);
                if (chatMembersCount > 0) {
                    bpermissions.setCount(chatMembersCount);
                }
            }
            bpermissionsService.save(bpermissions);
        } else {
            String title = chat.getTitle();
            boolean b = TeEmojiUtil.containsEmoji(title);
            if (b) {
                title = TeEmojiUtil.filterEmoji(title);
            }
            title = TeEmojiUtil.replaceEmoji(title, " ");
            bpermissions.setTitle(title);
            bpermissions.setType(chat.getType());
            bpermissions.setGroupstatus(newChatMember.getStatus());
            bpermissions.setUrl("https://t.me/" + chat.getUserName());
            Permissions permissions = JSONUtil.toBean(JSONUtil.toJsonStr(newChatMember), Permissions.class);
            if (permissions.isCanPostMessages()
                    && permissions.isCanEditMessages()
                    && permissions.isCanDeleteMessages()
                    && permissions.isCanInviteUsers()
                    && permissions.isCanRestrictMembers()
                    && permissions.isCanManageChat()) {
                bpermissions.setMsgandinvqx("1");
            } else {
                bpermissions.setMsgandinvqx("0");
            }
            if (!myChatMember.getFrom().getIsBot()) {
                bpermissions.setInviteuserid(myChatMember.getFrom().getId().toString());
            }
            int channelCount = spidersUtils.getChannelCount(bpermissions.getUrl());
            if (channelCount > 0) {
                bpermissions.setCount(channelCount);
            } else {
                int chatMembersCount = Msg.getChatMembersCount(Long.valueOf(bpermissions.getChatid()), bExecBot);
                if (chatMembersCount > 0) {
                    bpermissions.setCount(chatMembersCount);
                }
            }
            bpermissionsService.updateById(bpermissions);
        }
        if (newChatMember.getStatus().equals("administrator") && bpermissions.getMsgandinvqx().equals("1")) {
            delayedGenerationOfInvitationLinks(bpermissions.getId(), bExecBot);
        }
        String handleName = "";
        if (!myChatMember.getFrom().getIsBot() && StrUtil.isNotBlank(myChatMember.getFrom().getUserName())) {
            handleName = myChatMember.getFrom().getUserName();
        }
        //机器人离开频道通知
        String shqid = bmydataService.getValueByMyKey("shqid");
        if (newChatMember.getStatus().equals("left") || newChatMember.getStatus().equals("kicked")) {
            //如果在互推组中将从互推组中删除并删除相关进粉信息
            Msg.sendMsgHTML("检测到机器人已离开频道，已将"
                    + TeUrlUtil.getUrlExpression(bpermissions.getUrl(), bpermissions.getTitle())
                    + "从互推组中删除。", bExecBot, Long.parseLong(bpermissions.getInviteuserid()));

            Msg.sendMsgHTML("检测到机器人已离开频道，已将"
                            + TeUrlUtil.getUrlExpression(bpermissions.getUrl(), bpermissions.getTitle())
                            + "从互推组中删除。操作人 @"
                            + handleName,
                    bExecBot, Long.parseLong(shqid));
            HashSet<String> strings = new HashSet<>();
            strings.add(chatId.toString());
            bchannelhtService.removeByChatids(strings);
            bchannelmsgService.removeByChatids(strings);
            bfansrecordsinService.removeByChatids(strings);
        }
        //机器人进入频道通知
        if ((newChatMember.getStatus().equals("member") || newChatMember.getStatus().equals("administrator")) && (oldChatMember.getStatus().equals("left") || oldChatMember.getStatus().equals("kicked"))) {
            Msg.sendMsgHTML("检测到机器人进入频道"
                    + TeUrlUtil.getUrlExpression(bpermissions.getUrl()
                    , bpermissions.getTitle()), bExecBot, Long.parseLong(bpermissions.getInviteuserid()));
            Msg.sendMsgHTML("检测到机器人进入频道"
                    + TeUrlUtil.getUrlExpression(bpermissions.getUrl(), bpermissions.getTitle()) + "操作人 @" +
                    handleName, bExecBot, Long.parseLong(shqid));
        }
        //权限不足通知
        if (newChatMember.getStatus().equals("member") || newChatMember.getStatus().equals("administrator")) {
            if (bpermissions.getMsgandinvqx().equals("0")) {
                Msg.sendMsgHTML("检测到机器人权限不足，请添加管理员并管理消息和添加成员权限。" + TeUrlUtil.getUrlExpression(bpermissions.getUrl(), bpermissions.getTitle()), bExecBot, Long.parseLong(bpermissions.getInviteuserid()));
            } else {
                Msg.sendMsgHTML("检测到机器人权限正常" + TeUrlUtil.getUrlExpression(bpermissions.getUrl(), bpermissions.getTitle()), bExecBot, Long.parseLong(bpermissions.getInviteuserid()));
            }
        }

    }

    /**
     * 延迟生成邀请链接
     *
     * @param id
     * @param bExecBot
     */
    private void delayedGenerationOfInvitationLinks(Integer id, BExecBot bExecBot) {
        scheduler.schedule(() -> {
            Bpermissions permissions = bpermissionsService.getById(id);
            if (permissions != null) {
                String inviteLink = Msg.getInviteLink(Long.parseLong(permissions.getChatid()), bExecBot);
                if (StrUtil.isNotBlank(inviteLink)) {
                    permissions.setInviteurl(inviteLink);
                    bpermissionsService.updateById(permissions);
                }
            }
        }, 15, TimeUnit.SECONDS);
    }

    public void handlePermissionsFromGroup(Update update, BExecBot bExecBot) {
    }

    public void handlePermissionsFromSuperGroup(Update update, BExecBot bExecBot) {
    }

    public void handlePermissionsFromUserChat(Update update, BExecBot bExecBot) {
    }
}
