package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfpermissions;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface FzzfpermissionsService extends IService<Fzzfpermissions> {

    /**
     * 根据群组id获取群组信息
     *
     * @param chatId
     * @return
     */
    Fzzfpermissions getOneByChatId(Long chatId);

    List<Fzzfpermissions> findListByInviteuserid(Long chatId);

    void deleteBychatId(String chatId);
}
