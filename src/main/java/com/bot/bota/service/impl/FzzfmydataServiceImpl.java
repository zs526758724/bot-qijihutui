package com.bot.bota.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bota.entity.Fzzfmydata;
import com.bot.bota.mapper.FzzfmydataMapper;
import com.bot.bota.service.FzzfmydataService;
import com.bot.common.utils.TeListUtil;
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
public class FzzfmydataServiceImpl extends ServiceImpl<FzzfmydataMapper, Fzzfmydata> implements FzzfmydataService {

    @Override
    public String getValueByMyKey(String myKey) {
        LambdaQueryWrapper<Fzzfmydata> query = Wrappers.lambdaQuery(Fzzfmydata.class);
        query.eq(Fzzfmydata::getMykey, myKey);
        List<Fzzfmydata> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0).getMyvalus();
        }
        return null;
    }

    @Override
    public Fzzfmydata getOneByMyKey(String cdstatus) {
        LambdaQueryWrapper<Fzzfmydata> query = Wrappers.lambdaQuery(Fzzfmydata.class);
        query.eq(Fzzfmydata::getMykey, cdstatus);
        List<Fzzfmydata> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public void updateValueByKey(String key, String value) {
        LambdaUpdateWrapper<Fzzfmydata> updateWrapper = Wrappers.lambdaUpdate(Fzzfmydata.class);
        updateWrapper.eq(Fzzfmydata::getMykey, key);
        updateWrapper.set(Fzzfmydata::getMyvalus, value);
        this.update(updateWrapper);
    }

    @Override
    public List<Fzzfmydata> findListByKey(String pdremove) {
        LambdaQueryWrapper<Fzzfmydata> query = Wrappers.lambdaQuery(Fzzfmydata.class);
        query.eq(Fzzfmydata::getMykey, pdremove);
        return this.list(query);
    }
}
