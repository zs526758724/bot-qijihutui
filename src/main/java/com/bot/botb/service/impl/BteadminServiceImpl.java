package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bteadmin;
import com.bot.botb.mapper.BteadminMapper;
import com.bot.botb.service.BteadminService;
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
public class BteadminServiceImpl extends ServiceImpl<BteadminMapper, Bteadmin> implements BteadminService {

    @Override
    public Bteadmin getOneByChatId(Long chatId) {
        LambdaQueryWrapper<Bteadmin> queryWrapper = Wrappers.lambdaQuery(Bteadmin.class);
        queryWrapper.eq(Bteadmin::getAdminid, chatId);
        queryWrapper.eq(Bteadmin::getEnable, "1");
        List<Bteadmin> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
