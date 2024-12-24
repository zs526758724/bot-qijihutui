package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cpermissions;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface CpermissionsService extends IService<Cpermissions> {

    /**
     * 根据群组id获取群组信息
     *
     * @param chatId
     * @return
     */
    Cpermissions getOneByChatId(Long chatId);

    List<Cpermissions> findListByInviteuserid(Long chatId);

    void deleteBychatId(String chatId);
}
