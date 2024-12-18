package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bfansrecordsin;
import com.bot.botb.mapper.BfansrecordsinMapper;
import com.bot.botb.service.BfansrecordsinService;
import com.bot.common.utils.TeTimeUtil;
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
public class BfansrecordsinServiceImpl extends ServiceImpl<BfansrecordsinMapper, Bfansrecordsin> implements BfansrecordsinService {

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Bfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Bfansrecordsin.class);
        queryWrapper.in(Bfansrecordsin::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatid) {
        LambdaQueryWrapper<Bfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Bfansrecordsin.class);
        queryWrapper.eq(Bfansrecordsin::getChatid, chatid);
        this.remove(queryWrapper);
    }

    @Override
    public void fansAdd(String chatid) {
        LambdaQueryWrapper<Bfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Bfansrecordsin.class);
        queryWrapper.eq(Bfansrecordsin::getChatid, chatid);
        queryWrapper.eq(Bfansrecordsin::getDate, TeTimeUtil.getNowTime());
        Bfansrecordsin fansRecord = this.getOne(queryWrapper);
        if (fansRecord == null) {
            Bfansrecordsin fansRecordNew = new Bfansrecordsin();
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
    public List<Bfansrecordsin> findListByChatId(String chatid) {
        LambdaQueryWrapper<Bfansrecordsin> queryWrapper = Wrappers.lambdaQuery(Bfansrecordsin.class);
        queryWrapper.eq(Bfansrecordsin::getChatid, chatid);
        return this.list(queryWrapper);
    }
}
