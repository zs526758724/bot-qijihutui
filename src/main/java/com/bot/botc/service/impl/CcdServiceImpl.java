package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Ccd;
import com.bot.botc.mapper.CcdMapper;
import com.bot.botc.service.CcdService;
import com.bot.botc.service.CmydataService;
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
public class CcdServiceImpl extends ServiceImpl<CcdMapper, Ccd> implements CcdService {
    @Autowired
    private CmydataService cmydataService;

    @Override
    public Page<Ccd> pageList(int current) {
        String fzpagesize = cmydataService.getValueByMyKey("fzpagesize");
        Page<Ccd> page = new Page<>(current, Long.parseLong(fzpagesize));
        LambdaQueryWrapper<Ccd> queryWrapper = Wrappers.lambdaQuery(Ccd.class);
        return this.page(page, queryWrapper);
    }
}
