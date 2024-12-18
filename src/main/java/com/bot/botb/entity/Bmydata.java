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
@TableName("bmydata")
public class Bmydata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 键
     */
    @TableField("mykey")
    private String mykey;

    /**
     * 值
     */
    @TableField("myvalus")
    private String myvalus;

    /**
     * 描述
     */
    @TableField("mydesc")
    private String mydesc;


}
