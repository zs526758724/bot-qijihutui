package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfmydata;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface FzzfmydataService extends IService<Fzzfmydata> {
    /**
     * 根据mykey获取value
     *
     * @param myKey
     * @return
     */
    String getValueByMyKey(String myKey);

    /**
     * @param cdstatus
     */
    Fzzfmydata getOneByMyKey(String cdstatus);

    void updateValueByKey(String key, String value);

    List<Fzzfmydata> findListByKey(String pdremove);
}
