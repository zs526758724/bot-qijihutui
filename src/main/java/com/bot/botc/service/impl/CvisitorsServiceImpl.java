package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cvisitors;
import com.bot.botc.mapper.CvisitorsMapper;
import com.bot.botc.service.CvisitorsService;
import com.bot.common.utils.TeListUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
@Service
//@Transactional
public class CvisitorsServiceImpl extends ServiceImpl<CvisitorsMapper, Cvisitors> implements CvisitorsService {

    @Override
    public Cvisitors findByUseridAndFwtimeAndBotid(Long userid, String nowTime, Long botid) {
        LambdaQueryWrapper<Cvisitors> query = Wrappers.lambdaQuery(Cvisitors.class);
        query.eq(Cvisitors::getUserid, userid).eq(Cvisitors::getFwtime, nowTime).eq(Cvisitors::getBotid, botid);
        List<Cvisitors> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Cvisitors> findListByBotId(Long id) {
        LambdaQueryWrapper<Cvisitors> query = Wrappers.lambdaQuery(Cvisitors.class);
        query.eq(Cvisitors::getBotid, id);
        return this.list(query);
    }
}
