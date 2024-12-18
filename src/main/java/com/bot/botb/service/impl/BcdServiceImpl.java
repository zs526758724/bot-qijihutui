package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bcd;
import com.bot.botb.mapper.BcdMapper;
import com.bot.botb.service.BcdService;
import com.bot.botb.service.BmydataService;
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
public class BcdServiceImpl extends ServiceImpl<BcdMapper, Bcd> implements BcdService {
    @Autowired
    private BmydataService bmydataService;

    @Override
    public Page<Bcd> pageList(int current) {
        String fzpagesize = bmydataService.getValueByMyKey("fzpagesize");
        Page<Bcd> page = new Page<>(current, Long.parseLong(fzpagesize));
        LambdaQueryWrapper<Bcd> queryWrapper = Wrappers.lambdaQuery(Bcd.class);
        return this.page(page, queryWrapper);
    }
}
