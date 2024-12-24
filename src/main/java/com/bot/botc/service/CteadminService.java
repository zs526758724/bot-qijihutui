package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cteadmin;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface CteadminService extends IService<Cteadmin> {

    Cteadmin getOneByChatId(Long chatId);
}
