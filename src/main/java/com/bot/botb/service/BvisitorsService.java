package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bvisitors;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface BvisitorsService extends IService<Bvisitors> {


    /**
     * @param userid
     * @param nowTime
     * @param botid
     * @return
     */
    Bvisitors findByUseridAndFwtimeAndBotid(Long userid, String nowTime, Long botid);

    List<Bvisitors> findListByBotId(Long id);
}
