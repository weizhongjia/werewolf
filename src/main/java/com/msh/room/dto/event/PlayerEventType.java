package com.msh.room.dto.event;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public enum PlayerEventType {
    JOIN_ROOM,
    Exit_ROOM,
    //狼人指定杀人对象
    WOLF_KILL,
    //预言家验人
    SEER_VERIFY,
    //女巫救人
    WITCH_SAVE,
    //假装女巫救人
    FAKE_WITCH_SAVE,
    //女巫毒人
    WITCH_POISON,
    //假装女巫毒人
    FAKE_WITCH_POISON,

    //玩家白天投票
    DAYTIME_VOTE,
    //玩家PK阶段投票
    PK_VOTE,
    //猎人时间
    HUNTER_SHOOT,
    //白痴翻拍
    MORON_SHOW,
    //上警
    SHERIFF_REGISTER,
    //退出竞选
    SHERIFF_UNREGISTER,

    //上警投票
    SHERIFF_VOTE,
    //上警PK投票
    SHERIFF_PK_VOTE,
    //警徽移交
    SHERIFF_SWITCH,;
}
