package com.msh.room.model.room;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public interface RoomState {

    RoomStateData resolveJudgeEvent(JudgeEvent event);

    JudgeDisplayInfo displayJudgeInfo();

    RoomStateData resolvePlayerEvent(PlayerEvent event);

    PlayerDisplayInfo displayPlayerInfo(int seatNumber);
}
