package com.msh.room.dto.event;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public enum JudgeEventType {
    CREATE_ROOM,
    //发牌
    COMPLETE_CREATE,
    //天黑
    NIGHT_COMING,
    //狼人杀人
    WOLF_KILL,

    //预言家验人
    SEER_VERIFY,
    //假装预言家验人
    FAKE_SEER_VERIFY,

    //女巫救人
    WITCH_SAVE,
    FAKE_WITCH_SAVE,
    //女巫毒人
    WITCH_POISON,
    FAKE_WITCH_POISON,

    //天亮了
    DAYTIME_COMING,
    //开始投票
    DAYTIME_VOTING,

    //重开游戏
    RESTART_GAME,
    //解散游戏
    DISBAND_GAME
}
