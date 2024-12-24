package com.bot.botc.entity;

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
@TableName("ccd")
public class Ccd implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 车队标题
     */
    @TableField("title")
    private String title;

    /**
     * 车队描述
     */
    @TableField("cddesc")
    private String cddesc;

    /**
     * 最低订阅量
     */
    @TableField("minisubscription")
    private int minisubscription;

    /**
     * 最低阅读
     */
    @TableField("miniread")
    private int miniread;

    /**
     * 车队类型
     */
    @TableField("type")
    private String type;


}
