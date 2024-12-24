package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cchannelht;
import com.bot.botc.mapper.CchannelhtMapper;
import com.bot.botc.service.CchannelhtService;
import com.bot.botc.service.CmydataService;
import com.bot.common.utils.SpidersUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
//@Transactional
public class CchannelhtServiceImpl extends ServiceImpl<CchannelhtMapper, Cchannelht> implements CchannelhtService {

    @Autowired
    private SpidersUtils spidersUtils;

    @Autowired
    private CmydataService cmydataService;


    @Override
    public List<Cchannelht> findListByCdid(Long fzzfcdid) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getCdid, fzzfcdid);
        queryWrapper.orderByDesc(Cchannelht::getCount);
        return this.list(queryWrapper);
    }

    @Override
    public List<Cchannelht> getListByCdId(long cdid) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getCdid, cdid);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.in(Cchannelht::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public List<Cchannelht> getListBySubmitterid(Long chatId) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getSubmitterid, chatId);
        return this.list(queryWrapper);
    }

    @Override
    public Cchannelht getByChatid(String chatid) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getChatid, chatid);
        return this.getOne(queryWrapper);
    }

    @Override
    public long countByCdid(Integer id) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getCdid, id);
        return this.count(queryWrapper);
    }

    @Override
    public List<Cchannelht> findListByCdidAndSH1(Long aLong) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getCdid, aLong);
        queryWrapper.eq(Cchannelht::getAudit, "1");
        return this.list(queryWrapper);
    }

    @Override
    public List<Cchannelht> findListByAudit(String number) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getAudit, number);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Cchannelht> listOrderByCdid(int current) {
        int size = Integer.parseInt(cmydataService.getValueByMyKey("pdpagesize"));
        Page<Cchannelht> page = new Page<>(current, size);
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.orderByAsc(Cchannelht::getCdid);
        queryWrapper.orderByDesc(Cchannelht::getCount);
        return this.page(page, queryWrapper);
    }

    @Override
    public void refreshCount() {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        List<Cchannelht> list = this.list(queryWrapper);
        for (Cchannelht fzzfchannelht : list) {
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
    public Cchannelht getByInviteLink(String inviteLink1) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getInvitelink, inviteLink1);
        List<Cchannelht> list = this.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Cchannelht> findListBySubmitterId(Long chatid) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getSubmitterid, chatid);
        return this.list(queryWrapper);
    }

    @Override
    public Page<Cchannelht> getPagesBySubmitterId(Long chatId, int current) {
        int size = Integer.parseInt(cmydataService.getValueByMyKey("pdpagesize"));
        Page<Cchannelht> page = new Page<>(current, size);
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getSubmitterid, chatId);
        queryWrapper.orderByAsc(Cchannelht::getCdid);
        queryWrapper.orderByDesc(Cchannelht::getCount);
        return this.page(page, queryWrapper);
    }

    @Override
    public long countByCdidYTG(Integer id) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getCdid, id);
        queryWrapper.eq(Cchannelht::getAudit, "1");
        return this.count(queryWrapper);
    }

    @Override
    public void removeByChatid(String chatId) {
        LambdaQueryWrapper<Cchannelht> queryWrapper = Wrappers.lambdaQuery(Cchannelht.class);
        queryWrapper.eq(Cchannelht::getChatid, chatId);
        this.remove(queryWrapper);
    }
}
