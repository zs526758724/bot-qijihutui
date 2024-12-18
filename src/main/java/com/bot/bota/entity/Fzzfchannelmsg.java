package com.bot.bota.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author telegram
 * @since 2024-10-29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("fzzfchannelmsg")
public class Fzzfchannelmsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 频道id
     */
    @TableField("chatid")
    private String chatid;

    /**
     * 消息id
     */
    @TableField("messageid")
    private String messageid;

    /**
     * 最新消息id
     */
    @TableField("bestmessageid")
    private String bestmessageid;

    /**
     * 发送消息时间
     */
    @TableField("createdate")
    private String createdate;


}
