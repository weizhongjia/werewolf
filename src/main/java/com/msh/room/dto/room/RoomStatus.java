package com.msh.room.dto.room;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public enum RoomStatus {
    //空闲中
    VACANCY,

    //创建中
    CRATING,

    //创建完成
    CRATED,

    //夜晚
    NIGHT,
    //白天发言
    DAYTIME,

    //投票
    VOTING,
    //PK发言
    PK,
    //PK投票
    PK_VOTING,

    //警长举手
    SHERIFF_REGISTER,
    //警长竞选发言时间
    SHERIFF_RUNNING,
    //警长竞选投票
    SHERIFF_VOTING,
    //警长PK
    SHERIFF_PK,
    //警长PK投票
    SHERIFF_PK_VOTING,


    //猎人时间
    HUNTER_SHOOT,

    //白痴被票时间(白痴需要决定是否翻拍)
    MORON,
    ;
}
