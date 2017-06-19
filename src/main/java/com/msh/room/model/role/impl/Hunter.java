package com.msh.room.model.role.impl;

import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.result.GameResult;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
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
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
        //游戏没结束
        if (roomState.getGameResult() == null) {
            //判断是否被毒
            if (roomState.getLastNightRecord().getWitchPoisoned() != number) {
                HunterState hunterState = new HunterState();
                //当前房间状态缓存
                hunterState.setNextStatus(roomState.getStatus());
                roomState.setHunterState(hunterState);
                roomState.setStatus(RoomStatus.HUNTER_SHOOT);
                //如果是警长死亡,会先要求移交警徽
                resolveSheriffDie();
            }
        }
        return roomState;
    }

    @Override
    public void calculateScore() {
        //TODO 猎人结算
        if(GameResult.VILLAGERS_WIN.equals(roomState.getGameResult())){
            PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(this.number);
            seatInfo.setFinalScore(5);
        }
    }

    @Override
    public RoomStateData voted() {
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(number);
        seatInfo.setAlive(false);
        gameEndingCalculate();
        //游戏没结束
        if (roomState.getGameResult() == null) {
            HunterState hunterState = new HunterState();
            //当前房间状态缓存
            hunterState.setNextStatus(roomState.getStatus());
            roomState.setHunterState(hunterState);
            roomState.setStatus(RoomStatus.HUNTER_SHOOT);
            //如果是警长死亡,会先要求移交警徽
            resolveSheriffDie();
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
