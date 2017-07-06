package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;

/**
 * 空玩家
 * Created by zhangruiqian on 2017/5/7.
 */
public class NonePlayer extends CommonPlayer {
    public NonePlayer(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData killed() {
        throw new RoomBusinessException("该用户角色不接受此类型事件");
    }

    @Override
    public RoomStateData resolveEvent(PlayerEvent event) {
        if (PlayerEventType.JOIN_ROOM.equals(event.getEventType())) {
//            resolveJoinEvent(event);
            return roomState;
        } else {
            throw new RoomBusinessException("该用户类型不接受此类型事件");
        }
    }

//    private void joinRoom(PlayerEvent event) {
//        if (event.getSeatNumber() < 0 || event.getSeatNumber() > roomState.getPlayerSeatInfo().size()) {
//            throw new RoomBusinessException("该房间无法容纳该座位号玩家,请检查游戏配置");
//        }
//        PlayerSeatInfo seatInfo = roomState.getPlayerSeatInfo().get(event.getSeatNumber() - 1);
//        if (seatInfo.isSeatAvailable() && seatInfo.getSeatNumber() == number) {
//            seatInfo.setRole(Roles.UNASSIGN);
//            seatInfo.setSeatAvailable(false);
//            seatInfo.setAlive(true);
//            seatInfo.setUserID(event.getUserID());
//        } else {
//            throw new RoomBusinessException("该座位已被占用，请联系法官");
//        }
//    }

    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        roomState.getPlayerSeatInfo().stream().filter(seatInfo -> seatInfo.getSeatNumber() == number).forEach(seatInfo -> {
            displayInfo.setPlayerInfo(seatInfo);
        });
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        if (displayInfo.getPlayerInfo().isSeatAvailable()) {
            displayInfo.addAcceptableEventType(PlayerEventType.JOIN_ROOM);
        }
        return displayInfo;
    }

    @Override
    public RoomStateData voted() {
        return roomState;
    }

    @Override
    public boolean voteEnable() {
        return false;
    }
}
