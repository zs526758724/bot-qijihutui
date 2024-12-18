package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bteadmin;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface BteadminService extends IService<Bteadmin> {

    Bteadmin getOneByChatId(Long chatId);
}
