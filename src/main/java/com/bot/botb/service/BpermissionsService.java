package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bpermissions;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface BpermissionsService extends IService<Bpermissions> {

    /**
     * 根据群组id获取群组信息
     *
     * @param chatId
     * @return
     */
    Bpermissions getOneByChatId(Long chatId);

    List<Bpermissions> findListByInviteuserid(Long chatId);

    void deleteBychatId(String chatId);
}
