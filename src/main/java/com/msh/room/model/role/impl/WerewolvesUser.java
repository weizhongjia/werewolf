package com.msh.room.model.role.impl;

import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.model.role.CommonUser;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class WerewolvesUser extends CommonUser {
    public WerewolvesUser(RoomStateData state, int number) {
        super(state, number);
    }

    @Override
    public RoomStateData killed() {
        return null;
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
        return null;
    }
}
