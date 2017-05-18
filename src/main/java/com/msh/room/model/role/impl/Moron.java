package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.util.PlayerRoleMask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Moron extends AssignedPlayer{
    public Moron(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData vote() {
        return null;
    }

    @Override
    public RoomStateData resolveEvent(PlayerEvent event) {
        return null;
    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        return displayInfo;
    }
}
