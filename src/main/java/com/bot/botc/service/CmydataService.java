package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cmydata;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface CmydataService extends IService<Cmydata> {
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
    Cmydata getOneByMyKey(String cdstatus);

    void updateValueByKey(String key, String value);

    List<Cmydata> findListByKey(String pdremove);
}
