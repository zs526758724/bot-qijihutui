package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfchannelht;

import java.util.HashSet;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface FzzfchannelhtService extends IService<Fzzfchannelht> {

    List<Fzzfchannelht> findListByCdid(Long fzzfcdid);

    /**
     * 根据cdid获取列表
     *
     * @param cdid
     * @return
     */
    List<Fzzfchannelht> getListByCdId(long cdid);

    /**
     * @param chatids
     */
    void removeByChatids(HashSet<String> chatids);

    List<Fzzfchannelht> getListBySubmitterid(Long chatId);

    Fzzfchannelht getByChatid(String chatid);

    /**
     * 统计cdid的频道数
     *
     * @param id
     */
    long countByCdid(Integer id);

    List<Fzzfchannelht> findListByCdidAndSH1(Long aLong);

    List<Fzzfchannelht> findListByAudit(String number);

    Page<Fzzfchannelht> listOrderByCdid(int current);

    /**
     * 刷新频道数
     */
    void refreshCount();

    Fzzfchannelht getByInviteLink(String inviteLink1);

    List<Fzzfchannelht> findListBySubmitterId(Long chatid);

    Page<Fzzfchannelht> getPagesBySubmitterId(Long chatId, int current);

    long countByCdidYTG(Integer id);

    void removeByChatid(String chatId);
}
