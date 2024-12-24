package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Ccopywriter;
import com.bot.botc.mapper.CcopywriterMapper;
import com.bot.botc.service.CcopywriterService;
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
public class CcopywriterServiceImpl extends ServiceImpl<CcopywriterMapper, Ccopywriter> implements CcopywriterService {

    @Override
    public Ccopywriter getOneByTextkey(String textkey) {
        LambdaQueryWrapper<Ccopywriter> queryWrapper = Wrappers.lambdaQuery(Ccopywriter.class);
        queryWrapper.eq(Ccopywriter::getTextkey, textkey);
        List<Ccopywriter> list = this.list(queryWrapper);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }
}
