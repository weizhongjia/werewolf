package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Moron extends AssignedPlayer{
    public Moron(RoomStateData roomState, int number) {
        super(roomState, number);
    }
    //TODO 需要单独处理被投票情况


    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        return displayInfo;
    }
}
