package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfpermissions;
import com.bot.bota.mapper.FzzfpermissionsMapper;
import com.bot.bota.service.FzzfpermissionsService;
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
@Transactional
public class FzzfpermissionsServiceImpl extends ServiceImpl<FzzfpermissionsMapper, Fzzfpermissions> implements FzzfpermissionsService {

    @Override
    public Fzzfpermissions getOneByChatId(Long chatId) {
        LambdaQueryWrapper<Fzzfpermissions> queryWrapper = Wrappers.lambdaQuery(Fzzfpermissions.class);
        queryWrapper.eq(Fzzfpermissions::getChatid, chatId);
        try {
            return this.getOne(queryWrapper);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Fzzfpermissions> findListByInviteuserid(Long chatId) {
        LambdaQueryWrapper<Fzzfpermissions> queryWrapper = Wrappers.lambdaQuery(Fzzfpermissions.class);
        queryWrapper.eq(Fzzfpermissions::getInviteuserid, chatId);
        queryWrapper.eq(Fzzfpermissions::getType, "channel");
        queryWrapper.eq(Fzzfpermissions::getGroupstatus, "administrator");
        queryWrapper.eq(Fzzfpermissions::getMsgandinvqx, "1");
        return this.list(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatId) {
        LambdaQueryWrapper<Fzzfpermissions> queryWrapper = Wrappers.lambdaQuery(Fzzfpermissions.class);
        queryWrapper.eq(Fzzfpermissions::getChatid, chatId);
        this.remove(queryWrapper);
    }
}
