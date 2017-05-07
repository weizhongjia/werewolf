package com.msh.room.dto.event;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public enum JudgeEventType {
    CREATE_ROOM,
    //发牌
    COMPLETE_CREATE,
    //天黑
    NIGHT_COMMING,

    RESTART_GAME,
    DISBAND_GAME;
}
