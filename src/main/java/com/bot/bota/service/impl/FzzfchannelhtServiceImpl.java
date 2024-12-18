package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfchannelht;
import com.bot.bota.mapper.FzzfchannelhtMapper;
import com.bot.bota.service.FzzfchannelhtService;
import com.bot.bota.service.FzzfmydataService;
import com.bot.common.utils.SpidersUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
@Transactional
public class FzzfchannelhtServiceImpl extends ServiceImpl<FzzfchannelhtMapper, Fzzfchannelht> implements FzzfchannelhtService {

    @Autowired
    private SpidersUtils spidersUtils;

    @Autowired
    private FzzfmydataService fzzfmydataService;


    @Override
    public List<Fzzfchannelht> findListByCdid(Long fzzfcdid) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getCdid, fzzfcdid);
        queryWrapper.orderByDesc(Fzzfchannelht::getCount);
        return this.list(queryWrapper);
    }

    @Override
    public List<Fzzfchannelht> getListByCdId(long cdid) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getCdid, cdid);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.in(Fzzfchannelht::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public List<Fzzfchannelht> getListBySubmitterid(Long chatId) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getSubmitterid, chatId);
        return this.list(queryWrapper);
    }

    @Override
    public Fzzfchannelht getByChatid(String chatid) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getChatid, chatid);
        return this.getOne(queryWrapper);
    }

    @Override
    public long countByCdid(Integer id) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getCdid, id);
        return this.count(queryWrapper);
    }

    @Override
    public List<Fzzfchannelht> findListByCdidAndSH1(Long aLong) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getCdid, aLong);
        queryWrapper.eq(Fzzfchannelht::getAudit, "1");
        return this.list(queryWrapper);
    }

    @Override
    public List<Fzzfchannelht> findListByAudit(String number) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getAudit, number);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Fzzfchannelht> listOrderByCdid(int current) {
        int size = Integer.parseInt(fzzfmydataService.getValueByMyKey("pdpagesize"));
        Page<Fzzfchannelht> page = new Page<>(current, size);
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.orderByAsc(Fzzfchannelht::getCdid);
        queryWrapper.orderByDesc(Fzzfchannelht::getCount);
        return this.page(page, queryWrapper);
    }

    @Override
    public void refreshCount() {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        List<Fzzfchannelht> list = this.list(queryWrapper);
        for (Fzzfchannelht fzzfchannelht : list) {
            int channelCount = spidersUtils.getChannelCount(fzzfchannelht.getUrl());
            if (channelCount > 0) {
                fzzfchannelht.setCount(channelCount);
                System.out.println("更新频道：" + fzzfchannelht.getChatid() + "  频道数：" + channelCount);
                this.updateById(fzzfchannelht);
            } else {
                int channelCount2 = spidersUtils.getChannelCount(fzzfchannelht.getInvitelink());
                if (channelCount2 > 0) {
                    fzzfchannelht.setCount(channelCount2);
                    System.out.println("更新频道：" + fzzfchannelht.getChatid() + "  频道数：" + channelCount2);
                    this.updateById(fzzfchannelht);
                }
            }
        }
    }

    @Override
    public Fzzfchannelht getByInviteLink(String inviteLink1) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getInvitelink, inviteLink1);
        List<Fzzfchannelht> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Fzzfchannelht> findListBySubmitterId(Long chatid) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getSubmitterid, chatid);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Fzzfchannelht> getPagesBySubmitterId(Long chatId, int current) {
        int size = Integer.parseInt(fzzfmydataService.getValueByMyKey("pdpagesize"));
        Page<Fzzfchannelht> page = new Page<>(current, size);
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getSubmitterid, chatId);
        queryWrapper.orderByAsc(Fzzfchannelht::getCdid);
        queryWrapper.orderByDesc(Fzzfchannelht::getCount);
        return this.page(page, queryWrapper);
    }

    @Override
    public long countByCdidYTG(Integer id) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getCdid, id);
        queryWrapper.eq(Fzzfchannelht::getAudit, "1");
        return this.count(queryWrapper);
    }

    @Override
    public void removeByChatid(String chatId) {
        LambdaQueryWrapper<Fzzfchannelht> queryWrapper = Wrappers.lambdaQuery(Fzzfchannelht.class);
        queryWrapper.eq(Fzzfchannelht::getChatid, chatId);
        this.remove(queryWrapper);
    }
}
