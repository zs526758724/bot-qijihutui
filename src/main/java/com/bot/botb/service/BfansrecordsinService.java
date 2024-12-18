package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bfansrecordsin;

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
public interface BfansrecordsinService extends IService<Bfansrecordsin> {

    /**
     * 批量删除
     *
     * @param chatids
     */
    void removeByChatids(HashSet<String> chatids);

    void deleteBychatId(String chatid);

    void fansAdd(String chatid);

    List<Bfansrecordsin> findListByChatId(String chatid);
}
