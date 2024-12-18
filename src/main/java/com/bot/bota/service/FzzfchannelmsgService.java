package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfchannelmsg;

import java.util.HashSet;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface FzzfchannelmsgService extends IService<Fzzfchannelmsg> {

    void removeByChatids(HashSet<String> chatids);

    void deleteBychatId(String chatid);
}
