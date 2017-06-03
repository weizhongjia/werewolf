package com.msh.room.model.role.impl;

import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Villagers extends AssignedPlayer{
    public Villagers(RoomStateData roomState, int number) {
        super(roomState, number);
    }


    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        return displayInfo;
    }

}
