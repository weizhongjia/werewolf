package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Hunter extends AssignedPlayer {
    public Hunter(RoomStateData roomState, int number) {
        super(roomState, number);
    }

//TODO 猎人需要单独处理kill和vote事件,并且需要接受shoot事件

    @Override
    public RoomStateData killed() {
        super.killed();
        //判断是否可以触发动作
        return roomState;
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        return displayInfo;
    }
}
