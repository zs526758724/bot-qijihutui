package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bchannelmsg;
import com.bot.botb.mapper.BchannelmsgMapper;
import com.bot.botb.service.BchannelmsgService;
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
public class BchannelmsgServiceImpl extends ServiceImpl<BchannelmsgMapper, Bchannelmsg> implements BchannelmsgService {

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Bchannelmsg> queryWrapper = Wrappers.lambdaQuery(Bchannelmsg.class);
        queryWrapper.in(Bchannelmsg::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatid) {
        LambdaQueryWrapper<Bchannelmsg> queryWrapper = Wrappers.lambdaQuery(Bchannelmsg.class);
        queryWrapper.eq(Bchannelmsg::getChatid, chatid);
        this.remove(queryWrapper);
    }
}
