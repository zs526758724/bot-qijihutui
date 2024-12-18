package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bpermissions;
import com.bot.botb.mapper.BpermissionsMapper;
import com.bot.botb.service.BpermissionsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author telegram
 * @since 2024-10-28
 */
@Service
//@Transactional
public class BpermissionsServiceImpl extends ServiceImpl<BpermissionsMapper, Bpermissions> implements BpermissionsService {

    @Override
    public Bpermissions getOneByChatId(Long chatId) {
        LambdaQueryWrapper<Bpermissions> queryWrapper = Wrappers.lambdaQuery(Bpermissions.class);
        queryWrapper.eq(Bpermissions::getChatid, chatId);
        try {
            return this.getOne(queryWrapper);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Bpermissions> findListByInviteuserid(Long chatId) {
        LambdaQueryWrapper<Bpermissions> queryWrapper = Wrappers.lambdaQuery(Bpermissions.class);
        queryWrapper.eq(Bpermissions::getInviteuserid, chatId);
        queryWrapper.eq(Bpermissions::getType, "channel");
        queryWrapper.eq(Bpermissions::getGroupstatus, "administrator");
        queryWrapper.eq(Bpermissions::getMsgandinvqx, "1");
        return this.list(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatId) {
        LambdaQueryWrapper<Bpermissions> queryWrapper = Wrappers.lambdaQuery(Bpermissions.class);
        queryWrapper.eq(Bpermissions::getChatid, chatId);
        this.remove(queryWrapper);
    }
}
