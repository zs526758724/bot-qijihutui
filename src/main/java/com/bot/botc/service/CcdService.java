package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Ccd;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface CcdService extends IService<Ccd> {

    Page<Ccd> pageList(int current);
}
