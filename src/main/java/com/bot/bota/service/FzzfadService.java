package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfad;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface FzzfadService extends IService<Fzzfad> {

    List<Fzzfad> findByAdtype(String number);

    List<Fzzfad> findAllByAdtype(String number);
}
