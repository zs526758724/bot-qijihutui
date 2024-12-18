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
 * @since 2024-10-29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("bchannelht")
public class Bchannelht implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 频道id
     */
    @TableField("chatid")
    private String chatid;

    /**
     * 人数
     */
    @TableField("count")
    private Integer count;

    /**
     * 申请时间
     */
    @TableField("createtime")
    private String createtime;

    /**
     * 审核时间
     */
    @TableField("shtime")
    private String shtime;

    /**
     * 提交者id
     */
    @TableField("submitterid")
    private String submitterid;

    /**
     * 审核者id
     */
    @TableField("reviewersid")
    private String reviewersid;

    /**
     * 是否审核通过
     */
    @TableField("audit")
    private String audit;

    /**
     * 邀请链接
     */
    @TableField("invitelink")
    private String invitelink;

    /**
     * 车队id
     */
    @TableField("cdid")
    private Integer cdid;

    /**
     * 车队id
     */
    @TableField("url")
    private String url;


}
