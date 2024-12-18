package com.bot.botb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botb.entity.Bcopywriter;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface BcopywriterService extends IService<Bcopywriter> {

    /**
     * 通过textkey获取一条数据
     *
     * @param textkey
     * @return
     */
    Bcopywriter getOneByTextkey(String textkey);

}
