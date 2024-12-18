package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bcopywriter;
import com.bot.botb.mapper.BcopywriterMapper;
import com.bot.botb.service.BcopywriterService;
import com.bot.common.utils.TeListUtil;
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
public class BcopywriterServiceImpl extends ServiceImpl<BcopywriterMapper, Bcopywriter> implements BcopywriterService {

    @Override
    public Bcopywriter getOneByTextkey(String textkey) {
        LambdaQueryWrapper<Bcopywriter> queryWrapper = Wrappers.lambdaQuery(Bcopywriter.class);
        queryWrapper.eq(Bcopywriter::getTextkey, textkey);
        List<Bcopywriter> list = this.list(queryWrapper);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }
}
