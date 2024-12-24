package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cchannelmsg;
import com.bot.botc.mapper.CchannelmsgMapper;
import com.bot.botc.service.CchannelmsgService;
import org.springframework.stereotype.Service;

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
//@Transactional
public class CchannelmsgServiceImpl extends ServiceImpl<CchannelmsgMapper, Cchannelmsg> implements CchannelmsgService {

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Cchannelmsg> queryWrapper = Wrappers.lambdaQuery(Cchannelmsg.class);
        queryWrapper.in(Cchannelmsg::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatid) {
        LambdaQueryWrapper<Cchannelmsg> queryWrapper = Wrappers.lambdaQuery(Cchannelmsg.class);
        queryWrapper.eq(Cchannelmsg::getChatid, chatid);
        this.remove(queryWrapper);
    }
}
