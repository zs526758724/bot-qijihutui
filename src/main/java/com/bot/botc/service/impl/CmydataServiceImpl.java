package com.bot.botc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botc.entity.Cmydata;
import com.bot.botc.mapper.CmydataMapper;
import com.bot.botc.service.CmydataService;
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
public class CmydataServiceImpl extends ServiceImpl<CmydataMapper, Cmydata> implements CmydataService {

    @Override
    public String getValueByMyKey(String myKey) {
        LambdaQueryWrapper<Cmydata> query = Wrappers.lambdaQuery(Cmydata.class);
        query.eq(Cmydata::getMykey, myKey);
        List<Cmydata> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0).getMyvalus();
        }
        return null;
    }

    @Override
    public Cmydata getOneByMyKey(String cdstatus) {
        LambdaQueryWrapper<Cmydata> query = Wrappers.lambdaQuery(Cmydata.class);
        query.eq(Cmydata::getMykey, cdstatus);
        List<Cmydata> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public void updateValueByKey(String key, String value) {
        LambdaUpdateWrapper<Cmydata> updateWrapper = Wrappers.lambdaUpdate(Cmydata.class);
        updateWrapper.eq(Cmydata::getMykey, key);
        updateWrapper.set(Cmydata::getMyvalus, value);
        this.update(updateWrapper);
    }

    @Override
    public List<Cmydata> findListByKey(String pdremove) {
        LambdaQueryWrapper<Cmydata> query = Wrappers.lambdaQuery(Cmydata.class);
        query.eq(Cmydata::getMykey, pdremove);
        return this.list(query);
    }
}
