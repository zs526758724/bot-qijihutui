package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfteadmin;
import com.bot.bota.mapper.FzzfteadminMapper;
import com.bot.bota.service.FzzfteadminService;
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
public class FzzfteadminServiceImpl extends ServiceImpl<FzzfteadminMapper, Fzzfteadmin> implements FzzfteadminService {

    @Override
    public Fzzfteadmin getOneByChatId(Long chatId) {
        LambdaQueryWrapper<Fzzfteadmin> queryWrapper = Wrappers.lambdaQuery(Fzzfteadmin.class);
        queryWrapper.eq(Fzzfteadmin::getAdminid, chatId);
        queryWrapper.eq(Fzzfteadmin::getEnable, "1");
        List<Fzzfteadmin> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
