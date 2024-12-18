package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfchannelmsg;
import com.bot.bota.mapper.FzzfchannelmsgMapper;
import com.bot.bota.service.FzzfchannelmsgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
@Service
@Transactional
public class FzzfchannelmsgServiceImpl extends ServiceImpl<FzzfchannelmsgMapper, Fzzfchannelmsg> implements FzzfchannelmsgService {

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Fzzfchannelmsg> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelmsg.class);
        queryWrapper.in(Fzzfchannelmsg::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatid) {
        LambdaQueryWrapper<Fzzfchannelmsg> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelmsg.class);
        queryWrapper.eq(Fzzfchannelmsg::getChatid, chatid);
        this.remove(queryWrapper);
    }
}
