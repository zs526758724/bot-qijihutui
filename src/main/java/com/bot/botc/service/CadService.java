package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cad;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface CadService extends IService<Cad> {

    List<Cad> findByAdtype(String number);

    List<Cad> findAllByAdtype(String number);
}
