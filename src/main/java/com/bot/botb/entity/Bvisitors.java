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
@TableName("bvisitors")
public class Bvisitors implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 访问时间
     */
    @TableField("fwtime")
    private String fwtime;

    @TableField("username")
    private String username;

    @TableField("firstname")
    private String firstname;

    @TableField("lastname")
    private String lastname;

    @TableField("userid")
    private String userid;

    @TableField("botid")
    private String botid;


}
