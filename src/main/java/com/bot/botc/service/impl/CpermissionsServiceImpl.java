package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cpermissions;
import com.bot.botc.mapper.CpermissionsMapper;
import com.bot.botc.service.CpermissionsService;
import org.springframework.stereotype.Service;

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
public class CpermissionsServiceImpl extends ServiceImpl<CpermissionsMapper, Cpermissions> implements CpermissionsService {

    @Override
    public Cpermissions getOneByChatId(Long chatId) {
        LambdaQueryWrapper<Cpermissions> queryWrapper = Wrappers.lambdaQuery(Cpermissions.class);
        queryWrapper.eq(Cpermissions::getChatid, chatId);
        try {
            return this.getOne(queryWrapper);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Cpermissions> findListByInviteuserid(Long chatId) {
        LambdaQueryWrapper<Cpermissions> queryWrapper = Wrappers.lambdaQuery(Cpermissions.class);
        queryWrapper.eq(Cpermissions::getInviteuserid, chatId);
        queryWrapper.eq(Cpermissions::getType, "channel");
        queryWrapper.eq(Cpermissions::getGroupstatus, "administrator");
        queryWrapper.eq(Cpermissions::getMsgandinvqx, "1");
        return this.list(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatId) {
        LambdaQueryWrapper<Cpermissions> queryWrapper = Wrappers.lambdaQuery(Cpermissions.class);
        queryWrapper.eq(Cpermissions::getChatid, chatId);
        this.remove(queryWrapper);
    }
}
