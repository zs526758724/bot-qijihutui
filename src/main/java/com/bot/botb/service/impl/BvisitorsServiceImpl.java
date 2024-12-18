package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bvisitors;
import com.bot.botb.mapper.BvisitorsMapper;
import com.bot.botb.service.BvisitorsService;
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
public class BvisitorsServiceImpl extends ServiceImpl<BvisitorsMapper, Bvisitors> implements BvisitorsService {

    @Override
    public Bvisitors findByUseridAndFwtimeAndBotid(Long userid, String nowTime, Long botid) {
        LambdaQueryWrapper<Bvisitors> query = Wrappers.lambdaQuery(Bvisitors.class);
        query.eq(Bvisitors::getUserid, userid).eq(Bvisitors::getFwtime, nowTime).eq(Bvisitors::getBotid, botid);
        List<Bvisitors> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Bvisitors> findListByBotId(Long id) {
        LambdaQueryWrapper<Bvisitors> query = Wrappers.lambdaQuery(Bvisitors.class);
        query.eq(Bvisitors::getBotid, id);
        return this.list(query);
    }
}
