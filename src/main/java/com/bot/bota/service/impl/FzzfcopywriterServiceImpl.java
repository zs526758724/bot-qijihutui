package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfcopywriter;
import com.bot.bota.mapper.FzzfcopywriterMapper;
import com.bot.bota.service.FzzfcopywriterService;
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
public class FzzfcopywriterServiceImpl extends ServiceImpl<FzzfcopywriterMapper, Fzzfcopywriter> implements FzzfcopywriterService {

    @Override
    public Fzzfcopywriter getOneByTextkey(String textkey) {
        LambdaQueryWrapper<Fzzfcopywriter> queryWrapper = Wrappers.lambdaQuery(Fzzfcopywriter.class);
        queryWrapper.eq(Fzzfcopywriter::getTextkey, textkey);
        List<Fzzfcopywriter> list = this.list(queryWrapper);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }
}
