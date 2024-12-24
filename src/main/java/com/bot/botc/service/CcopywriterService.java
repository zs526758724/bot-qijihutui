package com.bot.botc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.botc.entity.Ccopywriter;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface CcopywriterService extends IService<Ccopywriter> {

    /**
     * 通过textkey获取一条数据
     *
     * @param textkey
     * @return
     */
    Ccopywriter getOneByTextkey(String textkey);

}
