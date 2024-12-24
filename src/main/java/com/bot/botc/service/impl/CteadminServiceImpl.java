package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cteadmin;
import com.bot.botc.mapper.CteadminMapper;
import com.bot.botc.service.CteadminService;
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
public class CteadminServiceImpl extends ServiceImpl<CteadminMapper, Cteadmin> implements CteadminService {

    @Override
    public Cteadmin getOneByChatId(Long chatId) {
        LambdaQueryWrapper<Cteadmin> queryWrapper = Wrappers.lambdaQuery(Cteadmin.class);
        queryWrapper.eq(Cteadmin::getAdminid, chatId);
        queryWrapper.eq(Cteadmin::getEnable, "1");
        List<Cteadmin> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
