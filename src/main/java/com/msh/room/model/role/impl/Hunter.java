package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.state.HunterState;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;

/**
 * Created by zhangruiqian on 2017/5/7.
 */
public class Hunter extends AssignedPlayer {
    public Hunter(RoomStateData roomState, int number) {
        super(roomState, number);
    }

    @Override
    public RoomStateData killed() {
        roomState = super.killed();
        //游戏没结束
        if (roomState.getGameResult() == null) {
            //判断是否可以触发动作
            if (roomState.getLastNightRecord().getWitchPoisoned() != number) {
                HunterState hunterState = new HunterState();
                //当前房间状态缓存
                hunterState.setNextStatus(roomState.getStatus());
                roomState.setHunterState(hunterState);
                roomState.setStatus(RoomStatus.HUNTER_SHOOT);
            }
        }
        return roomState;
    }

    @Override
    public RoomStateData voted() {
        roomState = super.voted();
        //游戏没结束
        if (roomState.getGameResult() == null) {
            HunterState hunterState = new HunterState();
            //当前房间状态缓存
            hunterState.setNextStatus(roomState.getStatus());
            roomState.setHunterState(hunterState);
            roomState.setStatus(RoomStatus.HUNTER_SHOOT);
        }
        return roomState;
    }

    public RoomStateData shoot(int number) {
        if (number > 0) {
            if (!roomState.getPlaySeatInfoBySeatNumber(number).isAlive()) {
                throw new RoomBusinessException("该玩家已死亡，无法再开枪");
            }
            CommonPlayer commonPlayer = PlayerRoleFactory.createPlayerInstance(roomState, number);
            commonPlayer.killed();
        }
        roomState.setStatus(roomState.getHunterState().getNextStatus());
        roomState.getHunterState().setShootNumber(number);
        return roomState;
    }


    @Override
    public PlayerDisplayInfo displayInfo() {
        PlayerDisplayInfo displayInfo = new PlayerDisplayInfo();
        resolveCommonDisplayInfo(displayInfo);
        if (RoomStatus.HUNTER_SHOOT.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(PlayerEventType.HUNTER_SHOOT);
        }
        return displayInfo;
    }
}
