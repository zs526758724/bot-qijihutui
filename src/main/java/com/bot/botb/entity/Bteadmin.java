package com.bot.botb.entity;

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
 * @since 2024-10-28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("bteadmin")
public class Bteadmin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 聊天id
     */
    @TableField("adminid")
    private String adminid;

    /**
     * 是否启用
     */
    @TableField("enable")
    private String enable;

    /**
     * 是否是超级管理员
     */
    @TableField("isuperadmin")
    private String isuperadmin;


}
