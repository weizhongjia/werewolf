package com.msh.room.model.role;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.response.PlayerDisplayInfo;

/**
 * Created by zhangruiqian on 2017/5/4.
 */
public abstract class CommonUser {
    protected RoomStateData roomState;
    protected int number;


    public CommonUser(RoomStateData roomState, int number) {
        this.roomState = roomState;
        this.number = number;
    }

    public abstract RoomStateData killed();

    public abstract RoomStateData vote();

    public abstract RoomStateData resolveEvent(PlayerEvent event);

    public abstract PlayerDisplayInfo displayInfo();
}
