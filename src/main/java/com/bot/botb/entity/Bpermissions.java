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
@TableName("bpermissions")
public class Bpermissions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 群id
     */
    @TableField("chatid")
    private String chatid;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 公开url
     */
    @TableField("url")
    private String url;

    /**
     * 邀请链接
     */
    @TableField("inviteurl")
    private String inviteurl;

    /**
     * 在群内状态
     */
    @TableField("groupstatus")
    private String groupstatus;

    /**
     * 创建时间
     */
    @TableField("createtime")
    private String createtime;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 是否拥有消息管理与邀请权限
     */
    @TableField("msgandinvqx")
    private String msgandinvqx;

    /**
     * 邀请者id
     */
    @TableField("inviteuserid")
    private String inviteuserid;

    /**
     * 人数
     */
    @TableField("count")
    private int count;


}
