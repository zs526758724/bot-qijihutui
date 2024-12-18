package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzffansrecordsin;
import com.bot.bota.mapper.FzzffansrecordsinMapper;
import com.bot.bota.service.FzzffansrecordsinService;
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
//@Transactional
public class FzzffansrecordsinServiceImpl extends ServiceImpl<FzzffansrecordsinMapper, Fzzffansrecordsin> implements FzzffansrecordsinService {

    @Override
    public void removeByChatids(HashSet<String> chatids) {
        LambdaQueryWrapper<Fzzffansrecordsin> queryWrapper = Wrappers.lambdaQuery(Fzzffansrecordsin.class);
        queryWrapper.in(Fzzffansrecordsin::getChatid, chatids);
        this.remove(queryWrapper);
    }

    @Override
    public void deleteBychatId(String chatid) {
        LambdaQueryWrapper<Fzzffansrecordsin> queryWrapper = Wrappers.lambdaQuery(Fzzffansrecordsin.class);
        queryWrapper.eq(Fzzffansrecordsin::getChatid, chatid);
        this.remove(queryWrapper);
    }

    @Override
    public void fansAdd(String chatid) {
        LambdaQueryWrapper<Fzzffansrecordsin> queryWrapper = Wrappers.lambdaQuery(Fzzffansrecordsin.class);
        queryWrapper.eq(Fzzffansrecordsin::getChatid, chatid);
        queryWrapper.eq(Fzzffansrecordsin::getDate, TeTimeUtil.getNowTime());
        Fzzffansrecordsin fansRecord = this.getOne(queryWrapper);
        if (fansRecord == null) {
            Fzzffansrecordsin fansRecordNew = new Fzzffansrecordsin();
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
    public List<Fzzffansrecordsin> findListByChatId(String chatid) {
        LambdaQueryWrapper<Fzzffansrecordsin> queryWrapper = Wrappers.lambdaQuery(Fzzffansrecordsin.class);
        queryWrapper.eq(Fzzffansrecordsin::getChatid, chatid);
        return this.list(queryWrapper);
    }
}
