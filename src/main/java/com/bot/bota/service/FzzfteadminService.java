package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfteadmin;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface FzzfteadminService extends IService<Fzzfteadmin> {

    Fzzfteadmin getOneByChatId(Long chatId);
}
