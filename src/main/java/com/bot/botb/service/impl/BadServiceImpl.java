package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bad;
import com.bot.botb.mapper.BadMapper;
import com.bot.botb.service.BadService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
@Service
//@Transactional
public class BadServiceImpl extends ServiceImpl<BadMapper, Bad> implements BadService {

    @Override
    public List<Bad> findByAdtype(String number) {
        LambdaQueryWrapper<Bad> queryWrapper = Wrappers.lambdaQuery(Bad.class);
        queryWrapper.eq(Bad::getFlag, number);
        queryWrapper.eq(Bad::getStatus, "1");
        return this.list(queryWrapper);
    }

    @Override
    public List<Bad> findAllByAdtype(String number) {
        LambdaQueryWrapper<Bad> queryWrapper = Wrappers.lambdaQuery(Bad.class);
        queryWrapper.eq(Bad::getFlag, number);
        return this.list(queryWrapper);
    }
}
