package com.msh.room.model.role;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.exception.RoomBusinessException;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class JudgeUser {
    private RoomStateData roomState;

    public JudgeUser(RoomStateData roomState) {
        this.roomState = roomState;
    }

    public RoomStateData resolveEvent(JudgeEvent event) throws RoomBusinessException {
        if (JudgeEventType.CREATE_ROOM.equals(event.getEventType())) {
            createRoom(event);
        }
        return roomState;
    }

    private void createRoom(JudgeEvent event) throws RoomBusinessException {
        if (roomState.getStatus().equals(RoomStatus.VACANCY)) {
            roomState.setStatus(RoomStatus.CRATING);
            roomState.setGameConfig(event.getGameConfig());
            //初始化座位
            roomState.setPlayerSeatInfo(initSeatInfo(event.getGameConfig()));
        } else {
            throw new RoomBusinessException("房间非空闲，无法创建游戏");
        }
    }

    private List<PlayerSeatInfo> initSeatInfo(Map<Roles, Integer> gameConfig) {
        List playerInfoList = new ArrayList();
        int seatNumber = 1;
        for (Roles role : gameConfig.keySet()) {
            Integer num = gameConfig.get(role);
            for (int i = 0; i < num; i++) {
                playerInfoList.add(new PlayerSeatInfo(seatNumber, true));
                seatNumber++;
            }
        }
        return playerInfoList;
    }


    public JudgeDisplayInfo displayInfo() {
        JudgeDisplayInfo displayInfo = new JudgeDisplayInfo(roomState.getRoomCode());
        displayInfo.setStatus(roomState.getStatus());
        displayInfo.setPlayerSeatInfo(roomState.getPlayerSeatInfo());
        return displayInfo;
    }
}
