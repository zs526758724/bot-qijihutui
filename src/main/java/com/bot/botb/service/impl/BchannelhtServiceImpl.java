package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bchannelht;
import com.bot.botb.mapper.BchannelhtMapper;
import com.bot.botb.service.BchannelhtService;
import com.bot.botb.service.BmydataService;
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
public class BchannelhtServiceImpl extends ServiceImpl<BchannelhtMapper, Bchannelht> implements BchannelhtService {

    @Autowired
    private SpidersUtils spidersUtils;

    @Autowired
    private BmydataService bmydataService;


    @Override
    public List<Bchannelht> findListByCdid(Long fzzfcdid) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getCdid, fzzfcdid);
        queryWrapper.orderByDesc(Bchannelht::getCount);
        return this.list(queryWrapper);
    }

    @Override
    public List<Bchannelht> getListByCdId(long cdid) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getCdid, cdid);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.in(Bchannelht::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public List<Bchannelht> getListBySubmitterid(Long chatId) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getSubmitterid, chatId);
        return this.list(queryWrapper);
    }

    @Override
    public Bchannelht getByChatid(String chatid) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getChatid, chatid);
        return this.getOne(queryWrapper);
    }

    @Override
    public long countByCdid(Integer id) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getCdid, id);
        return this.count(queryWrapper);
    }

    @Override
    public List<Bchannelht> findListByCdidAndSH1(Long aLong) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getCdid, aLong);
        queryWrapper.eq(Bchannelht::getAudit, "1");
        return this.list(queryWrapper);
    }

    @Override
    public List<Bchannelht> findListByAudit(String number) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getAudit, number);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Bchannelht> listOrderByCdid(int current) {
        int size = Integer.parseInt(bmydataService.getValueByMyKey("pdpagesize"));
        Page<Bchannelht> page = new Page<>(current, size);
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.orderByAsc(Bchannelht::getCdid);
        queryWrapper.orderByDesc(Bchannelht::getCount);
        return this.page(page, queryWrapper);
    }

    @Override
    public void refreshCount() {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        List<Bchannelht> list = this.list(queryWrapper);
        for (Bchannelht fzzfchannelht : list) {
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
    public Bchannelht getByInviteLink(String inviteLink1) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getInvitelink, inviteLink1);
        List<Bchannelht> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Bchannelht> findListBySubmitterId(Long chatid) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getSubmitterid, chatid);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Bchannelht> getPagesBySubmitterId(Long chatId, int current) {
        int size = Integer.parseInt(bmydataService.getValueByMyKey("pdpagesize"));
        Page<Bchannelht> page = new Page<>(current, size);
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getSubmitterid, chatId);
        queryWrapper.orderByAsc(Bchannelht::getCdid);
        queryWrapper.orderByDesc(Bchannelht::getCount);
        return this.page(page, queryWrapper);
    }

    @Override
    public long countByCdidYTG(Integer id) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getCdid, id);
        queryWrapper.eq(Bchannelht::getAudit, "1");
        return this.count(queryWrapper);
    }

    @Override
    public void removeByChatid(String chatId) {
        LambdaQueryWrapper<Bchannelht> queryWrapper = Wrappers.lambdaQuery(Bchannelht.class);
        queryWrapper.eq(Bchannelht::getChatid, chatId);
        this.remove(queryWrapper);
    }
}
