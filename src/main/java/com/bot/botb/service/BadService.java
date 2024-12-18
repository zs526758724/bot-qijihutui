package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bad;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface BadService extends IService<Bad> {

    List<Bad> findByAdtype(String number);

    List<Bad> findAllByAdtype(String number);
}
