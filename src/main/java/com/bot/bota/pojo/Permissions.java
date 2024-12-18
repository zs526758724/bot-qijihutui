package com.bot.bota.pojo;

import lombok.Data;

@Data
public class Permissions implements java.io.Serializable {
    private String status;//状态
    private boolean canBeEdited;//是否可以编辑
    private boolean isAnonymous;//是否匿名
    private boolean canManageChat;//是否可以管理聊天
    private boolean canPostMessages;//是否可以发消息
    private boolean canEditMessages;//是否可以编辑消息
    private boolean canDeleteMessages;//是否可以删除消息
    private boolean canRestrictMembers;//是否可以限制成员
    private boolean canPromoteMembers;//是否可以提升成员
    private boolean canChangeInfo;//是否可以修改信息
    private boolean canInviteUsers;//是否可以邀请用户
    private boolean canManageVideoChats;//是否可以管理视频聊天
    private boolean canPostStories;//是否可以发故事
    private boolean canEditStories;//是否可以编辑故事
    private boolean canDeleteStories;//是否可以删除故事

}
