package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bchannelht;

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
public interface BchannelhtService extends IService<Bchannelht> {

    List<Bchannelht> findListByCdid(Long fzzfcdid);

    /**
     * 根据cdid获取列表
     *
     * @param cdid
     * @return
     */
    List<Bchannelht> getListByCdId(long cdid);

    /**
     * @param chatids
     */
    void removeByChatids(HashSet<String> chatids);

    List<Bchannelht> getListBySubmitterid(Long chatId);

    Bchannelht getByChatid(String chatid);

    /**
     * 统计cdid的频道数
     *
     * @param id
     */
    long countByCdid(Integer id);

    List<Bchannelht> findListByCdidAndSH1(Long aLong);

    List<Bchannelht> findListByAudit(String number);

    Page<Bchannelht> listOrderByCdid(int current);

    /**
     * 刷新频道数
     */
    void refreshCount();

    Bchannelht getByInviteLink(String inviteLink1);

    List<Bchannelht> findListBySubmitterId(Long chatid);

    Page<Bchannelht> getPagesBySubmitterId(Long chatId, int current);

    long countByCdidYTG(Integer id);

    void removeByChatid(String chatId);
}
