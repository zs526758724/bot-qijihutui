package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bcd;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface BcdService extends IService<Bcd> {

    Page<Bcd> pageList(int current);
}
