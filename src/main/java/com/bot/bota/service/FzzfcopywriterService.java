package com.bot.bota.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bota.entity.Fzzfcopywriter;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
public interface FzzfcopywriterService extends IService<Fzzfcopywriter> {

    /**
     * 通过textkey获取一条数据
     *
     * @param textkey
     * @return
     */
    Fzzfcopywriter getOneByTextkey(String textkey);

}
