package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfvisitors;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
public interface FzzfvisitorsService extends IService<Fzzfvisitors> {


    /**
     * @param userid
     * @param nowTime
     * @param botid
     * @return
     */
    Fzzfvisitors findByUseridAndFwtimeAndBotid(Long userid, String nowTime, Long botid);

    List<Fzzfvisitors> findListByBotId(Long id);
}
