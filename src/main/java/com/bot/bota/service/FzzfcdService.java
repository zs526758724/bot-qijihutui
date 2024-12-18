package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfcd;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface FzzfcdService extends IService<Fzzfcd> {

    Page<Fzzfcd> pageList(int current);
}
