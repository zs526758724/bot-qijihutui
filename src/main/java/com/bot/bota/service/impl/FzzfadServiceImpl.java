package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfad;
import com.bot.bota.mapper.FzzfadMapper;
import com.bot.bota.service.FzzfadService;
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
public class FzzfadServiceImpl extends ServiceImpl<FzzfadMapper, Fzzfad> implements FzzfadService {

    @Override
    public List<Fzzfad> findByAdtype(String number) {
        LambdaQueryWrapper<Fzzfad> queryWrapper = Wrappers.lambdaQuery(Fzzfad.class);
        queryWrapper.eq(Fzzfad::getFlag, number);
        queryWrapper.eq(Fzzfad::getStatus, "1");
        return this.list(queryWrapper);
    }

    @Override
    public List<Fzzfad> findAllByAdtype(String number) {
        LambdaQueryWrapper<Fzzfad> queryWrapper = Wrappers.lambdaQuery(Fzzfad.class);
        queryWrapper.eq(Fzzfad::getFlag, number);
        return this.list(queryWrapper);
    }
}
