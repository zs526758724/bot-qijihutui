package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfcd;
import com.bot.bota.mapper.FzzfcdMapper;
import com.bot.bota.service.FzzfcdService;
import com.bot.bota.service.FzzfmydataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class FzzfcdServiceImpl extends ServiceImpl<FzzfcdMapper, Fzzfcd> implements FzzfcdService {
    @Autowired
    private FzzfmydataService fzzfmydataService;

    @Override
    public Page<Fzzfcd> pageList(int current) {
        String fzpagesize = fzzfmydataService.getValueByMyKey("fzpagesize");
        Page<Fzzfcd> page = new Page<>(current, Long.parseLong(fzpagesize));
        LambdaQueryWrapper<Fzzfcd> queryWrapper = Wrappers.lambdaQuery(Fzzfcd.class);
        return this.page(page, queryWrapper);
    }
}
