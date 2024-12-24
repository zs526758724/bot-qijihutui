package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cchannelht;

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
public interface CchannelhtService extends IService<Cchannelht> {

    List<Cchannelht> findListByCdid(Long fzzfcdid);

    /**
     * 根据cdid获取列表
     *
     * @param cdid
     * @return
     */
    List<Cchannelht> getListByCdId(long cdid);

    /**
     * @param chatids
     */
    void removeByChatids(HashSet<String> chatids);

    List<Cchannelht> getListBySubmitterid(Long chatId);

    Cchannelht getByChatid(String chatid);

    /**
     * 统计cdid的频道数
     *
     * @param id
     */
    long countByCdid(Integer id);

    List<Cchannelht> findListByCdidAndSH1(Long aLong);

    List<Cchannelht> findListByAudit(String number);

    Page<Cchannelht> listOrderByCdid(int current);

    /**
     * 刷新频道数
     */
    void refreshCount();

    Cchannelht getByInviteLink(String inviteLink1);

    List<Cchannelht> findListBySubmitterId(Long chatid);

    Page<Cchannelht> getPagesBySubmitterId(Long chatId, int current);

    long countByCdidYTG(Integer id);

    void removeByChatid(String chatId);
}
