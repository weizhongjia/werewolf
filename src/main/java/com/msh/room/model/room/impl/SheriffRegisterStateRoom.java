package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class SheriffRegisterStateRoom extends AbstractStateRoom {
    public SheriffRegisterStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        return null;
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        return null;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        return null;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        return null;
    }
}
