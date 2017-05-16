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
    FAKE_WITCH_POISON
}
