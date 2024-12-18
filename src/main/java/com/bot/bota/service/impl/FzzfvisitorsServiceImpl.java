package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfvisitors;
import com.bot.bota.mapper.FzzfvisitorsMapper;
import com.bot.bota.service.FzzfvisitorsService;
import com.bot.common.utils.TeListUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class FzzfvisitorsServiceImpl extends ServiceImpl<FzzfvisitorsMapper, Fzzfvisitors> implements FzzfvisitorsService {

    @Override
    public Fzzfvisitors findByUseridAndFwtimeAndBotid(Long userid, String nowTime, Long botid) {
        LambdaQueryWrapper<Fzzfvisitors> query = Wrappers.lambdaQuery(Fzzfvisitors.class);
        query.eq(Fzzfvisitors::getUserid, userid).eq(Fzzfvisitors::getFwtime, nowTime).eq(Fzzfvisitors::getBotid, botid);
        List<Fzzfvisitors> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Fzzfvisitors> findListByBotId(Long id) {
        LambdaQueryWrapper<Fzzfvisitors> query = Wrappers.lambdaQuery(Fzzfvisitors.class);
        query.eq(Fzzfvisitors::getBotid, id);
        return this.list(query);
    }
}
