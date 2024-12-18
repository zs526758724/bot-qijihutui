package com.bot.botb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.botb.entity.Bmydata;
import com.bot.botb.mapper.BmydataMapper;
import com.bot.botb.service.BmydataService;
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
public class BmydataServiceImpl extends ServiceImpl<BmydataMapper, Bmydata> implements BmydataService {

    @Override
    public String getValueByMyKey(String myKey) {
        LambdaQueryWrapper<Bmydata> query = Wrappers.lambdaQuery(Bmydata.class);
        query.eq(Bmydata::getMykey, myKey);
        List<Bmydata> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0).getMyvalus();
        }
        return null;
    }

    @Override
    public Bmydata getOneByMyKey(String cdstatus) {
        LambdaQueryWrapper<Bmydata> query = Wrappers.lambdaQuery(Bmydata.class);
        query.eq(Bmydata::getMykey, cdstatus);
        List<Bmydata> list = this.list(query);
        if (TeListUtil.isNotEmptyAndNull(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public void updateValueByKey(String key, String value) {
        LambdaUpdateWrapper<Bmydata> updateWrapper = Wrappers.lambdaUpdate(Bmydata.class);
        updateWrapper.eq(Bmydata::getMykey, key);
        updateWrapper.set(Bmydata::getMyvalus, value);
        this.update(updateWrapper);
    }

    @Override
    public List<Bmydata> findListByKey(String pdremove) {
        LambdaQueryWrapper<Bmydata> query = Wrappers.lambdaQuery(Bmydata.class);
        query.eq(Bmydata::getMykey, pdremove);
        return this.list(query);
    }
}
