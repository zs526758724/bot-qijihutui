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
@TableName("ccopywriter")
public class Ccopywriter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文案内容
     */
    @TableField("textcontent")
    private String textcontent;

    /**
     * 文案key
     */
    @TableField("textkey")
    private String textkey;

    /**
     * 文案格式
     */
    @TableField("textentities")
    private String textentities;


}
