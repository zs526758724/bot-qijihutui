package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bmydata;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface BmydataService extends IService<Bmydata> {
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
    Bmydata getOneByMyKey(String cdstatus);

    void updateValueByKey(String key, String value);

    List<Bmydata> findListByKey(String pdremove);
}
