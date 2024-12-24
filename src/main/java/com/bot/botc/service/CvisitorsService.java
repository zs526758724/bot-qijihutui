package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Cvisitors;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface CvisitorsService extends IService<Cvisitors> {


    /**
     * @param userid
     * @param nowTime
     * @param botid
     * @return
     */
    Cvisitors findByUseridAndFwtimeAndBotid(Long userid, String nowTime, Long botid);

    List<Cvisitors> findListByBotId(Long id);
}
