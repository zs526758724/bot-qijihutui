package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bchannelmsg;

import java.util.HashSet;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface BchannelmsgService extends IService<Bchannelmsg> {

    void removeByChatids(HashSet<String> chatids);

    void deleteBychatId(String chatid);
}
