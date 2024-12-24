package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cfansrecordsin;

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
public interface CfansrecordsinService extends IService<Cfansrecordsin> {

    /**
     * 批量删除
     *
     * @param chatids
     */
    void removeByChatids(HashSet<String> chatids);

    void deleteBychatId(String chatid);

    void fansAdd(String chatid);

    List<Cfansrecordsin> findListByChatId(String chatid);
}
