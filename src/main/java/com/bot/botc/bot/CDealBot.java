package com.bot.botc.bot;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.common.pojo.ChannelHtInfo;
import com.bot.common.pojo.Permissions;
import com.bot.botc.entity.*;
import com.bot.botc.enums.CStr;
import com.bot.botc.service.*;
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
public class CDealBot {

    @Autowired
    private SpidersUtils spidersUtils;
    @Autowired
    private CadService cadService;
    @Autowired
    private CcdService ccdService;
    @Autowired
    private CchannelhtService cchannelhtService;
    @Autowired
    private CfansrecordsinService cfansrecordsinService;
    @Autowired
    private CchannelmsgService cchannelmsgService;
    @Autowired
    private CteadminService cteadminService;
    @Autowired
    private CpermissionsService cpermissionsService;
    @Autowired
    private CmydataService cmydataService;
    @Autowired
    private CcopywriterService ccopywriterService;
    @Autowired
    private CvisitorsService cvisitorsService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    // 定时任务调度器
    private ScheduledExecutorService htscheduler;


    public void handleUserCallbackQuery(Update update, CExecBot cExecBot) {
        if (update.getCallbackQuery().getData().startsWith("gl")) {
            Cteadmin cteadmin = cteadminService.getOneByChatId(update.getCallbackQuery().getFrom().getId());
            if (cteadmin == null) {
                return;
            }
            handlestartsWithglCallbackQuery(update, cExecBot);
            return;
        }
        if (update.getCallbackQuery().getData().startsWith("cdstart")) {
            handlestartsWithcdstartCallbackQuery(update, cExecBot);
            return;
        }
    }

    private void handlestartsWithcdstartCallbackQuery(Update update, CExecBot cExecBot) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (data.contains("#")) {
            String[] split = data.split("#");
            switch (split[0]) {
                case "cdstartsqprev", "cdstartsqnext" -> {
                    sendfzsq(chatId, cExecBot, update.getCallbackQuery().getMessage().getMessageId(), Integer.parseInt(split[1]));
                }
                case "cdstartsqselectfz" -> {
                    Long cdid = Long.valueOf(split[1]);
                    Ccd ccd = ccdService.getById(cdid);
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    sendcdstartsqselectfz(chatId, cdid, cExecBot, update.getCallbackQuery().getMessage().getMessageId());
                }
                case "cdstartsqadd" -> {
                    Long cdid = Long.valueOf(split[1]);
                    Ccd ccd = ccdService.getById(cdid);
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    long count = cchannelhtService.countByCdidYTG(Math.toIntExact(cdid));
                    long l = Long.parseLong(cmydataService.getValueByMyKey("cdrl"));
                    if (count >= l) {
                        Msg.answerCallbackQueryALert(update, "当前车队成员已满", cExecBot);
                        return;
                    }
                    //判断是否有满足的频道
                    List<Cpermissions> listByInviteuserid = cpermissionsService.findListByInviteuserid(chatId);
                    if (listByInviteuserid == null || listByInviteuserid.isEmpty()) {
                        Msg.answerCallbackQueryALert(update, CStr.str10, cExecBot);
                        return;
                    }
                    boolean flag = false;
                    for (Cpermissions cpermissions : listByInviteuserid) {
                        if (cpermissions.getCount() > ccd.getMinisubscription()) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        Msg.answerCallbackQueryALert(update, CStr.str31, cExecBot);
                        return;
                    }
                    List<Cchannelht> listBySubmitterid = cchannelhtService.getListBySubmitterid(chatId);
                    //将listByInviteuserid中chatid与listBySubmitterid中cdid相同的元素剔除
                    if (listBySubmitterid == null || listBySubmitterid.isEmpty()) {
                        sendcdstartsqadd(chatId, cdid, cExecBot, update.getCallbackQuery().getMessage().getMessageId());
                    } else {
                        HashSet<String> chatids = new HashSet<>();
                        for (Cpermissions cpermissions : listByInviteuserid) {
                            chatids.add(cpermissions.getChatid());
                        }
                        for (Cchannelht cchannelht : listBySubmitterid) {
                            chatids.remove(cchannelht.getChatid());
                        }
                        if (chatids.isEmpty()) {
                            Msg.answerCallbackQueryALert(update, CStr.str10, cExecBot);
                        } else {
                            sendcdstartsqadd(chatId, cdid, cExecBot, update.getCallbackQuery().getMessage().getMessageId());
                        }
                    }
                }
                case "cdstartsqaddpd" -> {
                    Long cdid = Long.valueOf(split[1]);
                    String chatid = split[2];
                    Ccd ccd = ccdService.getById(cdid);
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    Cchannelht cchannelht = cchannelhtService.getByChatid(chatid);
                    if (cchannelht != null) {
                        Msg.answerCallbackQuery(update, "频道已存在", cExecBot);
                        return;
                    }
                    long count = cchannelhtService.countByCdidYTG(Math.toIntExact(cdid));
                    long l = Long.parseLong(cmydataService.getValueByMyKey("cdrl"));
                    if (count >= l) {
                        Msg.answerCallbackQuery(update, "当前车队成员已满", cExecBot);
                        return;
                    }
                    Cpermissions oneByChatId = cpermissionsService.getOneByChatId(Long.valueOf(chatid));
                    if (oneByChatId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    cchannelht = new Cchannelht();
                    cchannelht.setTitle(oneByChatId.getTitle());
                    cchannelht.setCdid(ccd.getId());
                    cchannelht.setChatid(chatid);
                    cchannelht.setSubmitterid(oneByChatId.getInviteuserid());
                    int channelCount = spidersUtils.getChannelCount(oneByChatId.getUrl());
                    if (channelCount == 0) {
                        int channelCount1 = spidersUtils.getChannelCount(oneByChatId.getInviteurl());
                        if (channelCount1 == 0) {
                            int chatMembersCount = Msg.getChatMembersCount(chatId, cExecBot);
                            cchannelht.setCount(chatMembersCount);
                        } else {
                            cchannelht.setCount(channelCount1);
                        }
                    } else {
                        cchannelht.setCount(channelCount);
                    }
                    cchannelht.setCreatetime(TeTimeUtil.getNowTime());
                    cchannelht.setUrl(oneByChatId.getUrl());
                    cchannelht.setAudit("0");
                    cchannelht.setInvitelink(oneByChatId.getInviteurl());
                    cchannelhtService.save(cchannelht);
                    Msg.answerCallbackQuery(update, "提交成功", cExecBot);
                    //15秒后再次同步邀请链接
                    scheduler.schedule(() -> {
                        Cchannelht byChatid = cchannelhtService.getByChatid(chatid);
                        Cpermissions oneByChatId1 = cpermissionsService.getOneByChatId(chatId);
                        if (byChatid != null && oneByChatId1 != null) {
                            byChatid.setInvitelink(oneByChatId1.getInviteurl());
                            cchannelhtService.updateById(byChatid);
                        }
                    }, 15, TimeUnit.SECONDS);
                    sendcdstartsqadd(chatId, cdid, cExecBot, update.getCallbackQuery().getMessage().getMessageId());
                    //发送频道审核通知
                    sendChannelAuditNotice(cExecBot, cchannelht.getId(), "sendnew", null);
                    Msg.sendMsgHTML("您提交的频道" + TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()) + "已提交审核，请耐心等待审核结果。", cExecBot, chatId);
                }
                case "cdstartglmypddetail" -> {
                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                    Long id = Long.parseLong(split[1]);
                    Cchannelht cchannelht = cchannelhtService.getById(id);
                    if (cchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    Cpermissions oneByChatId = cpermissionsService.getOneByChatId(Long.valueOf(cchannelht.getChatid()));
                    Ccd byId = ccdService.getById(cchannelht.getCdid());
                    StringBuilder msgText = new StringBuilder("频道详情");
                    msgText.append("\n频道标题: ").append(TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()));
                    msgText.append("\n频道ID: ").append(cchannelht.getChatid());
                    msgText.append("\n提交者ID: ").append(cchannelht.getSubmitterid());
                    msgText.append("\n车队: ").append(byId.getTitle());
                    msgText.append("\n人数: ").append(cchannelht.getCount());
                    msgText.append("\n审核状态: ").append(cchannelht.getAudit().equals("1") ? "通过" : "审核中");
                    msgText.append("\n管理权限: ").append(oneByChatId.getMsgandinvqx().equals("1") ? "有" : "无");
                    msgText.append("\n5日进粉统计: ");
                    List<String> beforeDays = TeTimeUtil.getBeforeDays(5);
                    List<Cfansrecordsin> listByChatId = cfansrecordsinService.findListByChatId(cchannelht.getChatid());
                    for (String beforeDay : beforeDays) {
                        int count = 0;
                        for (Cfansrecordsin cfansrecordsin : listByChatId) {
                            if (beforeDay.equals(cfansrecordsin.getDate())) {
                                count = cfansrecordsin.getFanscount();
                                break;
                            }
                        }
                        msgText.append("\n").append(beforeDay).append("：").append(count).append("人");
                    }
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(CStr.str17, "callback", "cdstartglmypdexit#" + id));
                    list1.add(new MyButton(CStr.str30, "callback", "cdstartglmypdedittitle#" + id));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton(CStr.str0713, "callback", "cdstartreturnmenu"));
                    list2.add(new MyButton(CStr.str08, "callback", "cdstartglmypd"));
                    lists.add(list2);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText.toString(), cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                }
                case "cdstartglmypdedittitle" -> {
                    String id = split[1];
                    Cchannelht cchannelht = cchannelhtService.getById(id);
                    if (cchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    if (!chatId.toString().equals(cchannelht.getSubmitterid())) {
                        Msg.answerCallbackQuery(update, "无权限操作", cExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    Msg.sendMsgForceReplyKeyboard(cchannelht.getChatid() + "\n请输入新的频道标题", cExecBot, chatId);
                }
                case "cdstartglmypdexit" -> {
                    String id = split[1];
                    Cchannelht cchannelht = cchannelhtService.getById(id);
                    if (cchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    if (!chatId.toString().equals(cchannelht.getSubmitterid())) {
                        Msg.answerCallbackQuery(update, "无权限操作", cExecBot);
                        return;
                    }
                    cchannelhtService.removeById(id);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(cchannelht.getChatid());
                    cchannelmsgService.removeByChatids(strings);
                    cfansrecordsinService.removeByChatids(strings);
                    Msg.leaveChat(Long.valueOf(cchannelht.getChatid()), cExecBot);
                    Msg.answerCallbackQuery(update, "退出频道成功", cExecBot);
                    sendcdstartglmypd(cExecBot, update, 1);
                }
                case "cdstartglmypdprev", "cdstartglmypdnext" -> {
                    String current = split[1];
                    sendcdstartglmypd(cExecBot, update, Integer.parseInt(current));
                }

            }
        } else {
            switch (data) {
                case "cdstartvive" -> {

                }
                case "cdstartsq" -> {
                    sendfzsq(update.getCallbackQuery().getMessage().getChatId(), cExecBot, update.getCallbackQuery().getMessage().getMessageId(), 1);
                }
                case "cdstartreturnmenu" -> {
                    sendStartMain(update.getCallbackQuery().getMessage().getChatId(), cExecBot, "edit", update.getCallbackQuery().getMessage().getMessageId());
                }
                case "cdstartglmypd" -> {
                    sendcdstartglmypd(cExecBot, update, 1);
                }
            }
        }
    }

    //todo 我的频道管理
    private void sendcdstartglmypd(CExecBot cExecBot, Update update, int current) {
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        List<Cchannelht> listBySubmitterId = cchannelhtService.findListBySubmitterId(chatId);
        if (listBySubmitterId == null || listBySubmitterId.isEmpty()) {
            Msg.answerCallbackQuery(update, "没有数据", cExecBot);
            return;
        } else {
            Page<Cchannelht> pagesBySubmitterId = cchannelhtService.getPagesBySubmitterId(chatId, current);
            if (pagesBySubmitterId.getRecords() == null || pagesBySubmitterId.getRecords().isEmpty()) {
                Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                return;
            } else {
                List<Cchannelht> records = pagesBySubmitterId.getRecords();
                StringBuilder message = new StringBuilder();
                message.append("我的车队频道列表");
                int i = 1;
                List<Cpermissions> cpermissionsList = cpermissionsService.list();
                List<Ccd> list = ccdService.list();
                for (Cchannelht cchannelht : records) {
                    message.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()));
                    message.append(" - ").append(TeNumberUtil.formatNumber(cchannelht.getCount()));
                    for (Ccd ccd : list) {
                        if (cchannelht.getCdid().equals(ccd.getId())) {
                            message.append(" - ").append(ccd.getTitle());
                            break;
                        }
                    }
                    if (cchannelht.getAudit().equals("1")) {
                        message.append("\n").append("✅审核通过");
                    } else {
                        message.append("\n").append("\uD83D\uDEAB审核中");
                    }
                    i++;
                    for (Cpermissions cpermissions : cpermissionsList) {
                        if (cpermissions.getChatid().equals(cchannelht.getChatid())) {
                            if (cpermissions.getMsgandinvqx().equals("1")) {
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
                            Cchannelht cchannelht = records.get(j * 3 + k);
                            list2.add(new MyButton((j * 3 + k + 1) + "", "callback", "cdstartglmypddetail#" + cchannelht.getId()));
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
                list99.add(new MyButton(CStr.str0713, "callback", "cdstartreturnmenu"));
                lists.add(list99);
                InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                Msg.editMsgAndKeyboard(message.toString(), cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
            }
        }
    }

    /**
     * 发送审核
     *
     * @param cExecBot
     * @param id
     * @param sendMode
     * @param messageId
     */
    private void sendChannelAuditNotice(CExecBot cExecBot, Integer id, String sendMode, Integer messageId) {
        Cchannelht byId = cchannelhtService.getById(id);
        if (byId == null) {
            return;
        }
        Integer cdid = byId.getCdid();
        Ccd ccd = ccdService.getById(cdid);
        String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
        msgText += "\n频道ID: " + byId.getChatid();
        msgText += "\n提交者ID: " + byId.getSubmitterid();
        msgText += "\n车队: " + ccd.getTitle();
        msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(ccd.getMinisubscription());
        msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(ccd.getMiniread());
        msgText += "\n人数: " + byId.getCount();
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(CStr.str11, "callback", "shtg#" + id));
        list1.add(new MyButton(CStr.str12, "callback", "shjj#" + id));
        list1.add(new MyButton(CStr.str13, "callback", "shjh#" + id));
        lists.add(list1);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        String shqid = cmydataService.getValueByMyKey("shqid");
        if (sendMode.equals("sendnew") && messageId == null) {
            Msg.sendMsgAndKeyboard(msgText, cExecBot, "html", Long.valueOf(shqid), inlineKeyboardMarkup, null);
        } else {
            Msg.editMsgAndKeyboard(msgText, cExecBot, "html", Long.valueOf(shqid), messageId, inlineKeyboardMarkup, null);
        }

    }

    private void sendcdstartsqadd(Long chatId, Long cdid, CExecBot cExecBot, Integer messageId) {
        Ccd ccd = ccdService.getById(cdid);
        StringBuilder msgText = new StringBuilder();
        msgText.append("请选择频道");
        msgText.append("\n频道标题: ").append(ccd.getTitle());
        msgText.append("\n频道介绍: ").append(ccd.getCddesc());
        msgText.append("\n车队类型:").append(ccd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(ccd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(ccd.getMiniread()));
        msgText.append("\n请确认是否已经将机器人拉入频道并授予管理员权限");
        msgText.append("\n请选择一个提交");
        List<List<MyButton>> lists = new ArrayList<>();
        List<Cpermissions> listByInviteuserid = cpermissionsService.findListByInviteuserid(chatId);
        List<Cchannelht> listBySubmitterid = cchannelhtService.getListBySubmitterid(chatId);
        if (listBySubmitterid == null || listBySubmitterid.isEmpty()) {
            for (Cpermissions cpermissions : listByInviteuserid) {
                List<MyButton> list1 = new ArrayList<>();
                list1.add(new MyButton(cpermissions.getTitle(), "callback", "cdstartsqaddpd#" + cdid + "#" + cpermissions.getChatid()));
                lists.add(list1);
            }
        } else {
            HashSet<String> chatids = new HashSet<>();
            for (Cpermissions cpermissions : listByInviteuserid) {
                if (cpermissions.getCount() > ccd.getMinisubscription()) {
                    chatids.add(cpermissions.getChatid());
                }
            }
            for (Cchannelht cchannelht : listBySubmitterid) {
                chatids.remove(cchannelht.getChatid());
            }
            for (String chatid : chatids) {
                List<MyButton> list1 = new ArrayList<>();
                String title = "";
                for (Cpermissions cpermissions : listByInviteuserid) {
                    if (cpermissions.getChatid().equals(chatid)) {
                        title = cpermissions.getTitle();
                        break;
                    }
                }
                list1.add(new MyButton(title, "callback", "cdstartsqaddpd#" + cdid + "#" + chatid));
                lists.add(list1);
            }
        }
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "cdstartreturnmenu"));
        list99.add(new MyButton(CStr.str08, "callback", "cdstartsq"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), cExecBot, null, chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendcdstartsqselectfz(Long chatId, Long ccdid, CExecBot cExecBot, Integer messageId) {
        StringBuilder msgText = new StringBuilder();
        Ccd ccd = ccdService.getById(ccdid);
        Long count = cchannelhtService.lambdaQuery().eq(Cchannelht::getCdid, ccdid).count();
        long l = Long.parseLong(cmydataService.getValueByMyKey("cdrl"));
        msgText.append("车队标题:").append(ccd.getTitle());
        msgText.append("\n车队介绍:").append(ccd.getCddesc());
        msgText.append("\n当前/最大(成员):").append(count).append("/").append(l);
        msgText.append("\n车队类型:").append(ccd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(ccd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(ccd.getMiniread()));
        msgText.append("\n成员列表:");
        List<Cchannelht> cchannelhtslist = cchannelhtService.findListByCdid(ccdid);
        if (cchannelhtslist == null || cchannelhtslist.isEmpty()) {
            msgText.append("\n当前没有互推成员");
        } else {
            int i = 1;
            for (Cchannelht cchannelht : cchannelhtslist) {
                msgText.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(cchannelht.getInvitelink(), cchannelht.getTitle()));
                msgText.append(" - ").append(TeNumberUtil.formatNumber(cchannelht.getCount()));
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
            list1.add(new MyButton(CStr.str09, "callback", "cdstartsqadd#" + ccdid));
            lists.add(list1);
        }

        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "cdstartreturnmenu"));
        list99.add(new MyButton(CStr.str08, "callback", "cdstartsq"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    /**
     * 分组申请选择界面
     *
     * @param chatId
     * @param cExecBot
     * @param messageId
     * @param current
     */
    private void sendfzsq(Long chatId, CExecBot cExecBot, Integer messageId, int current) {
        long count = ccdService.count();
        long pdcount = cchannelhtService.count();
        String msgText = "频道-车队大厅";
        msgText += "\n车队数量:" + count;
        msgText += "\n频道数量:" + pdcount;
        msgText += "\n图标介绍:";
        msgText += "\n\uD83D\uDFE2未满/\uD83D\uDD34已满|队内成员数-最大成员数|最小订阅需求";
        msgText += "\n选择下方车队进入指定车队，然后选择你要申请的频道进行上车提交。";
        Page<Ccd> page = ccdService.pageList(current);
        List<List<MyButton>> lists = new ArrayList<>();
        int cdrl = Integer.parseInt(cmydataService.getValueByMyKey("cdrl"));
//        for (Ccd ccd : page.getRecords()) {
//            List<MyButton> list1 = new ArrayList<>();
//            Long count1 = cchannelhtService.lambdaQuery().eq(Cchannelht::getCdid, ccd.getId()).count();
//            String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + ccd.getTitle() + "|"
//                    + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(ccd.getMinisubscription());
//            list1.add(new MyButton(buttonName, "callback", "cdstartsqselectfz#" + ccd.getId()));
//            lists.add(list1);
//        }
        int size = page.getRecords().size() / 2 + (page.getRecords().size() % 2 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                if (j * 2 + k < page.getRecords().size()) {
                    Ccd ccd = page.getRecords().get(j * 2 + k);
                    Long count1 = cchannelhtService.lambdaQuery().eq(Cchannelht::getCdid, ccd.getId()).count();
                    String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + ccd.getTitle() + "|"
                            + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(ccd.getMinisubscription());
                    list2.add(new MyButton(buttonName, "callback", "cdstartsqselectfz#" + ccd.getId()));
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
        list99.add(new MyButton(CStr.str0713, "callback", "cdstartreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, cExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    //todo gl开头回调分支
    private void handlestartsWithglCallbackQuery(Update update, CExecBot cExecBot) {
        String data = update.getCallbackQuery().getData();
        if (data.equals("glreturnmenu")) {
            myGlMenu(update.getCallbackQuery().getMessage().getChatId(), cExecBot, "edit", update.getCallbackQuery().getMessage().getMessageId());
        }
        if (data.startsWith("glfz")) {
            handlestartsWithglfzCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glht")) {
            handlestartsWithglhtCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glpd")) {
            handlestartsWithglpdCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glad")) {
            handlestartsWithgladCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glsendfrequency")) {
            handlestartsWithglsendfrequencyCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glrefreshcount")) {
            handlestartsWithglrefreshcountCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glcheckpd")) {
            handlestartsWithglcheckpdCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("gluserused")) {
            handlestartsWithgluserusedCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("gltgbutton")) {
            handlestartsWithgltgbuttonCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glwa")) {
            handlestartsWithglwaCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glbb")) {
            handlestartsWithglbbCallbackQuery(update, cExecBot);
            return;
        }

    }

    //todo 处理报表相关回调
    private void handlestartsWithglbbCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glbb" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    List<Ccd> list = ccdService.list();
                    List<Cchannelht> listByAudit = cchannelhtService.findListByAudit("1");
                    List<Cfansrecordsin> list1 = cfansrecordsinService.list();
                    for (Ccd ccd : list) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("车队:").append(ccd.getTitle());
                        int i = 1;
                        for (Cchannelht cchannelht : listByAudit) {
                            if (cchannelht.getCdid().equals(ccd.getId())) {
                                sb.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()));
                                int today = 0;
                                for (Cfansrecordsin cfansrecordsin : list1) {
                                    if (cfansrecordsin.getChatid().equals(cchannelht.getChatid()) && cfansrecordsin.getDate().equals(TeTimeUtil.getNowTime())) {
                                        today = cfansrecordsin.getFanscount();
                                        break;
                                    }
                                }
                                int total = 0;
                                for (Cfansrecordsin cfansrecordsin : list1) {
                                    if (cfansrecordsin.getChatid().equals(cchannelht.getChatid())) {
                                        total += cfansrecordsin.getFanscount();
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
                        Msg.sendMsgHTML(sb.toString(), cExecBot, chatId);
                    }
                }
            }
        }
    }

    /**
     * @param update
     * @param cExecBot
     */
    //todo 管理文案回调
    private void handlestartsWithglwaCallbackQuery(Update update, CExecBot cExecBot) {
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
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    Msg.sendMsgMarkdown(msg, cExecBot, chatId);
                }
                case "glwavive" -> {
                    String key = split[1];
                    Ccopywriter oneByTextkey = switch (key) {
                        case "1" -> ccopywriterService.getOneByTextkey("start01");
                        case "2" -> ccopywriterService.getOneByTextkey("start02");
                        case "3" -> ccopywriterService.getOneByTextkey("help");
                        default -> null;
                    };
                    if (oneByTextkey == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    List<MessageEntity> entities = null;
                    if (StrUtil.isNotBlank(oneByTextkey.getTextentities())) {
                        entities = JSONUtil.toList(JSONUtil.parseArray(oneByTextkey.getTextentities()), MessageEntity.class);
                    }
                    Msg.sendMsgAndEntities(oneByTextkey.getTextcontent(), cExecBot, chatId, entities);
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
                    list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
                    lists.add(list99);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, cExecBot, chatId, messageId, inlineKeyboardMarkup, null);
                }
            }
        }

    }

    private void handlestartsWithgltgbuttonCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "gltgbutton" -> {
                    sendgltgbutton(chatId, cExecBot, messageId);
                }
                case "gltgbuttonvive" -> {
                    String mybutton = cmydataService.getValueByMyKey("mybutton");
                    if (StrUtil.isBlank(mybutton)) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    InlineKeyboardMarkup inlineKeyboardMarkupString = Msg.getInlineKeyboardMarkupString(mybutton);
                    if (inlineKeyboardMarkupString == null) {
                        Msg.answerCallbackQuery(update, "按钮格式错误", cExecBot);
                        return;
                    } else {
                        Msg.answerCallbackQueryNull(update, cExecBot);
                        String msgText = "按钮预览：\n" + mybutton;
                        Msg.sendMsgAndKeyboard(msgText, cExecBot, chatId, inlineKeyboardMarkupString, null);
                    }
                }
                case "gltgbuttonedit" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
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
                    Msg.sendMsgMarkdown(msgText, cExecBot, chatId);
                }
                case "gltgbuttonclear" -> {
                    cmydataService.updateValueByKey("mybutton", "");
                    Msg.answerCallbackQuery(update, "已清除", cExecBot);
                }
            }
        }
    }

    private void sendgltgbutton(Long chatId, CExecBot cExecBot, Integer messageId) {
        String msgText = "管理推广按钮";
        //按钮
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(CStr.str25, "callback", "gltgbuttonvive"));
        list1.add(new MyButton(CStr.str26, "callback", "gltgbuttonedit"));
        list1.add(new MyButton(CStr.str27, "callback", "gltgbuttonclear"));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, cExecBot, chatId, messageId, inlineKeyboardMarkup, null);

    }

    private void handlestartsWithgluserusedCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "gluserused" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    try {
                        Long id = cExecBot.getMe().getId();
                        List<Cvisitors> listByBotId = cvisitorsService.findListByBotId(id);
                        if (listByBotId != null && !listByBotId.isEmpty()) {

                            HashSet<String> ids = new HashSet<>();
                            for (Cvisitors cvisitors : listByBotId) {
                                ids.add(cvisitors.getUserid());
                            }
                            int totalCount = ids.size();//总用户数
                            //统计今日访问用户数
                            int todayCount = 0;
                            String nowTime = TeTimeUtil.getNowTime();
                            for (Cvisitors cvisitors : listByBotId) {
                                if (cvisitors.getFwtime().equals(nowTime)) {
                                    todayCount++;
                                }
                            }
                            String msgText = "机器人总用户数：" + totalCount + "人，今日访问用户数：" + todayCount + "人。";
                            Msg.sendMsg(msgText, cExecBot, chatId);
                        }

                    } catch (TelegramApiException e) {
                        log.error("获取botId失败", e);
                    }
                }
            }
        }
    }

    private void handlestartsWithglcheckpdCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glcheckpd" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    String msgText = "正在检测。。。。。";
                    List<Cchannelht> list = cchannelhtService.list();
                    if (list != null && !list.isEmpty()) {
                        Msg.sendMsgMarkdown(msgText, cExecBot, chatId);
                        for (Cchannelht cchannelht : list) {
                            int channelCount1 = spidersUtils.getChannelCount(cchannelht.getUrl());
                            int channelCount2 = spidersUtils.getChannelCount(cchannelht.getInvitelink());
                            if (channelCount1 == 0 && channelCount2 == 0) {
                                Msg.sendMsgHTML("频道id" + cchannelht.getChatid() + "：" + TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()) + "疑似失效，请检查。", cExecBot, chatId);
                                Msg.sendMsgMarkdown("私聊机器人输入以下内容可以删除该频道:\n" + "`/glpddelete " + cchannelht.getId() + "`", cExecBot, chatId);
                            }
                            log.info("频道id{}：{}检测结果：{}|{}", cchannelht.getChatid(), TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()), channelCount1, channelCount2);
                        }
                        Msg.sendMsg("检测完成", cExecBot, chatId);
                    } else {
                        Msg.answerCallbackQuery(update, "暂无数据", cExecBot);
                        return;
                    }
                }
            }
        }
    }

    private void handlestartsWithglrefreshcountCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glrefreshcount" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    String msgText = "刷新频道/群组成员数量中...";
                    Msg.sendMsg(msgText, cExecBot, chatId);
                    cchannelhtService.refreshCount();
                    Msg.sendMsg("已刷新", cExecBot, chatId);
                }
            }
        }
    }

    private void handlestartsWithglsendfrequencyCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
        } else {
            switch (data) {
                case "glsendfrequency" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    String msgText = "请编辑以下模版发送：（备注，频率单位为分钟）";
                    msgText += "\n`频道发送频率修改";
                    msgText += "\n频率:30`";
                    Msg.sendMsgMarkdown(msgText, cExecBot, chatId);
                }
            }
        }
    }


    private void handlestartsWithgladCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "gladstatus" -> {
                    Long id = Long.parseLong(split[1]);
                    Cad cad = cadService.getById(id);
                    if (cad == null) {
                        Msg.answerCallbackQuery(update, "广告不存在", cExecBot);
                        return;
                    }
                    if (cad.getStatus().equals("1")) {
                        cad.setStatus("0");
                        cadService.updateById(cad);
                    } else {
                        cad.setStatus("1");
                        cadService.updateById(cad);
                    }
                    sendGladMenu(chatId, cExecBot, messageId);
                }
                case "gladdelete" -> {
                    Long id = Long.parseLong(split[1]);
                    Cad cad = cadService.getById(id);
                    if (cad == null) {
                        Msg.answerCallbackQuery(update, "广告不存在", cExecBot);
                        return;
                    }
                    cadService.removeById(id);
                    sendGladMenu(chatId, cExecBot, messageId);
                }
            }
        } else {
            switch (data) {
                case "glad" -> {
                    sendGladMenu(chatId, cExecBot, messageId);
                }
                case "gladadd" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    String msgText = "请编辑以下模版发送：（备注，位置1代表消息头部，2代表尾部）";
                    msgText += "\n`广告添加";
                    msgText += "\n标题:xxx";
                    msgText += "\nurl:xxx";
                    msgText += "\n位置:1`";
                    Msg.sendMsgMarkdown(msgText, cExecBot, chatId);
                }
            }
        }
    }

    private void sendGladMenu(Long chatId, CExecBot cExecBot, Integer messageId) {
        String msgText = "广告管理";
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(CStr.str19, "callback", "gladadd"));
        lists.add(list1);
        List<Cad> allByAdtype = cadService.findAllByAdtype("1");
        if (allByAdtype != null && !allByAdtype.isEmpty()) {
            for (Cad cad : allByAdtype) {
                List<MyButton> list2 = new ArrayList<>();
                list2.add(new MyButton(cad.getTitle(), "url", cad.getContent()));
                if (cad.getFlag().equals("1")) {
                    list2.add(new MyButton(CStr.str23, "callback", "null"));
                } else {
                    list2.add(new MyButton(CStr.str24, "callback", "null"));
                }
                if (cad.getStatus().equals("1")) {
                    list2.add(new MyButton(CStr.str21, "callback", "gladstatus#" + cad.getId()));
                } else {
                    list2.add(new MyButton(CStr.str22, "callback", "gladstatus#" + cad.getId()));
                }
                list2.add(new MyButton(CStr.str20, "callback", "gladdelete#" + cad.getId()));
                lists.add(list2);
            }
        }
        List<Cad> allByAdtypew = cadService.findAllByAdtype("2");
        if (allByAdtypew != null && !allByAdtypew.isEmpty()) {
            for (Cad cad : allByAdtypew) {
                List<MyButton> list2 = new ArrayList<>();
                list2.add(new MyButton(cad.getTitle(), "url", cad.getContent()));
                if (cad.getFlag().equals("1")) {
                    list2.add(new MyButton(CStr.str23, "callback", "null"));
                } else {
                    list2.add(new MyButton(CStr.str24, "callback", "null"));
                }
                if (cad.getStatus().equals("1")) {
                    list2.add(new MyButton(CStr.str21, "callback", "gladstatus#" + cad.getId()));
                } else {
                    list2.add(new MyButton(CStr.str22, "callback", "gladstatus#" + cad.getId()));
                }
                list2.add(new MyButton(CStr.str20, "callback", "gladdelete#" + cad.getId()));
                lists.add(list2);
            }
        }
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, cExecBot, chatId, messageId, inlineKeyboardMarkup, null);

    }

    /**
     * 处理glpd开头的回调
     *
     * @param update
     * @param cExecBot
     */
    //todo 处理glpd开头的回调 管理频道
    private void handlestartsWithglpdCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glpdprev", "glpdnext" -> {
                    int current = Integer.parseInt(split[1]);
                    sendGlpdMenu(update, cExecBot, current);
                }
                case "glpddetail" -> {
                    Long id = Long.parseLong(split[1]);
                    Cchannelht cchannelht = cchannelhtService.getById(id);
                    if (cchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    Cpermissions oneByChatId = cpermissionsService.getOneByChatId(Long.valueOf(cchannelht.getChatid()));
                    Ccd byId = ccdService.getById(cchannelht.getCdid());
                    StringBuilder msgText = new StringBuilder("频道详情");
                    msgText.append("\n频道标题: ").append(TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()));
                    msgText.append("\n频道ID: ").append(cchannelht.getChatid());
                    msgText.append("\n提交者ID: ").append(cchannelht.getSubmitterid());
                    msgText.append("\n车队: ").append(byId.getTitle());
                    msgText.append("\n人数: ").append(cchannelht.getCount());
                    msgText.append("\n审核状态: ").append(cchannelht.getAudit().equals("1") ? "通过" : "审核中");
                    msgText.append("\n管理权限: ").append(oneByChatId.getMsgandinvqx().equals("1") ? "有" : "无");
                    msgText.append("\n5日进粉统计: ");
                    List<String> beforeDays = TeTimeUtil.getBeforeDays(5);
                    List<Cfansrecordsin> listByChatId = cfansrecordsinService.findListByChatId(cchannelht.getChatid());
                    for (String beforeDay : beforeDays) {
                        int count = 0;
                        for (Cfansrecordsin cfansrecordsin : listByChatId) {
                            if (beforeDay.equals(cfansrecordsin.getDate())) {
                                count = cfansrecordsin.getFanscount();
                                break;
                            }
                        }
                        msgText.append("\n").append(beforeDay).append("：").append(count).append("人");
                    }
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(CStr.str17, "callback", "glpdexit#" + id));
                    list1.add(new MyButton(CStr.str28, "callback", "glpdreurl#" + id));
                    lists.add(list1);
                    List<MyButton> list2 = new ArrayList<>();
                    list2.add(new MyButton(CStr.str0713, "callback", "glreturnmenu#" + id));
                    list2.add(new MyButton(CStr.str08, "callback", "glpd"));
                    lists.add(list2);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText.toString(), cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                }
                case "glpdreurl" -> {
                    String id = split[1];
                    Cchannelht cchannelht = cchannelhtService.getById(id);
                    if (cchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    Cpermissions oneByChatId = cpermissionsService.getOneByChatId(Long.valueOf(cchannelht.getChatid()));
                    if (oneByChatId.getMsgandinvqx().equals("0")) {
                        Msg.answerCallbackQuery(update, "无管理权限", cExecBot);
                        return;
                    }
                    String inviteLink = Msg.getInviteLink(Long.parseLong(cchannelht.getChatid()), cExecBot);
                    if (StrUtil.isNotBlank(inviteLink)) {
                        Msg.answerCallbackQueryNull(update, cExecBot);
                        oneByChatId.setInviteurl(inviteLink);
                        cpermissionsService.updateById(oneByChatId);
                        cchannelht.setInvitelink(inviteLink);
                        cchannelhtService.updateById(cchannelht);
                        Msg.sendMsg("频道邀请链接已更新,链接为:" + inviteLink, cExecBot, chatId);
                    } else {
                        Msg.answerCallbackQuery(update, "生成邀请链接失败", cExecBot);
                    }
                    return;
                }
                case "glpdexit" -> {
                    String id = split[1];
                    Cchannelht cchannelht = cchannelhtService.getById(id);
                    if (cchannelht == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    cchannelhtService.removeById(id);
                    HashSet<String> strings = new HashSet<>();
                    strings.add(cchannelht.getChatid());
                    cchannelmsgService.removeByChatids(strings);
                    cfansrecordsinService.removeByChatids(strings);
                    Msg.leaveChat(Long.valueOf(cchannelht.getChatid()), cExecBot);
                    Msg.answerCallbackQuery(update, "退出频道成功", cExecBot);
                    sendGlpdMenu(update, cExecBot, 1);
                }
            }
        } else {
            switch (data) {
                case "glpd" -> {
                    sendGlpdMenu(update, cExecBot, 1);
                }
            }
        }
    }

    private void sendGlpdMenu(Update update, CExecBot cExecBot, int current) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        StringBuilder message = new StringBuilder("管理频道");
        Page<Cchannelht> cchannelhtPage = cchannelhtService.listOrderByCdid(current);
        if (cchannelhtPage.getRecords() == null || cchannelhtPage.getRecords().isEmpty()) {
            Msg.answerCallbackQuery(update, "没有可管理的频道", cExecBot);
            return;
        }
        int i = 1;
        List<Cpermissions> cpermissionsList = cpermissionsService.list();
        List<Ccd> list = ccdService.list();
        for (Cchannelht cchannelht : cchannelhtPage.getRecords()) {
            message.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(cchannelht.getUrl(), cchannelht.getTitle()));
            message.append(" - ").append(TeNumberUtil.formatNumber(cchannelht.getCount()));
            for (Ccd ccd : list) {
                if (cchannelht.getCdid().equals(ccd.getId())) {
                    message.append(" - ").append(ccd.getTitle());
                    break;
                }
            }
            if (cchannelht.getAudit().equals("1")) {
                message.append("\n").append("✅审核通过");
            } else {
                message.append("\n").append("\uD83D\uDEAB审核中");
            }
            i++;
            for (Cpermissions cpermissions : cpermissionsList) {
                if (cpermissions.getChatid().equals(cchannelht.getChatid())) {
                    if (cpermissions.getMsgandinvqx().equals("1")) {
                        message.append("✅有管理权限");
                    } else {
                        message.append("\uD83D\uDEAB无管理权限");
                    }
                    break;
                }
            }
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<Cchannelht> records = cchannelhtPage.getRecords();
        //每行三个
        int size = records.size() / 3 + (records.size() % 3 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 3; k++) {
                if (j * 3 + k < records.size()) {
                    Cchannelht cchannelht = records.get(j * 3 + k);
                    list2.add(new MyButton((j * 3 + k + 1) + "", "callback", "glpddetail#" + cchannelht.getId()));
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
        list3.add(new MyButton(current + "/" + cchannelhtPage.getPages() + "页", "callback", "null"));
        if (cchannelhtPage.getPages() > current) {//存在下一页
            list3.add(new MyButton("➡\uFE0F下一页", "callback", "glpdnext" + "#" + (current + 1)));
        } else {
            list3.add(new MyButton("-", "callback", "null"));
        }
        lists.add(list3);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(message.toString(), cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    /**
     * 处理glht开头的回调
     *
     * @param update
     * @param cExecBot
     */
    //todo 处理glht开头的回调 启动互推、关闭互推
    private void handlestartsWithglhtCallbackQuery(Update update, CExecBot cExecBot) {
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
                    String cdstatus = cmydataService.getValueByMyKey("cdstatus");
                    if (cdstatus.equals("1")) {
                        Msg.answerCallbackQuery(update, CStr.str14, cExecBot);
                        myGlMenu(chatId, cExecBot, "edit", messageId);
                        return;
                    }
                    // 定时任务
                    //todo 定时任务主体

                    Runnable pushTask = () -> {
                        String cdname = cmydataService.getValueByMyKey("cdname");
                        List<Ccd> ccdList = ccdService.list();//获取所有车队
                        if (ccdList == null || ccdList.isEmpty()) {
                            Msg.answerCallbackQuery(update, "车队为空，无法启动", cExecBot);
                            return;
                        }
                        List<Cchannelht> cchannelhtList = cchannelhtService.findListByAudit("1");
                        if (cchannelhtList == null || cchannelhtList.isEmpty()) {
                            Msg.answerCallbackQuery(update, "没有需要互推的频道", cExecBot);
                            return;
                        }
                        boolean flag = false;
                        for (Cchannelht cchannelht : cchannelhtList) {
                            String invitelink = cchannelht.getInvitelink();
                            if (StrUtil.isBlank(invitelink)) {
                                String inviteLink = Msg.getInviteLink(Long.parseLong(cchannelht.getChatid()), cExecBot);
                                if (StrUtil.isNotBlank(inviteLink)) {
                                    cchannelht.setInvitelink(inviteLink);
                                    cchannelhtService.updateById(cchannelht);
                                    flag = true;
                                }
                            }
                        }
                        if (flag) {
                            cchannelhtList = cchannelhtService.findListByAudit("1");
                        }
                        List<List<Cchannelht>> cchannelhtslists = new ArrayList<>();
                        for (Ccd ccd : ccdList) {//根据不同车队分组
                            List<Cchannelht> cchannelhtslist = new ArrayList<>();
                            for (Cchannelht cchannelht : cchannelhtList) {
                                if (cchannelht.getCdid().equals(ccd.getId())) {
                                    cchannelhtslist.add(cchannelht);
                                }
                            }
                            if (!cchannelhtslist.isEmpty() && cchannelhtslist.size() >= 2) {
                                cchannelhtslists.add(cchannelhtslist);
                            }
                        }
                        List<ChannelHtInfo> channelHtInfoList = new ArrayList<>();
                        for (List<Cchannelht> cchannelhtslist : cchannelhtslists) {//根据分组进行互推
                            for (Cchannelht cchannelht : cchannelhtslist) {//对组内成员进行互推
                                ChannelHtInfo channelHtInfo = new ChannelHtInfo();
                                channelHtInfo.setChatId(cchannelht.getChatid());
                                channelHtInfo.setInviteLink(cchannelht.getInvitelink());
                                channelHtInfo.setUrl(cchannelht.getUrl());
                                StringBuilder message = new StringBuilder();
                                String zuname = "\uD83D\uDE80来自"+cdname+"【";
                                for (Ccd ccd : ccdList) {
                                    if (cchannelht.getCdid().equals(ccd.getId())) {
                                        zuname += ccd.getTitle();
                                        channelHtInfo.setCdname(ccd.getTitle());
                                        break;
                                    }
                                }
                                zuname += "】组\uD83D\uDE80";
                                String botName = "xxxxxxxx";
                                try {
                                    botName = cExecBot.getMe().getUserName();
                                } catch (TelegramApiException e) {
                                    log.error("获取机器人用户名失败", e);
                                }
                                message.append("<b>").append(TeUrlUtil.getUrlExpression("https://t.me/" + botName, zuname)).append("</b>");
                                //头部广告
                                List<Cad> byAdtypeHead = cadService.findByAdtype("1");
                                if (byAdtypeHead != null && !byAdtypeHead.isEmpty()) {
                                    for (Cad cad : byAdtypeHead) {
                                        message.append("\n<b>AD:").append(TeUrlUtil.getUrlExpression(cad.getContent(), cad.getTitle())).append("</b>");
                                    }
                                }
                                message.append("\n\n");
                                int i = 1;
                                for (Cchannelht cchannelht1 : cchannelhtslist) {
                                    if (!cchannelht1.getId().equals(cchannelht.getId())) {
                                        message.append(i).append(".").append(TeUrlUtil.getUrlExpression(cchannelht1.getInvitelink(), cchannelht1.getTitle()));
                                        message.append("\n");
                                        i++;
                                    }
                                }
                                //尾部广告
                                List<Cad> byAdtypetail = cadService.findByAdtype("2");
                                if (byAdtypetail != null && !byAdtypetail.isEmpty()) {
                                    for (Cad cad : byAdtypetail) {
                                        message.append("\n<b>AD:").append(TeUrlUtil.getUrlExpression(cad.getContent(), cad.getTitle())).append("</b>");
                                    }
                                }
                                channelHtInfo.setMessage(message);
                                channelHtInfoList.add(channelHtInfo);
                            }
                        }
                        List<Cchannelmsg> list = cchannelmsgService.list();
                        String shqid = cmydataService.getValueByMyKey("shqid");
                        String mybutton = cmydataService.getValueByMyKey("mybutton");
                        InlineKeyboardMarkup inlineKeyboardMarkupString = null;
                        if (StrUtil.isNotBlank(mybutton)) {
                            inlineKeyboardMarkupString = Msg.getInlineKeyboardMarkupString(mybutton);
                        }
                        List<Cmydata> pdremove = cmydataService.findListByKey("pdremove");
                        for (ChannelHtInfo channelHtInfo : channelHtInfoList) {//往各个频道推送内容
                            for (Cchannelmsg cchannelmsg : list) {
                                if (cchannelmsg.getChatid().equals(channelHtInfo.getChatId())) {
                                    if (StrUtil.isNotBlank(cchannelmsg.getMessageid())) {
                                        Msg.deleteMessage(Long.parseLong(cchannelmsg.getChatid()), Integer.parseInt(cchannelmsg.getMessageid()), cExecBot);
                                    }
                                    HashSet<String> strings = Msg.sendMsgAndKeyboardHT(channelHtInfo.getMessage().toString(), cExecBot,
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
                                                    + "\n原因：" + string, cExecBot, Long.valueOf(shqid));
                                            if (pdremove != null && !pdremove.isEmpty()) {
                                                for (Cmydata cmydata : pdremove) {
                                                    if (string.contains(cmydata.getMyvalus())) {
                                                        flag2 = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    if (flag2) {//删除该频道
                                        Msg.sendMsg("检测到频道" + channelHtInfo.getUrl() + "符合移除规则,将自动移除该频道。", cExecBot, Long.valueOf(shqid));
                                        cchannelhtService.removeByChatid(channelHtInfo.getChatId());
                                        cchannelmsgService.deleteBychatId(channelHtInfo.getChatId());
                                        cfansrecordsinService.deleteBychatId(channelHtInfo.getChatId());
                                        cpermissionsService.deleteBychatId(channelHtInfo.getChatId());
                                        Msg.leaveChat(Long.parseLong(channelHtInfo.getChatId()), cExecBot);
                                        continue;
                                    }
                                    for (String string : strings) {
                                        if (!string.contains("消息发送失败")) {
                                            cchannelmsg.setMessageid(string);
                                            break;
                                        }
                                    }
                                    cchannelmsg.setCreatedate(TeTimeUtil.getNowTimeDetail());
                                    cchannelmsgService.updateById(cchannelmsg);
                                    break;
                                }
                            }
                        }
                    };
                    htscheduler = Executors.newScheduledThreadPool(1);
                    int resetmessagetime = Integer.parseInt(cmydataService.getValueByMyKey("resetmessagetime"));
                    htscheduler.scheduleAtFixedRate(pushTask, 0, resetmessagetime, TimeUnit.MINUTES);
                    cmydataService.updateValueByKey("cdstatus", "1");
                    myGlMenu(chatId, cExecBot, "edit", messageId);
                }
                case "glhtstop" -> {
                    String cdstatus = cmydataService.getValueByMyKey("cdstatus");
                    if (cdstatus.equals("0")) {
                        Msg.answerCallbackQuery(update, CStr.str15, cExecBot);
                        myGlMenu(chatId, cExecBot, "edit", messageId);
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
                        Msg.answerCallbackQuery(update, CStr.str16, cExecBot);
                        cmydataService.updateValueByKey("cdstatus", "0");
                        myGlMenu(chatId, cExecBot, "edit", messageId);
                    }
                }

            }
        }
    }

    /**
     * 处理glfz开头的回调
     *
     * @param update
     * @param cExecBot
     */
    //todo 处理glfz开头的回调 管理分组
    private void handlestartsWithglfzCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glfzmanagenext", "glfzmanageprev" -> {
                    sendglfzmanage(callbackQuery.getMessage().getChatId(), cExecBot, callbackQuery.getMessage().getMessageId(), Integer.parseInt(split[1]));
                }
                case "glfzmanagedetail" -> {
                    Ccd ccd = ccdService.getById(Long.valueOf(split[1]));
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    sendglfzmanagedetail(chatId, Long.valueOf(split[1]), cExecBot, messageId);
                }
                case "glfzedit" -> {
                    Ccd ccd = ccdService.getById(Long.valueOf(split[1]));
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    String msgText = "请编辑以下并发送：";
                    msgText += "\n`车队编辑";
                    msgText += "\n车队id:" + ccd.getId();
                    msgText += "\n车队标题:" + ccd.getTitle();
                    msgText += "\n车队描述:" + ccd.getCddesc();
                    msgText += "\n最低订阅量:" + ccd.getMinisubscription();
                    msgText += "\n最低阅读:" + ccd.getMiniread() + "`";
                    Msg.sendMsgMarkdown(msgText, cExecBot, chatId);
                }
                case "glfzdelete" -> {
                    Ccd ccd = ccdService.getById(Long.valueOf(split[1]));
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    String msgText = "请复制以下命令并发送：（注意：删除分组会删除分组下的所有频道信息，并将机器人移除所在频道。）\n`/glfzdelete " + ccd.getId() + "`";
                    Msg.sendMsgMarkdown(msgText, cExecBot, chatId);
                }
            }
        } else {//不带参数
            switch (data) {
                case "glfz" -> {
                    sendglfz(callbackQuery.getMessage().getChatId(), cExecBot, callbackQuery.getMessage().getMessageId());
                }
                case "glfzadd" -> {
                    Msg.answerCallbackQueryNull(update, cExecBot);
                    Msg.sendMsgAndEntitiesMd(CStr.str0716, cExecBot, callbackQuery.getMessage().getChatId(), null);
                }
                case "glfzmanage" -> {
                    sendglfzmanage(callbackQuery.getMessage().getChatId(), cExecBot, callbackQuery.getMessage().getMessageId(), 1);
                }
            }
        }
    }

    private void sendglfzmanagedetail(Long chatId, Long ccdid, CExecBot cExecBot, Integer messageId) {
        StringBuilder msgText = new StringBuilder();
        Ccd ccd = ccdService.getById(ccdid);
        Long count = cchannelhtService.lambdaQuery().eq(Cchannelht::getCdid, ccdid).count();
        long l = Long.parseLong(cmydataService.getValueByMyKey("cdrl"));
        msgText.append("车队标题:").append(ccd.getTitle());
        msgText.append("\n车队介绍:").append(ccd.getCddesc());
        msgText.append("\n当前/最大(成员):").append(count).append("/").append(l);
        msgText.append("\n车队类型:").append(ccd.getType().equals("channel") ? "频道" : "群组");
        msgText.append("\n最低订阅:").append(TeNumberUtil.formatNumber(ccd.getMinisubscription()));
        msgText.append("\n最低阅读:").append(TeNumberUtil.formatNumber(ccd.getMiniread()));
        msgText.append("\n成员列表:");
        List<Cchannelht> cchannelhtslist = cchannelhtService.findListByCdid(ccdid);
        if (cchannelhtslist == null || cchannelhtslist.isEmpty()) {
            msgText.append("\n当前没有互推成员");
        } else {
            int i = 1;
            for (Cchannelht cchannelht : cchannelhtslist) {
                msgText.append("\n").append(i).append(".").append(TeUrlUtil.getUrlExpression(cchannelht.getInvitelink(), cchannelht.getTitle()));
                msgText.append(" - ").append(TeNumberUtil.formatNumber(cchannelht.getCount()));
                i++;
            }
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton("编辑", "callback", "glfzedit#" + ccdid));
        list1.add(new MyButton("删除", "callback", "glfzdelete#" + ccdid));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
        list99.add(new MyButton(CStr.str08, "callback", "glfzmanage"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText.toString(), cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendglfzmanage(Long chatId, CExecBot cExecBot, Integer messageId, int current) {
        long count = ccdService.count();
        long pdcount = cchannelhtService.count();
        String msgText = "频道-车队管理";
        msgText += "\n车队数量:" + count;
        msgText += "\n频道数量:" + pdcount;
        msgText += "\n图标介绍:";
        msgText += "\n\uD83D\uDFE2未满/\uD83D\uDD34已满|队内成员数-最大成员数|最小订阅需求";
        Page<Ccd> page = ccdService.pageList(current);
        List<List<MyButton>> lists = new ArrayList<>();
        int cdrl = Integer.parseInt(cmydataService.getValueByMyKey("cdrl"));
//        for (Ccd ccd : page.getRecords()) {
//            List<MyButton> list1 = new ArrayList<>();
//            Long count1 = cchannelhtService.lambdaQuery().eq(Cchannelht::getCdid, ccd.getId()).count();
//            String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + ccd.getTitle() + "|"
//                    + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(ccd.getMinisubscription());
//            list1.add(new MyButton(buttonName, "callback", "glfzmanagedetail#" + ccd.getId()));
//            lists.add(list1);
//        }
        int size = page.getRecords().size() / 2 + (page.getRecords().size() % 2 > 0 ? 1 : 0);
        for (int j = 0; j < size; j++) {
            List<MyButton> list2 = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                if (j * 2 + k < page.getRecords().size()) {
                    Ccd ccd = page.getRecords().get(j * 2 + k);
                    Long count1 = cchannelhtService.lambdaQuery().eq(Cchannelht::getCdid, ccd.getId()).count();
                    String buttonName = (count1 == cdrl ? "\uD83D\uDD34" : "\uD83D\uDFE2") + ccd.getTitle() + "|"
                            + count1 + "/" + cdrl + "|" + TeNumberUtil.formatNumber(ccd.getMinisubscription());
                    list2.add(new MyButton(buttonName, "callback", "glfzmanagedetail#" + ccd.getId()));
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
        list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
        list99.add(new MyButton(CStr.str08, "callback", "glfz"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(msgText, cExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    private void sendglfz(Long chatId, CExecBot cExecBot, Integer messageId) {
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(CStr.str0714, "callback", "glfzadd"));
        list1.add(new MyButton(CStr.str0715, "callback", "glfzmanage"));
        lists.add(list1);
        List<MyButton> list99 = new ArrayList<>();
        list99.add(new MyButton(CStr.str0713, "callback", "glreturnmenu"));
        lists.add(list99);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        Msg.editMsgAndKeyboard(CStr.str0701, cExecBot, chatId, messageId, inlineKeyboardMarkup, null);
    }

    public void handleGroupCallbackQuery(Update update, CExecBot cExecBot) {
    }

    public void handleSuperGroupCallbackQuery(Update update, CExecBot cExecBot) {
        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getFrom().getId();
        if (data.startsWith("sh")) {
            Cteadmin oneByChatId = cteadminService.getOneByChatId(chatId);
            if (oneByChatId == null) {
                Msg.answerCallbackQuery(update, "无权限操作", cExecBot);
                return;
            }
            handleSuperGroupstartsWithshCallbackQuery(update, cExecBot);
            return;
        }
        if (data.startsWith("glpd")) {
            Cteadmin oneByChatId = cteadminService.getOneByChatId(chatId);
            if (oneByChatId == null) {
                Msg.answerCallbackQuery(update, "无权限操作", cExecBot);
                return;
            }
            handleSuperGroupstartsWithglpdCallbackQuery(update, cExecBot);
            return;
        }
    }

    private void handleSuperGroupstartsWithglpdCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "glpdtitleeditshtg" -> {
                    String pdchatid = split[1];
                    Cchannelht byChatid = cchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
                        return;
                    }
                    String oldTitle = byChatid.getTitle();
                    Message message = (Message) callbackQuery.getMessage();
                    String text = message.getText();
                    List<MessageEntity> entities = message.getEntities();
                    String[] split1 = text.split("\n");
                    String newTitle = split1[3].replace("新标题:", "").trim();
                    byChatid.setTitle(newTitle);
                    cchannelhtService.updateById(byChatid);
                    Msg.answerCallbackQuery(update, "修改成功", cExecBot);
                    //编辑旧消息
                    //按钮
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(CStr.str111, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, cExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(cExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), true);
                }
                case "glpdtitleeditshjj" -> {
                    String pdchatid = split[1];
                    Cchannelht byChatid = cchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
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
                    list1.add(new MyButton(CStr.str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, cExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(cExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), false);

                }
                case "glpdtitleeditshjh" -> {
                    String pdchatid = split[1];
                    Cchannelht byChatid = cchannelhtService.getByChatid(pdchatid);
                    if (byChatid == null) {
                        Msg.answerCallbackQuery(update, "频道不存在", cExecBot);
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
                    list1.add(new MyButton(CStr.str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(text, cExecBot, null, chatId, messageId, inlineKeyboardMarkup, entities);
                    //通知用户
                    sendTztitleResult(cExecBot, byChatid, newTitle, oldTitle, Long.valueOf(byChatid.getSubmitterid()), false);
                    Msg.sendMsgForceReplyKeyboard(byChatid.getSubmitterid() + "\n请输入拒绝标题修改原因", cExecBot, chatId);
                }
            }
        } else {//不带参数
        }
    }

    private static void sendTztitleResult(CExecBot cExecBot, Cchannelht byChatid, String newTitle, String oldTitle, Long chatId, boolean isSuccess) {
        String msgText2 = "\uD83D\uDCE3系统通知\uD83D\uDCE3";
        msgText2 += "\n频道标题修改通知";
        msgText2 += "\n频道ID: " + byChatid.getChatid();
        msgText2 += "\n频道: " + TeUrlUtil.getUrlExpression(byChatid.getUrl(), byChatid.getTitle());
        msgText2 += "\n原标题: " + oldTitle;
        msgText2 += "\n新标题: " + newTitle;
        msgText2 += "\n申请结果:" + (isSuccess ? "通过" : "拒绝");
        Msg.sendMsgHTML(msgText2, cExecBot, chatId);
    }

    private void handleSuperGroupstartsWithshCallbackQuery(Update update, CExecBot cExecBot) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        if (data.contains("#")) {//带参数
            String[] split = data.split("#");
            switch (split[0]) {
                case "shtg" -> {
                    String id = split[1];
                    Cchannelht byId = cchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    if (byId.getAudit().equals("1")) {
                        Msg.answerCallbackQuery(update, "该频道已经审核通过", cExecBot);
                        return;
                    }
                    Ccd ccd = ccdService.getById(byId.getCdid());
                    if (ccd == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    long l = cchannelhtService.countByCdidYTG(ccd.getId());
                    int i = Integer.parseInt(cmydataService.getValueByMyKey("cdrl"));
                    if (l >= i) {
                        Msg.answerCallbackQuery(update, "车队成员已满", cExecBot);
                        return;
                    }
                    Cpermissions oneByChatId = cpermissionsService.getOneByChatId(Long.valueOf(byId.getChatid()));
                    byId.setAudit("1");
                    byId.setInvitelink(oneByChatId.getInviteurl());
                    byId.setReviewersid(String.valueOf(update.getCallbackQuery().getFrom().getId()));
                    byId.setShtime(TeTimeUtil.getNowTime());
                    cchannelhtService.updateById(byId);
                    Msg.answerCallbackQuery(update, "审核通过", cExecBot);
                    //todo 发送到交流群
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n车队: " + ccd.getTitle();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(ccd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(ccd.getMiniread());
                    msgText += "\n人数: " + byId.getCount();
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(CStr.str111, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(cExecBot, ccd, l, i, byId);
                    //新建消息关联
                    cchannelmsgService.deleteBychatId(byId.getChatid());
                    cfansrecordsinService.deleteBychatId(byId.getChatid());
                    Cchannelmsg cchannelmsg = new Cchannelmsg();
                    cchannelmsg.setChatid(byId.getChatid());
                    cchannelmsgService.save(cchannelmsg);
                }
                case "shjj" -> {
                    String id = split[1];
                    Cchannelht byId = cchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    Ccd ccd = ccdService.getById(byId.getCdid());
                    cchannelhtService.removeById(byId.getId());
                    Msg.answerCallbackQuery(update, "拒绝成功", cExecBot);
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n车队: " + ccd.getTitle();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(ccd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(ccd.getMiniread());
                    msgText += "\n人数: " + byId.getCount();
                    long l = cchannelhtService.countByCdid(ccd.getId());
                    int i = Integer.parseInt(cmydataService.getValueByMyKey("cdrl"));
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(CStr.str121, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(cExecBot, ccd, l, i, byId);//通知提交者状态
                    cchannelmsgService.deleteBychatId(byId.getChatid());
                    cfansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), cExecBot);
                }
                case "shjh" -> {
                    String id = split[1];
                    Cchannelht byId = cchannelhtService.getById(Long.valueOf(id));
                    if (byId == null) {
                        Msg.answerCallbackQuery(update, "没有数据", cExecBot);
                        return;
                    }
                    Ccd ccd = ccdService.getById(byId.getCdid());
                    cchannelhtService.removeById(byId.getId());
                    Msg.answerCallbackQuery(update, "拒绝成功", cExecBot);
                    //编辑旧消息
                    String msgText = "标题: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
                    msgText += "\n频道ID: " + byId.getChatid();
                    msgText += "\n提交者ID: " + byId.getSubmitterid();
                    msgText += "\n最低订阅量: " + TeNumberUtil.formatNumber(ccd.getMinisubscription());
                    msgText += "\n最低阅读: " + TeNumberUtil.formatNumber(ccd.getMiniread());
                    msgText += "\n车队: " + ccd.getTitle();
                    msgText += "\n人数: " + byId.getCount();
                    long l = cchannelhtService.countByCdid(ccd.getId());
                    int i = Integer.parseInt(cmydataService.getValueByMyKey("cdrl"));
                    List<List<MyButton>> lists = new ArrayList<>();
                    List<MyButton> list1 = new ArrayList<>();
                    list1.add(new MyButton(CStr.str131, "callback", "null"));
                    lists.add(list1);
                    InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
                    Msg.editMsgAndKeyboard(msgText, cExecBot, "html", chatId, messageId, inlineKeyboardMarkup, null);
                    sendTz(cExecBot, ccd, l, i, byId);//通知提交者状态
                    Msg.sendMsgForceReplyKeyboard(byId.getSubmitterid() + "\n" + byId.getUrl() + "\n请回复审核不通过原因:", cExecBot, chatId);
                    cchannelmsgService.deleteBychatId(byId.getChatid());
                    cfansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), cExecBot);
                }
            }
        } else {//不带参数
            switch (data) {
                case "xxx" -> {
                }
            }
        }
    }

    private static void sendTz(CExecBot cExecBot, Ccd ccd, long l, int i, Cchannelht byId) {
        String msgText2 = "\uD83D\uDCE3系统通知\uD83D\uDCE3";
        msgText2 += "\n申请车队名:" + ccd.getTitle();
        msgText2 += "\n车队类型:频道";
        msgText2 += "\n车队介绍:" + ccd.getCddesc();
        msgText2 += "\n当前/最大(成员):" + l + "/" + i;
        msgText2 += "\n最低订阅: " + TeNumberUtil.formatNumber(ccd.getMinisubscription());
        msgText2 += "\n最低阅读: " + TeNumberUtil.formatNumber(ccd.getMiniread());
        msgText2 += "\n频道ID: " + byId.getChatid();
        msgText2 += "\n申请频道: " + TeUrlUtil.getUrlExpression(byId.getUrl(), byId.getTitle());
        msgText2 += "\n订阅人数: " + byId.getCount();
        msgText2 += "\n申请状态: " + (byId.getAudit().equals("1") ? "通过" : "未通过");
        Msg.sendMsgHTML(msgText2, cExecBot, Long.valueOf(byId.getSubmitterid()));
    }

    public void handleChannelMsg(Update update, CExecBot cExecBot) {
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
        Cpermissions oneByChatId = cpermissionsService.getOneByChatId(id);
        if (oneByChatId != null) {
            String oldUrl = oneByChatId.getUrl();
            if (StrUtil.isNotBlank(chat.getUserName())) {
                String newUrl = "https://t.me/" + chat.getUserName();
                oneByChatId.setUrl(newUrl);
                String oldTitle = oneByChatId.getTitle();
                if (!oldUrl.equals(newUrl) || !oldTitle.equals(chat.getTitle())) {
                    oneByChatId.setTitle(chat.getTitle());
                    cpermissionsService.updateById(oneByChatId);
                    Cchannelht cchannelht = cchannelhtService.getByChatid(String.valueOf(id));
                    if (cchannelht != null) {
                        if (StrUtil.isNotBlank(chat.getUserName())) {
                            cchannelht.setUrl(oneByChatId.getUrl());
                        }
                        cchannelhtService.updateById(cchannelht);
                    }
                }
            }

        }
    }

    public void handleSuperGroupMsg(Update update, CExecBot cExecBot) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String shqid = cmydataService.getValueByMyKey("shqid");
        if (shqid.equals(chatId.toString()) && update.getMessage().getReplyToMessage() != null) {
            Message replyToMessage = update.getMessage().getReplyToMessage();
            if (replyToMessage.getText().contains("请回复审核不通过原因:")) {
                String cahtId = replyToMessage.getText().split("\n")[0];
                String text = update.getMessage().getText();//审核不通过原因
                Msg.sendMsg("您好，您的互推频道申请已被管理员拒绝，原因如下：\n" + text, cExecBot, Long.parseLong(cahtId));
                return;
            }
            if (replyToMessage.getText().contains("请输入拒绝标题修改原因")) {
                String cahtId = replyToMessage.getText().split("\n")[0];
                String text = update.getMessage().getText();//拒绝标题修改原因
                Msg.sendMsg("您好，您的频道标题修改申请已被管理员拒绝，原因如下：\n" + text, cExecBot, Long.parseLong(cahtId));
                return;
            }
        }
    }

    public void handleGroupMsg(Update update, CExecBot cExecBot) {

    }

    /**
     * 处理私聊用户消息
     *
     * @param update
     * @param cExecBot
     */
    public void handleUserMsg(Update update, CExecBot cExecBot) {
        saveUserFwInfo(update, cExecBot);//保存用户访问信息
        Message replyToMessage = update.getMessage().getReplyToMessage();
        //判断是否是命令
        if (CmdUtil.isPrivateCmd(update) && replyToMessage == null) {
            handleCmd(update, cExecBot);
            return;
        } else if (replyToMessage != null) {
            handleReplyToMsg(update, cExecBot);
            return;
        } else {//普通消息
            handleGeneralMsg(update, cExecBot);
            return;
        }

    }

    /**
     * 处理普通消息
     *
     * @param update
     * @param cExecBot
     */
    //todo 处理所有普通消息
    private void handleGeneralMsg(Update update, CExecBot cExecBot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        if (text.startsWith("车队添加")) {
            Cteadmin cteadmin = cteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (cteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 5) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("车队标题:")
                    || !split[2].startsWith("车队描述:")
                    || !split[3].startsWith("最低订阅量:")
                    || !split[4].startsWith("最低阅读:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            String title = split[1].trim().replaceAll("车队标题:", "");
            String cddesc = split[2].trim().replaceAll("车队描述:", "");
            String minisubscription = split[3].trim().replaceAll("最低订阅量:", "");
            String miniread = split[4].trim().replaceAll("最低阅读:", "");
            Ccd ccd = new Ccd();
            ccd.setTitle(title);
            ccd.setCddesc(cddesc);
            ccd.setMinisubscription(Integer.parseInt(minisubscription));
            ccd.setMiniread(Integer.parseInt(miniread));
            ccd.setType("channel");
            ccdService.save(ccd);
            Msg.sendMsg("车队分组添加成功", cExecBot, chatId);
            return;
        }
        if (text.startsWith("车队编辑")) {
            Cteadmin cteadmin = cteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (cteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 6) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("车队id:")
                    || !split[2].startsWith("车队标题:")
                    || !split[3].startsWith("车队描述:")
                    || !split[4].startsWith("最低订阅量:")
                    || !split[5].startsWith("最低阅读:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            Long cdid = Long.parseLong(split[1].trim().replaceAll("车队id:", ""));
            String title = split[2].trim().replaceAll("车队标题:", "");
            String cddesc = split[3].trim().replaceAll("车队描述:", "");
            String minisubscription = split[4].trim().replaceAll("最低订阅量:", "");
            String miniread = split[5].trim().replaceAll("最低阅读:", "");
            Ccd ccd = ccdService.getById(cdid);
            ccd.setTitle(title);
            ccd.setCddesc(cddesc);
            ccd.setMinisubscription(Integer.parseInt(minisubscription));
            ccd.setMiniread(Integer.parseInt(miniread));
            ccdService.updateById(ccd);
            Msg.sendMsg("车队分组编辑成功", cExecBot, chatId);
            return;
        }
        if (text.startsWith("广告添加")) {
            Cteadmin cteadmin = cteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (cteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 4) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("标题:") || !split[2].startsWith("url:") || !split[3].startsWith("位置:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            String title = split[1].trim().replaceAll("标题:", "");
            String url = split[2].trim().replaceAll("url:", "");
            String position = split[3].trim().replaceAll("位置:", "");
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                Msg.sendMsg("url格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            if (position.equals("1") || position.equals("2")) {
                Cad cad = new Cad();
                cad.setTitle(title);
                cad.setContent(url);
                cad.setFlag(position);
                cad.setStatus("1");
                cadService.save(cad);
                Msg.sendMsg("广告添加成功", cExecBot, chatId);
            } else {
                Msg.sendMsg("位置只能为1或2", cExecBot, chatId);
                return;
            }
            return;
        }
        if (text.startsWith("频道发送频率修改")) {
            Cteadmin cteadmin = cteadminService.getOneByChatId(update.getMessage().getFrom().getId());
            if (cteadmin == null) {
                return;
            }
            String[] split = text.split("\n");
            if (split.length != 2) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            if (!split[1].startsWith("频率:")) {
                Msg.sendMsg("录入信息格式不正确，请重新录入", cExecBot, chatId);
                return;
            }
            String frequency = split[1].trim().replaceAll("频率:", "");
            cmydataService.updateValueByKey("resetmessagetime", frequency);
            Msg.sendMsg("频道发送频率修改成功", cExecBot, chatId);
            return;
        }
        if (text.startsWith("推广按钮编辑")) {
            text = text.trim().replaceAll("推广按钮编辑\n", "");
            cmydataService.updateValueByKey("mybutton", text);
            Msg.sendMsg("推广按钮编辑成功", cExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑help文案")) {
            text = text.trim().replaceAll("编辑help文案\n", "");
            Ccopywriter oneByTextkey = ccopywriterService.getOneByTextkey("help");
            oneByTextkey.setTextcontent(text);
            ccopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑help文案成功", cExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑start02文案")) {
            text = text.trim().replaceAll("编辑start02文案\n", "");
            Ccopywriter oneByTextkey = ccopywriterService.getOneByTextkey("start02");
            oneByTextkey.setTextcontent(text);
            ccopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑start02文案成功", cExecBot, chatId);
            return;
        }
        if (text.startsWith("编辑start01文案")) {
            text = text.trim().replaceAll("编辑start01文案\n", "");
            Ccopywriter oneByTextkey = ccopywriterService.getOneByTextkey("start01");
            oneByTextkey.setTextcontent(text);
            ccopywriterService.updateById(oneByTextkey);
            Msg.sendMsg("编辑start01文案成功", cExecBot, chatId);
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
     * @param cExecBot
     */
    private void handleReplyToMsg(Update update, CExecBot cExecBot) {
        String shqid = cmydataService.getValueByMyKey("shqid");
        Long chatId = update.getMessage().getChat().getId();//回复者
        String text = update.getMessage().getReplyToMessage().getText();//原始消息内容
        String text1 = update.getMessage().getText();//回复内容
        if (text.contains("请输入新的频道标题")) {
            String pdchatId = text.split("\n")[0].trim();
            Cchannelht byChatid = cchannelhtService.getByChatid(pdchatId);
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
            list1.add(new MyButton(CStr.str11, "callback", "glpdtitleeditshtg#" + pdchatId));
            list1.add(new MyButton(CStr.str12, "callback", "glpdtitleeditshjj#" + pdchatId));
            list1.add(new MyButton(CStr.str13, "callback", "glpdtitleeditshjh#" + pdchatId));
            lists.add(list1);
            InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
            Msg.sendMsgAndKeyboard(msgText, cExecBot, "html", Long.valueOf(shqid), inlineKeyboardMarkup, null);
        }
    }

    /**
     * 处理命令
     *
     * @param update
     * @param cExecBot
     */
    //todo 处理所有cmd命令
    private void handleCmd(Update update, CExecBot cExecBot) {
        Long chatId = update.getMessage().getChatId();
        if (CmdUtil.isCmdWithArgs(update)) {//处理带参数的命令
            String cmdName = CmdUtil.getCmdNameWithArgs(update);
            String cmdArgs = CmdUtil.getCmdArgs(update).trim();
            switch (cmdName) {
                case "glfzdelete" -> {
                    //权限判断
                    Cteadmin cteadmin = cteadminService.getOneByChatId(update.getMessage().getFrom().getId());
                    if (cteadmin == null) {
                        return;
                    }
                    long cdid = Long.parseLong(cmdArgs);
                    Ccd ccd = ccdService.getById(cdid);
                    if (ccd == null) {
                        Msg.sendMsg("车队不存在", cExecBot, chatId);
                        return;
                    }
                    boolean b = ccdService.removeById(cdid);
                    if (b) {
                        //移除所有机器人
                        List<Cchannelht> list = cchannelhtService.getListByCdId(cdid);
                        HashSet<String> chatids = new HashSet<>();
                        for (Cchannelht cchannelht : list) {
                            chatids.add(cchannelht.getChatid());
                            Msg.leaveChat(Long.valueOf(cchannelht.getChatid()), cExecBot);
                        }
                        if (!chatids.isEmpty()) {
                            cchannelhtService.removeByChatids(chatids);
                            cchannelmsgService.removeByChatids(chatids);
                            cfansrecordsinService.removeByChatids(chatids);
                        }
                        Msg.sendMsg("车队删除成功", cExecBot, chatId);
                    } else {
                        Msg.sendMsg("车队删除失败", cExecBot, chatId);
                    }
                    return;
                }
                case "glpddelete" -> {
                    Cteadmin cteadmin = cteadminService.getOneByChatId(update.getMessage().getFrom().getId());
                    if (cteadmin == null) {
                        return;
                    }
                    long pdid = Long.parseLong(cmdArgs);
                    Cchannelht byId = cchannelhtService.getById(pdid);
                    if (byId == null) {
                        Msg.sendMsg("频道不存在", cExecBot, chatId);
                        return;
                    }
                    cchannelhtService.removeById(pdid);
                    Msg.leaveChat(Long.valueOf(byId.getChatid()), cExecBot);
                    cchannelmsgService.deleteBychatId(byId.getChatid());
                    cfansrecordsinService.deleteBychatId(byId.getChatid());
                    Msg.sendMsg("频道删除成功", cExecBot, chatId);
                }
            }
        } else {//处理不带参数的命令
            String cmdName = CmdUtil.getCmdNameNoWithArgs(update);
            switch (cmdName) {
                case "gl" -> {
                    //判断权限
                    Cteadmin cteadmin = cteadminService.getOneByChatId(chatId);
                    if (cteadmin == null) {
                        return;
                    }
                    handleglCmd(update, cExecBot);
                    return;
                }
                case "start" -> {
                    dealStartCmd(update, cExecBot);
                    return;
                }
                case "help" -> {
                    Ccopywriter ccopywriter = ccopywriterService.getOneByTextkey("help");
                    if (ccopywriter == null) {
                        return;
                    }
                    Msg.sendMsgAndEntities(ccopywriter.getTextcontent(), cExecBot, chatId, StrUtil.isNotBlank(ccopywriter.getTextentities()) ? JSONUtil.toList(ccopywriter.getTextentities(), MessageEntity.class) : null);
                    return;
                }
            }
        }
    }

    /**
     * 处理start命令
     *
     * @param update
     * @param cExecBot
     */
    private void dealStartCmd(Update update, CExecBot cExecBot) {
        Ccopywriter ccopywriterstart01 = ccopywriterService.getOneByTextkey("start01");
        if (ccopywriterstart01 != null) {
            Msg.sendMsgAndEntities(ccopywriterstart01.getTextcontent(), cExecBot, update.getMessage().getChatId(), StrUtil.isNotBlank(ccopywriterstart01.getTextentities()) ? JSONUtil.toList(ccopywriterstart01.getTextentities(), MessageEntity.class) : null);
        }
        sendStartMain(update.getMessage().getChatId(), cExecBot, "sendnew", null);
    }

    private void sendStartMain(Long chatid, CExecBot cExecBot, String sendModel, Integer messageId) {
        Ccopywriter ccopywriterstart02 = ccopywriterService.getOneByTextkey("start02");
        if (ccopywriterstart02 == null) {
            return;
        }
        String botName;
        try {
            botName = cExecBot.getMe().getUserName();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(CStr.str01, "url", "https://t.me/" + botName + "?startchannel"));
        lists.add(list1);
//        List<MyButton> list2 = new ArrayList<>();
//        list2.add(new MyButton(str02, "callback", "cdstartvive"));
//        lists.add(list2);
        List<MyButton> list3 = new ArrayList<>();
        list3.add(new MyButton(CStr.str03, "callback", "cdstartsq"));
        lists.add(list3);
        List<Cchannelht> listBySubmitterId = cchannelhtService.findListBySubmitterId(chatid);
        if (listBySubmitterId != null && !listBySubmitterId.isEmpty()) {
            List<MyButton> list5 = new ArrayList<>();
            list5.add(new MyButton(CStr.str29, "callback", "cdstartglmypd"));
            lists.add(list5);
        }
        List<MyButton> list4 = new ArrayList<>();
        String jlgroupurl = cmydataService.getValueByMyKey("jlgroupurl");
        String kf = cmydataService.getValueByMyKey("kf");
        list4.add(new MyButton(CStr.str04, "url", jlgroupurl));
        list4.add(new MyButton(CStr.str05, "url", kf));
        lists.add(list4);
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        if (sendModel.equals("sendnew")) {
            Msg.sendMsgAndKeyboard(ccopywriterstart02.getTextcontent(),
                    cExecBot,
                    chatid,
                    inlineKeyboardMarkup,
                    StrUtil.isNotBlank(ccopywriterstart02.getTextentities()) ? JSONUtil.toList(ccopywriterstart02.getTextentities(), MessageEntity.class) : null);
        } else {
            Msg.editMsgAndKeyboard(ccopywriterstart02.getTextcontent(),
                    cExecBot,
                    chatid,
                    messageId,
                    inlineKeyboardMarkup,
                    StrUtil.isNotBlank(ccopywriterstart02.getTextentities()) ? JSONUtil.toList(ccopywriterstart02.getTextentities(), MessageEntity.class) : null);
        }
    }

    /**
     * 处理gl命令
     *
     * @param update
     * @param cExecBot
     */
    //todo 处理gl命令
    private void handleglCmd(Update update, CExecBot cExecBot) {
        Long chatId = update.getMessage().getChatId();
        myGlMenu(chatId, cExecBot, "sendnew", null);
    }

    private void myGlMenu(Long chatId, CExecBot cExecBot, String sendModel, Integer messageId) {
        List<List<MyButton>> lists = new ArrayList<>();
        List<MyButton> list1 = new ArrayList<>();
        list1.add(new MyButton(CStr.str0701, "callback", "glfz"));
        lists.add(list1);
        List<MyButton> list2 = new ArrayList<>();
        list2.add(new MyButton(CStr.str0702, "callback", "glpd"));
        lists.add(list2);
        List<MyButton> list3 = new ArrayList<>();
        list3.add(new MyButton(CStr.str0703, "callback", "glad"));
        lists.add(list3);
        List<MyButton> list4 = new ArrayList<>();
        list4.add(new MyButton(CStr.str0704, "callback", "glwa"));
        lists.add(list4);
        List<MyButton> list5 = new ArrayList<>();
        list5.add(new MyButton(CStr.str0705, "callback", "glbb"));
        lists.add(list5);
        List<MyButton> list6 = new ArrayList<>();
        list6.add(new MyButton(CStr.str0706, "callback", "gltgbutton"));
        lists.add(list6);
        List<MyButton> list7 = new ArrayList<>();
        list7.add(new MyButton(CStr.str0707, "callback", "glcheckpd"));
        lists.add(list7);
        List<MyButton> list8 = new ArrayList<>();
        list8.add(new MyButton(CStr.str0708, "callback", "glrefreshcount"));
        lists.add(list8);
        List<MyButton> list9 = new ArrayList<>();
        list9.add(new MyButton(CStr.str0709, "callback", "glsendfrequency"));
        lists.add(list9);
        List<MyButton> list10 = new ArrayList<>();
        list10.add(new MyButton(CStr.str0710, "callback", "gluserused"));
        lists.add(list10);
        String cdstatus = cmydataService.getValueByMyKey("cdstatus");
        List<MyButton> list11 = new ArrayList<>();
        if (cdstatus.equals("0")) {
            list11.add(new MyButton(CStr.str0711, "callback", "glhtstart"));
        } else {
            list11.add(new MyButton(CStr.str0712, "callback", "glhtstop"));
        }
        lists.add(list11);
        String msgText = CStr.str06;
        String resetmessagetime = cmydataService.getValueByMyKey("resetmessagetime");
        msgText += "\n发送频率(单位分钟):" + resetmessagetime;
        String cdrl = cmydataService.getValueByMyKey("cdrl");
        msgText += "\n车队容量:" + cdrl;
        String cdname = cmydataService.getValueByMyKey("cdname");
        msgText += "\n车队名称:" + cdname;
        msgText += ("\n车队状态:" + (cdstatus.equals("1") ? "已开启" : "已关闭"));
        InlineKeyboardMarkup inlineKeyboardMarkup = Msg.getInlineKeyboardMarkup(lists);
        if (sendModel.equals("sendnew") && messageId == null) {
            Msg.sendMsgAndKeyboard(msgText,
                    cExecBot,
                    chatId,
                    inlineKeyboardMarkup,
                    null);
        } else {
            Msg.editMsgAndKeyboard(msgText,
                    cExecBot,
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
     * @param cExecBot
     */
    private void saveUserFwInfo(Update update, CExecBot cExecBot) {
        try {
            Long botid = cExecBot.getMe().getId();
            Long userid = update.getMessage().getFrom().getId();
            String nowTime = TeTimeUtil.getNowTime();
            User user = update.getMessage().getFrom();
            Cvisitors cvisitors = cvisitorsService.findByUseridAndFwtimeAndBotid(userid, nowTime, botid);
            if (cvisitors != null) {
                return;
            } else {
                cvisitors = new Cvisitors();
                cvisitors.setBotid(String.valueOf(botid));
                cvisitors.setUserid(String.valueOf(userid));
                cvisitors.setFwtime(nowTime);
                if (StrUtil.isNotBlank(user.getUserName())) {
                    cvisitors.setUsername(user.getUserName());
                }
                if (StrUtil.isNotBlank(user.getFirstName())) {
                    cvisitors.setFirstname(user.getFirstName());
                }
                if (StrUtil.isNotBlank(user.getLastName())) {
                    cvisitors.setLastname(user.getLastName());
                }
                cvisitorsService.save(cvisitors);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleJoinChannel(Update update, CExecBot cExecBot) {
        ChatInviteLink inviteLink = update.getChatMember().getInviteLink();
        if (inviteLink == null) {
            return;
        }
        String inviteLink1 = inviteLink.getInviteLink();
        Cchannelht byInviteLink = cchannelhtService.getByInviteLink(inviteLink1);
        if (byInviteLink == null) {
            return;
        }
        cfansrecordsinService.fansAdd(byInviteLink.getChatid());
    }

    public void handleJoinGroup(Update update, CExecBot cExecBot) {
    }

    public void handleJoinSuperGroup(Update update, CExecBot cExecBot) {
    }

    public void handleJoinUser(Update update, CExecBot cExecBot) {
    }

    public void handleChatJoinRequest(Update update, CExecBot cExecBot) {
    }

    public void handlePermissionsFromChannel(Update update, CExecBot cExecBot) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        Chat chat = myChatMember.getChat();
        Long chatId = chat.getId();
        ChatMember newChatMember = myChatMember.getNewChatMember();
        ChatMember oldChatMember = myChatMember.getOldChatMember();
        User user = newChatMember.getUser();

        try {//检测成员权限变化是否为机器人自己
            if (!user.getId().toString().equals(cExecBot.getMe().getId().toString())) {
                return;
            }
        } catch (TelegramApiException e) {
            return;
        }
        if (StrUtil.isBlank(chat.getUserName())) {//不处理私聊频道
            return;
        }
        //权限表更新
        Cpermissions cpermissions = cpermissionsService.getOneByChatId(chatId);
        if (cpermissions == null) {
            cpermissions = new Cpermissions();
            cpermissions.setChatid(chatId.toString());
            String title = chat.getTitle();
            boolean b = TeEmojiUtil.containsEmoji(title);
            if (b) {
                title = TeEmojiUtil.filterEmoji(title);
            }
            title = TeEmojiUtil.replaceEmoji(title, " ");

            cpermissions.setTitle(title);
            cpermissions.setType(chat.getType());
            cpermissions.setGroupstatus(newChatMember.getStatus());

            cpermissions.setCreatetime(TeTimeUtil.getNowTimeDetail());
            cpermissions.setUrl("https://t.me/" + chat.getUserName());
            Permissions permissions = JSONUtil.toBean(JSONUtil.toJsonStr(newChatMember), Permissions.class);
            if (permissions.isCanPostMessages()
                    && permissions.isCanEditMessages()
                    && permissions.isCanDeleteMessages()
                    && permissions.isCanInviteUsers()
                    && permissions.isCanRestrictMembers()
                    && permissions.isCanManageChat()) {
                cpermissions.setMsgandinvqx("1");
            } else {
                cpermissions.setMsgandinvqx("0");
            }
            if (!myChatMember.getFrom().getIsBot()) {
                cpermissions.setInviteuserid(myChatMember.getFrom().getId().toString());
            }
            int channelCount = spidersUtils.getChannelCount(cpermissions.getUrl());
            if (channelCount > 0) {
                cpermissions.setCount(channelCount);
            } else {
                int chatMembersCount = Msg.getChatMembersCount(Long.valueOf(cpermissions.getChatid()), cExecBot);
                if (chatMembersCount > 0) {
                    cpermissions.setCount(chatMembersCount);
                }
            }
            cpermissionsService.save(cpermissions);
        } else {
            String title = chat.getTitle();
            boolean b = TeEmojiUtil.containsEmoji(title);
            if (b) {
                title = TeEmojiUtil.filterEmoji(title);
            }
            title = TeEmojiUtil.replaceEmoji(title, " ");
            cpermissions.setTitle(title);
            cpermissions.setType(chat.getType());
            cpermissions.setGroupstatus(newChatMember.getStatus());
            cpermissions.setUrl("https://t.me/" + chat.getUserName());
            Permissions permissions = JSONUtil.toBean(JSONUtil.toJsonStr(newChatMember), Permissions.class);
            if (permissions.isCanPostMessages()
                    && permissions.isCanEditMessages()
                    && permissions.isCanDeleteMessages()
                    && permissions.isCanInviteUsers()
                    && permissions.isCanRestrictMembers()
                    && permissions.isCanManageChat()) {
                cpermissions.setMsgandinvqx("1");
            } else {
                cpermissions.setMsgandinvqx("0");
            }
            if (!myChatMember.getFrom().getIsBot()) {
                cpermissions.setInviteuserid(myChatMember.getFrom().getId().toString());
            }
            int channelCount = spidersUtils.getChannelCount(cpermissions.getUrl());
            if (channelCount > 0) {
                cpermissions.setCount(channelCount);
            } else {
                int chatMembersCount = Msg.getChatMembersCount(Long.valueOf(cpermissions.getChatid()), cExecBot);
                if (chatMembersCount > 0) {
                    cpermissions.setCount(chatMembersCount);
                }
            }
            cpermissionsService.updateById(cpermissions);
        }
        if (newChatMember.getStatus().equals("administrator") && cpermissions.getMsgandinvqx().equals("1")) {
            delayedGenerationOfInvitationLinks(cpermissions.getId(), cExecBot);
        }
        String handleName = "";
        if (!myChatMember.getFrom().getIsBot() && StrUtil.isNotBlank(myChatMember.getFrom().getUserName())) {
            handleName = myChatMember.getFrom().getUserName();
        }
        //机器人离开频道通知
        String shqid = cmydataService.getValueByMyKey("shqid");
        if (newChatMember.getStatus().equals("left") || newChatMember.getStatus().equals("kicked")) {
            //如果在互推组中将从互推组中删除并删除相关进粉信息
            Msg.sendMsgHTML("检测到机器人已离开频道，已将"
                    + TeUrlUtil.getUrlExpression(cpermissions.getUrl(), cpermissions.getTitle())
                    + "从互推组中删除。", cExecBot, Long.parseLong(cpermissions.getInviteuserid()));

            Msg.sendMsgHTML("检测到机器人已离开频道，已将"
                            + TeUrlUtil.getUrlExpression(cpermissions.getUrl(), cpermissions.getTitle())
                            + "从互推组中删除。操作人 @"
                            + handleName,
                    cExecBot, Long.parseLong(shqid));
            HashSet<String> strings = new HashSet<>();
            strings.add(chatId.toString());
            cchannelhtService.removeByChatids(strings);
            cchannelmsgService.removeByChatids(strings);
            cfansrecordsinService.removeByChatids(strings);
        }
        //机器人进入频道通知
        if ((newChatMember.getStatus().equals("member") || newChatMember.getStatus().equals("administrator")) && (oldChatMember.getStatus().equals("left") || oldChatMember.getStatus().equals("kicked"))) {
            Msg.sendMsgHTML("检测到机器人进入频道"
                    + TeUrlUtil.getUrlExpression(cpermissions.getUrl()
                    , cpermissions.getTitle()), cExecBot, Long.parseLong(cpermissions.getInviteuserid()));
            Msg.sendMsgHTML("检测到机器人进入频道"
                    + TeUrlUtil.getUrlExpression(cpermissions.getUrl(), cpermissions.getTitle()) + "操作人 @" +
                    handleName, cExecBot, Long.parseLong(shqid));
        }
        //权限不足通知
        if (newChatMember.getStatus().equals("member") || newChatMember.getStatus().equals("administrator")) {
            if (cpermissions.getMsgandinvqx().equals("0")) {
                Msg.sendMsgHTML("检测到机器人权限不足，请添加管理员并管理消息和添加成员权限。" + TeUrlUtil.getUrlExpression(cpermissions.getUrl(), cpermissions.getTitle()), cExecBot, Long.parseLong(cpermissions.getInviteuserid()));
            } else {
                Msg.sendMsgHTML("检测到机器人权限正常" + TeUrlUtil.getUrlExpression(cpermissions.getUrl(), cpermissions.getTitle()), cExecBot, Long.parseLong(cpermissions.getInviteuserid()));
            }
        }

    }

    /**
     * 延迟生成邀请链接
     *
     * @param id
     * @param cExecBot
     */
    private void delayedGenerationOfInvitationLinks(Integer id, CExecBot cExecBot) {
        scheduler.schedule(() -> {
            Cpermissions permissions = cpermissionsService.getById(id);
            if (permissions != null) {
                String inviteLink = Msg.getInviteLink(Long.parseLong(permissions.getChatid()), cExecBot);
                if (StrUtil.isNotBlank(inviteLink)) {
                    permissions.setInviteurl(inviteLink);
                    cpermissionsService.updateById(permissions);
                }
            }
        }, 15, TimeUnit.SECONDS);
    }

    public void handlePermissionsFromGroup(Update update, CExecBot cExecBot) {
    }

    public void handlePermissionsFromSuperGroup(Update update, CExecBot cExecBot) {
    }

    public void handlePermissionsFromUserChat(Update update, CExecBot cExecBot) {
    }
}
