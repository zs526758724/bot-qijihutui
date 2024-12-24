package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cchannelmsg;

import java.util.HashSet;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface CchannelmsgService extends IService<Cchannelmsg> {

    void removeByChatids(HashSet<String> chatids);

    void deleteBychatId(String chatid);
}
