package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cad;
import com.bot.botc.mapper.CadMapper;
import com.bot.botc.service.CadService;
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
public class CadServiceImpl extends ServiceImpl<CadMapper, Cad> implements CadService {

    @Override
    public List<Cad> findByAdtype(String number) {
        LambdaQueryWrapper<Cad> queryWrapper = Wrappers.lambdaQuery(Cad.class);
        queryWrapper.eq(Cad::getFlag, number);
        queryWrapper.eq(Cad::getStatus, "1");
        return this.list(queryWrapper);
    }

    @Override
    public List<Cad> findAllByAdtype(String number) {
        LambdaQueryWrapper<Cad> queryWrapper = Wrappers.lambdaQuery(Cad.class);
        queryWrapper.eq(Cad::getFlag, number);
        return this.list(queryWrapper);
    }
}
