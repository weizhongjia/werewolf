package com.msh.room.model.role;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.response.PlayerDisplayInfo;

/**
 * Created by zhangruiqian on 2017/5/4.
 */
public abstract class CommonPlayer {
    protected RoomStateData roomState;
    protected int number;


    public CommonPlayer(RoomStateData roomState, int number) {
        this.roomState = roomState;
        this.number = number;
    }

    public abstract RoomStateData killed();

    public abstract RoomStateData resolveEvent(PlayerEvent event);

    public abstract PlayerDisplayInfo displayInfo();

    //被投票
    public abstract RoomStateData voted();

    //能否投票
    public abstract boolean voteEnable();
}
