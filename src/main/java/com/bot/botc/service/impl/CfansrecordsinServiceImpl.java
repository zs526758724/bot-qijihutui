package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cfansrecordsin;
import com.bot.botc.mapper.CfansrecordsinMapper;
import com.bot.botc.service.CfansrecordsinService;
import com.bot.common.utils.TeTimeUtil;
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
public class CfansrecordsinServiceImpl extends ServiceImpl<CfansrecordsinMapper, Cfansrecordsin> implements CfansrecordsinService {

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Cfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Cfansrecordsin.class);
        queryWrapper.in(Cfansrecordsin::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatid) {
        LambdaQueryWrapper<Cfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Cfansrecordsin.class);
        queryWrapper.eq(Cfansrecordsin::getChatid, chatid);
        this.remove(queryWrapper);
    }

    @Override
    public void fansAdd(String chatid) {
        LambdaQueryWrapper<Cfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Cfansrecordsin.class);
        queryWrapper.eq(Cfansrecordsin::getChatid, chatid);
        queryWrapper.eq(Cfansrecordsin::getDate, TeTimeUtil.getNowTime());
        Cfansrecordsin fansRecord = this.getOne(queryWrapper);
        if (fansRecord == null) {
            Cfansrecordsin fansRecordNew = new Cfansrecordsin();
            fansRecordNew.setChatid(chatid);
            fansRecordNew.setDate(TeTimeUtil.getNowTime());
            fansRecordNew.setFanscount(1);
            this.save(fansRecordNew);
        } else {
            fansRecord.setFanscount(fansRecord.getFanscount() + 1);
            this.updateById(fansRecord);
        }
    }

    @Override
    public List<Cfansrecordsin> findListByChatId(String chatid) {
        LambdaQueryWrapper<Cfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Cfansrecordsin.class);
        queryWrapper.eq(Cfansrecordsin::getChatid, chatid);
        return this.list(queryWrapper);
    }
}
